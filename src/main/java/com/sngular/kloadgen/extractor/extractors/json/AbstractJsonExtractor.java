package com.sngular.kloadgen.extractor.extractors.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.SchemaExtractorUtil;
import com.sngular.kloadgen.extractor.parser.impl.JSONSchemaParser;
import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.model.json.ArrayField;
import com.sngular.kloadgen.model.json.EnumField;
import com.sngular.kloadgen.model.json.Field;
import com.sngular.kloadgen.model.json.MapField;
import com.sngular.kloadgen.model.json.NumberField;
import com.sngular.kloadgen.model.json.ObjectField;
import com.sngular.kloadgen.model.json.StringField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractJsonExtractor {

  private static final JSONSchemaParser JSON_SCHEMA_PARSER = new JSONSchemaParser();

  protected final JSONSchemaParser getSchemaParser() {
    return JSON_SCHEMA_PARSER;
  }

  private static String extractFieldName(final String fieldName) {
    String fieldNameClean = fieldName;
    if (fieldName.endsWith("[][]") || fieldName.endsWith("[:][]")) {
      fieldNameClean = fieldName.substring(0, fieldName.length() - 2);
    } else if (fieldName.endsWith("[][:]") || fieldName.endsWith("[:][:]")) {
      fieldNameClean = fieldName.substring(0, fieldName.length() - 3);
    }
    return fieldNameClean;
  }

  private List<FieldValueMapping> extractInternalFields(final ObjectField field, final Boolean isAncestorRequired) {
    final List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Field innerField : field.getProperties()) {
      completeFieldList.addAll(processField(innerField, false, isAncestorRequired));
    }
    return completeFieldList;
  }

  private Transformer<FieldValueMapping, FieldValueMapping> fixName(final String fieldName, final String splitter) {
    return fieldValue -> {
      fieldValue.setFieldName(extractFieldName(fieldName) + splitter + fieldValue.getFieldName());
      return fieldValue;
    };
  }

  public final List<FieldValueMapping> processField(final Field innerField, final Boolean isRootElement, final Boolean isAncestorRequired) {
    final List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (innerField instanceof ObjectField) {
      processRecordFieldList(innerField.getName(), ".",
                             extractInternalFields((ObjectField) innerField, isAncestorRequired != null ? isAncestorRequired : ((ObjectField) innerField).isFieldRequired()),
                             completeFieldList, checkRequiredElement(isRootElement, isAncestorRequired, ((ObjectField) innerField).isFieldRequired()));
    } else if (innerField instanceof ArrayField) {
      completeFieldList.addAll(extractArrayInternalFields((ArrayField) innerField, isRootElement,
                                                          checkRequiredElement(isRootElement, isAncestorRequired, ((ArrayField) innerField).isFieldRequired()), ""));
    } else if (innerField instanceof EnumField) {
      completeFieldList.add(FieldValueMapping
                                .builder()
                                .fieldName(innerField.getName())
                                .fieldType(innerField.getType())
                                .valueLength(0)
                                .fieldValueList(String.join(",", ((EnumField) innerField).getEnumValues()))
                                .build());
    } else if (innerField instanceof MapField) {
      completeFieldList.addAll(
          extractMapInternalFields((MapField) innerField, isRootElement,
                                   checkRequiredElement(isRootElement, isAncestorRequired, ((MapField) innerField).isFieldRequired()), ""));
    } else if (innerField instanceof NumberField) {
      final FieldValueMapping.FieldValueMappingBuilder builder = FieldValueMapping
                                                                     .builder()
                                                                     .fieldName(innerField.getName())
                                                                     .fieldType(innerField.getType());

      addConstraint(builder, ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getExclusiveMaximum()));
      addConstraint(builder, ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getExclusiveMinimum()));
      addConstraint(builder, ConstraintTypeEnum.MAXIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getMaximum()));
      addConstraint(builder, ConstraintTypeEnum.MINIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getMinimum()));
      addConstraint(builder, ConstraintTypeEnum.MULTIPLE_OF, getSafeNumberAsString(((NumberField) innerField).getMultipleOf()));

      completeFieldList.add(builder.build());
    } else if (innerField instanceof StringField) {
      final FieldValueMapping.FieldValueMappingBuilder builder = FieldValueMapping
          .builder()
          .fieldName(innerField.getName())
          .fieldType(innerField.getType());

      addConstraint(builder, ConstraintTypeEnum.REGEX, ((StringField) innerField).getRegex());
      addConstraint(builder, ConstraintTypeEnum.MAXIMUM_VALUE, getSafeNumberAsString(((StringField) innerField).getMaxlength()));
      addConstraint(builder, ConstraintTypeEnum.MINIMUM_VALUE, getSafeNumberAsString(((StringField) innerField).getMinLength()));
      addConstraint(builder, ConstraintTypeEnum.FORMAT, ((StringField) innerField).getFormat());

      completeFieldList.add(builder.build());
    } else {
      completeFieldList.add(FieldValueMapping.builder().fieldName(innerField.getName()).fieldType(innerField.getType()).build());
    }
    return completeFieldList;
  }

  private void addConstraint(final FieldValueMapping.FieldValueMappingBuilder builder, final ConstraintTypeEnum constraint, final String constrainValue) {
    if (StringUtils.isNotBlank(constrainValue)) {
      builder.constraint(constraint, constrainValue);
    }
  }

  private String getSafeNumberAsString(final Number exclusiveMaximum) {
    String result = null;
    if (Objects.nonNull(exclusiveMaximum)) {
      result = exclusiveMaximum.toString();
    }
    return result;
  }

  private List<FieldValueMapping> extractObjectInternalFields(
      final ObjectField value, final String innerFieldName, final Boolean isAncestorRequired, final String breadCrumb,
      final String endValue) {
    final List<FieldValueMapping> completeFieldList = new ArrayList<>();
    final List<String> requiredInternalFields = value.getRequired();
    for (Field propertiesField : value.getProperties()) {
      final List<FieldValueMapping> processedField = processField(propertiesField, false, isAncestorRequired);
      processedField.get(0).setAncestorRequired(isAncestorRequired != null && isAncestorRequired);
      processedField.get(0).setRequired(checkRequiredByType(propertiesField, requiredInternalFields, processedField.get(0)));
      CollectionUtils.collect(
          processedField,
          fixName(StringUtils.isNotEmpty(breadCrumb) ? breadCrumb + endValue : innerFieldName, endValue + "."),
          completeFieldList);
    }

    return completeFieldList;
  }

  private List<FieldValueMapping> extractArrayInternalFields(
      final ArrayField innerField, final Boolean isRootElement,
      final Boolean isAncestorRequired, final String breadCrumb) {

    final List<FieldValueMapping> completeFieldList = new ArrayList<>();

    for (Field value : innerField.getValues()) {
      if (value instanceof ObjectField) {
        completeFieldList.addAll(extractObjectInternalFields((ObjectField) value, innerField.getName(), isAncestorRequired, breadCrumb, "[]"));
      } else if (value instanceof ArrayField) {
        completeFieldList.addAll(extractArrayInternalFields(
            (ArrayField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[]")));
      } else if (value instanceof MapField) {
        completeFieldList.addAll(extractMapInternalFields(
            (MapField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[]")));
      } else {
        final String name = (StringUtils.isNotEmpty(breadCrumb) ? breadCrumb : innerField.getName() + "[]") + (StringUtils.isNotEmpty(breadCrumb) ? "[]" : breadCrumb);
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(name)
                             .fieldType(calculateFieldType(breadCrumb, value))
                             .required(!name.endsWith("][]") && !name.endsWith("][:]") && innerField.isFieldRequired())
                             .isAncestorRequired(!isRootElement && isAncestorRequired != null && isAncestorRequired)
                             .build());
      }
    }
    return completeFieldList;
  }

  @NotNull
  private static String calculateFieldType(final String breadCrumb, final Field value) {
    return value.getType()
           + SchemaExtractorUtil.ARRAY_TYPE_POSTFIX
           + ((StringUtils.isNotEmpty(breadCrumb)
               && breadCrumb.endsWith("[]")) ? SchemaExtractorUtil.ARRAY_TYPE_POSTFIX
                  : (StringUtils.isNotEmpty(breadCrumb)
                     && breadCrumb.endsWith("[:]")) ? "-map"
                        : breadCrumb);
  }

  private List<FieldValueMapping> extractMapInternalFields(
      final MapField innerField, final Boolean isRootElement,
      final Boolean isAncestorRequired, final String breadCrumb) {
    final List<FieldValueMapping> completeFieldList = new ArrayList<>();
    final Field value = innerField.getMapType();

    if (value instanceof ObjectField) {
      completeFieldList.addAll(extractObjectInternalFields((ObjectField) value, innerField.getName(), isAncestorRequired, breadCrumb, "[:]"));
    } else if (value instanceof ArrayField) {
      completeFieldList.addAll(extractArrayInternalFields(
          (ArrayField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[:]")));
    } else if (value instanceof MapField) {
      completeFieldList.addAll(extractMapInternalFields(
          (MapField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[:]")));
    } else {
      final String name = (StringUtils.isNotEmpty(breadCrumb) ? breadCrumb : innerField.getName() + "[:]") + (StringUtils.isNotEmpty(breadCrumb) ? "[:]" : breadCrumb);
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(name)
                           .fieldType(getFieldType(breadCrumb, value))
                           .required(!name.endsWith("][]") && !name.endsWith("][:]") && innerField.isFieldRequired())
                           .isAncestorRequired(!isRootElement && isAncestorRequired != null && isAncestorRequired)
                           .build());
    }
    return completeFieldList;
  }

  @NotNull
  private static String getFieldType(final String breadCrumb, final Field value) {
    return value.getType()
           + "-map"
           + ((StringUtils.isNotEmpty(breadCrumb)
               && breadCrumb.endsWith("[:]")) ? "-map"
                  : (StringUtils.isNotEmpty(breadCrumb)
                     && breadCrumb.endsWith("[]")) ? SchemaExtractorUtil.ARRAY_TYPE_POSTFIX
                        : breadCrumb);
  }

  private void processRecordFieldList(final String fieldName, final String splitter, final List<FieldValueMapping> internalFields, final List<FieldValueMapping> completeFieldList,
      final boolean isAncestorRequired) {
    internalFields.forEach(internalField -> extracted(fieldName, splitter, completeFieldList, isAncestorRequired, internalField));
  }

  private static void extracted(final String fieldName, final String splitter, final List<FieldValueMapping> completeFieldList, final boolean isAncestorRequired,
      final FieldValueMapping internalField) {
    if (Objects.nonNull(internalField.getFieldName())) {
      internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
    } else {
      internalField.setFieldName(fieldName);
    }
    final String[] splitName = internalField.getFieldName().split("\\.");
    String parentName = splitName[splitName.length - 2];
    parentName = parentName.replace("[]", "");
    if (fieldName.equals(parentName)) {
      internalField.setAncestorRequired(isAncestorRequired);
    }
    completeFieldList.add(internalField);
  }

  private Boolean checkRequiredByType(
      final Field innerField, final List<String> requiredInternalFields,
      final FieldValueMapping fieldValueMapping) {
    boolean result = fieldValueMapping.getRequired();
    if (!(innerField instanceof ArrayField) && !(innerField instanceof MapField)) {
      result = requiredInternalFields.contains(fieldValueMapping.getFieldName());
    }
    return result;
  }

  private Boolean checkRequiredElement(final Boolean isRootElement, final Boolean isAncestorRequired, final Boolean isFieldRequired) {
    final boolean isRequired = isAncestorRequired != null && isAncestorRequired;
    return isRootElement != null && isRootElement ? isFieldRequired : isRequired;
  }

  private String generateBreadCrumb(final String breadCrumb, final String fieldName, final String endValue) {

    if (breadCrumb.endsWith("[]") || breadCrumb.endsWith("[:]")) {
      throw new KLoadGenException("Wrong Json Schema, 3+ consecutive nested collections are not allowed");
    }

    return fieldName + endValue;
  }

}
