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
import java.util.List;
import java.util.Objects;

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

    schema.getProperties().forEach(field -> attributeList.addAll(processField(field)));
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(ObjectField field) {
    return processFieldList(field.getProperties());
  }

  private List<FieldValueMapping> processFieldList(List<Field> fieldList) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Field innerField : fieldList) {
      completeFieldList.addAll(processField(innerField));
    }
    return completeFieldList;
  }

  private Transformer<FieldValueMapping, FieldValueMapping> fixName(String fieldName, String splitter) {
    return fieldValue -> {
      fieldValue.setFieldName(fieldName + splitter + fieldValue.getFieldName());
      return fieldValue;
    };
  }

  private List<FieldValueMapping> processField(Field innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (innerField instanceof ObjectField) {
      processRecordFieldList(innerField.getName(), ".", extractInternalFields((ObjectField) innerField), completeFieldList);
    } else if (innerField instanceof ArrayField) {
      completeFieldList.addAll(extractArrayInternalFields((ArrayField) innerField));
    } else if (innerField instanceof EnumField) {
      completeFieldList.add(FieldValueMapping
                                .builder()
                                .fieldName(innerField.getName())
                                .fieldType(innerField.getType())
                                .valueLength(0)
                                .fieldValueList(join(",", ((EnumField) innerField).getEnumValues()))
                                .build());
    } else if (innerField instanceof MapField) {
      completeFieldList.addAll(extractMapInternalFields((MapField) innerField));
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

  private List<FieldValueMapping> extractArrayInternalFields(ArrayField innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Field value : innerField.getValues()) {
      if (value instanceof ObjectField) {
        for (Field arrayElementField : value.getProperties()) {
          CollectionUtils.collect(
              processField(arrayElementField),
              fixName(innerField.getName(), "[]."),
              completeFieldList);
        }
      } else {
        completeFieldList.add(FieldValueMapping.builder().fieldName(innerField.getName() + "[]").fieldType(value.getType() + "-array").build());
      }
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractMapInternalFields(MapField innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    Field value = innerField.getMapType();
    if (value instanceof ObjectField) {
      for (Field arrayElementField : value.getProperties()) {
        CollectionUtils.collect(
            processField(arrayElementField),
            fixName(innerField.getName(), "[][]."),
            completeFieldList);
      }
    } else {
      completeFieldList.add(FieldValueMapping.builder().fieldName(innerField.getName() + "[]").fieldType(value.getType() + "-map").build());
    }
    return completeFieldList;
  }

  private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields, List<FieldValueMapping> completeFieldList) {
    internalFields.forEach(internalField -> {
      if (Objects.nonNull(internalField.getFieldName())) {
        internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      } else {
        internalField.setFieldName(fieldName);
      }
      completeFieldList.add(internalField);
    });
  }

}
