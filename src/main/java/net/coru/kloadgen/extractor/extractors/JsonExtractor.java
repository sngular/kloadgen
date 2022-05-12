package net.coru.kloadgen.extractor.extractors;

import static java.lang.String.join;

import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.FORMAT;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MULTIPLE_OF;
import static net.coru.kloadgen.model.ConstraintTypeEnum.REGEX;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import net.coru.kloadgen.extractor.parser.impl.JSONSchemaParser;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.model.json.ArrayField;
import net.coru.kloadgen.model.json.EnumField;
import net.coru.kloadgen.model.json.Field;
import net.coru.kloadgen.model.json.MapField;
import net.coru.kloadgen.model.json.NumberField;
import net.coru.kloadgen.model.json.ObjectField;
import net.coru.kloadgen.model.json.Schema;
import net.coru.kloadgen.model.json.StringField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

public class JsonExtractor {

  private final JSONSchemaParser jsonSchemaParser = new JSONSchemaParser();

  public List<FieldValueMapping> processSchema(JsonNode jsonNode) {
    return processSchema(jsonSchemaParser.parse(jsonNode));
  }

  public List<FieldValueMapping> processSchema(Schema schema) {
    List<FieldValueMapping> attributeList = new ArrayList<>();

    schema.getProperties().forEach(field -> attributeList.addAll(processField(field, true, null)));

    Set<String> requiredFields = new HashSet<String>(schema.getRequiredFields());

    for (FieldValueMapping field : attributeList) {
      if (!field.getFieldName().contains("[]") && !field.getFieldName().contains("[:]")) {
        field.setRequired(requiredFields.contains(field.getFieldName()));
      }
    }
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(ObjectField field, Boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Field innerField : field.getProperties()) {
      completeFieldList.addAll(processField(innerField, false, isAncestorRequired));
    }
    return completeFieldList;
  }

  private Transformer<FieldValueMapping, FieldValueMapping> fixName(String fieldName, String splitter) {
    String[] fieldNameClean = new String[1];
    return fieldValue -> {
      if (fieldName.endsWith("[][]") || fieldName.endsWith("[:][]")) {
        fieldNameClean[0] = fieldName.substring(0, fieldName.length() - 2);
      } else if (fieldName.endsWith("[][:]") || fieldName.endsWith("[:][:]")) {
        fieldNameClean[0] = fieldName.substring(0, fieldName.length() - 3);
      } else {
        fieldNameClean[0] = fieldName;
      }

      fieldValue.setFieldName(fieldNameClean[0] + splitter + fieldValue.getFieldName());
      return fieldValue;
    };
  }

  private List<FieldValueMapping> processField(Field innerField, Boolean isRootElement, Boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
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
                                .fieldValueList(join(",", ((EnumField) innerField).getEnumValues()))
                                .build());
    } else if (innerField instanceof MapField) {
      completeFieldList.addAll(
          extractMapInternalFields((MapField) innerField, isRootElement,
                                   checkRequiredElement(isRootElement, isAncestorRequired, ((MapField) innerField).isFieldRequired()), ""));
    } else if (innerField instanceof NumberField) {
      FieldValueMapping.FieldValueMappingBuilder builder = FieldValueMapping
          .builder()
          .fieldName(innerField.getName())
          .fieldType(innerField.getType());

      addConstraint(builder, EXCLUDED_MAXIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getExclusiveMaximum()));
      addConstraint(builder, EXCLUDED_MINIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getExclusiveMinimum()));
      addConstraint(builder, MAXIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getMaximum()));
      addConstraint(builder, MINIMUM_VALUE, getSafeNumberAsString(((NumberField) innerField).getMinimum()));
      addConstraint(builder, MULTIPLE_OF, getSafeNumberAsString(((NumberField) innerField).getMultipleOf()));

      completeFieldList.add(builder.build());
    } else if (innerField instanceof StringField) {
      FieldValueMapping.FieldValueMappingBuilder builder = FieldValueMapping
          .builder()
          .fieldName(innerField.getName())
          .fieldType(innerField.getType());

      addConstraint(builder, REGEX, ((StringField) innerField).getRegex());
      addConstraint(builder, MAXIMUM_VALUE, getSafeNumberAsString(((StringField) innerField).getMaxlength()));
      addConstraint(builder, MINIMUM_VALUE, getSafeNumberAsString(((StringField) innerField).getMinLength()));
      addConstraint(builder, FORMAT, ((StringField) innerField).getFormat());

      completeFieldList.add(builder.build());
    } else {
      completeFieldList.add(FieldValueMapping.builder().fieldName(innerField.getName()).fieldType(innerField.getType()).build());
    }
    return completeFieldList;
  }

  private void addConstraint(FieldValueMapping.FieldValueMappingBuilder builder, ConstraintTypeEnum constrain, String constrainValue) {
    if (StringUtils.isNotBlank(constrainValue)) {
      builder.constrain(constrain, constrainValue);
    }
  }

  private String getSafeNumberAsString(Number exclusiveMaximum) {
    String result = null;
    if (Objects.nonNull(exclusiveMaximum)) {
      result = exclusiveMaximum.toString();
    }
    return result;
  }

  private List<FieldValueMapping> extractArrayInternalFields(
      ArrayField innerField, Boolean isRootElement,
      Boolean isAncestorRequired, String breadCrumb) {

    List<FieldValueMapping> completeFieldList = new ArrayList<>();

    for (Field value : innerField.getValues()) {
      if (value instanceof ObjectField) {

        List<String> requiredInternalFields = ((ObjectField) value).getRequired();
        for (Field propertiesField : value.getProperties()) {
          List<FieldValueMapping> processedField = processField(propertiesField, false, isAncestorRequired);
          processedField.get(0).setAncestorRequired(isAncestorRequired != null && isAncestorRequired);
          processedField.get(0).setRequired(checkRequiredByType(propertiesField, requiredInternalFields, processedField.get(0)));
          CollectionUtils.collect(
              processedField,
              fixName(StringUtils.isNotEmpty(breadCrumb) ? breadCrumb + "[]" : innerField.getName(), "[]."),
              completeFieldList);
        }

      } else if (value instanceof ArrayField) {
        completeFieldList.addAll(extractArrayInternalFields(
            (ArrayField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[]")));
      } else if (value instanceof MapField) {
        completeFieldList.addAll(extractMapInternalFields(
            (MapField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[]")));
      } else {
        String name = (StringUtils.isNotEmpty(breadCrumb) ? breadCrumb : innerField.getName() + "[]") + (StringUtils.isNotEmpty(breadCrumb) ? "[]" : breadCrumb);
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(name)
                             .fieldType(value.getType() + "-array" + ((StringUtils.isNotEmpty(breadCrumb) && breadCrumb.endsWith("[]")) ? "-array" :
                                 (StringUtils.isNotEmpty(breadCrumb) && breadCrumb.endsWith("[:]")) ? "-map" : breadCrumb))
                             .required(!name.endsWith("][]") && !name.endsWith("][:]") && innerField.isFieldRequired())
                             .isAncestorRequired(!isRootElement && (isAncestorRequired != null && isAncestorRequired))
                             .build());
      }
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractMapInternalFields(
      MapField innerField, Boolean isRootElement,
      Boolean isAncestorRequired, String breadCrumb) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    Field value = innerField.getMapType();

    if (value instanceof ObjectField) {
      List<String> requiredInternalFields = ((ObjectField) value).getRequired();
      for (Field propertiesField : value.getProperties()) {
        List<FieldValueMapping> processedField = processField(propertiesField, false, isAncestorRequired);
        processedField.get(0).setAncestorRequired(isAncestorRequired != null && isAncestorRequired);
        processedField.get(0).setRequired(checkRequiredByType(propertiesField, requiredInternalFields, processedField.get(0)));
        CollectionUtils.collect(
            processedField,
            fixName(StringUtils.isNotEmpty(breadCrumb) ? breadCrumb + "[:]" : innerField.getName(), "[:]."),
            completeFieldList);
      }
    } else if (value instanceof ArrayField) {
      completeFieldList.addAll(extractArrayInternalFields(
          (ArrayField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[:]")));
    } else if (value instanceof MapField) {
      completeFieldList.addAll(extractMapInternalFields(
          (MapField) value, false, isAncestorRequired, generateBreadCrumb(breadCrumb, innerField.getName(), "[:]")));
    } else {
      String name = (StringUtils.isNotEmpty(breadCrumb) ? breadCrumb : innerField.getName() + "[:]") + (StringUtils.isNotEmpty(breadCrumb) ? "[:]" : breadCrumb);
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(name)
                           .fieldType(value.getType() + "-map" + ((StringUtils.isNotEmpty(breadCrumb) && breadCrumb.endsWith("[:]")) ? "-map" :
                               (StringUtils.isNotEmpty(breadCrumb) && breadCrumb.endsWith("[]")) ? "-array" : breadCrumb))
                           .required(!name.endsWith("][]") && !name.endsWith("][:]") && innerField.isFieldRequired())
                           .isAncestorRequired(!isRootElement && (isAncestorRequired != null && isAncestorRequired))
                           .build());
    }
    return completeFieldList;
  }

  private void processRecordFieldList(
      String fieldName, String splitter, List<FieldValueMapping> internalFields,
      List<FieldValueMapping> completeFieldList, boolean isAncestorRequired) {
    internalFields.forEach(internalField -> {
      if (Objects.nonNull(internalField.getFieldName())) {
        internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      } else {
        internalField.setFieldName(fieldName);
      }
      String[] splittedName = internalField.getFieldName().split("\\.");
      String parentName = splittedName[splittedName.length - 2];
      parentName = parentName.replace("[]", "");
      if (fieldName.equals(parentName)) {
        internalField.setAncestorRequired(isAncestorRequired);
      }
      completeFieldList.add(internalField);
    });
  }

  private Boolean checkRequiredByType(
      Field innerField, List<String> requiredInternalFields,
      FieldValueMapping fieldValueMapping) {

    if (!(innerField instanceof ArrayField) && !(innerField instanceof MapField)) {
      return requiredInternalFields.contains(fieldValueMapping.getFieldName());
    }
    return fieldValueMapping.getRequired();
  }

  private Boolean checkRequiredElement(Boolean isRootElement, Boolean isAncestorRequired, Boolean isFieldRequired) {
    boolean isRequired = isAncestorRequired != null ? isAncestorRequired : false;
    return isRootElement != null && isRootElement ? isFieldRequired : isRequired;
  }

  private String generateBreadCrumb(String breadCrumb, String fieldName, String endValue) {

    if (breadCrumb.endsWith("[]") || breadCrumb.endsWith("[:]")) {
      throw new net.coru.kloadgen.exception.KLoadGenException("Wrong Json Schema, 3+ consecutive nested collections are not allowed");
    }

    return fieldName + endValue;
  }

}
