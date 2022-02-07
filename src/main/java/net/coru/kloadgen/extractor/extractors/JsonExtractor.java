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

    schema.getProperties().forEach(field -> attributeList.addAll(processField(field)));

    Set<String> requiredFields = new HashSet<String>(schema.getRequiredFields());

    for (FieldValueMapping field: attributeList){
      if (!field.getFieldName().contains("[]")){
        field.setRequired(requiredFields.contains(field.getFieldName()));
      }
    }
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(ObjectField field) {
    return processFieldList(field.getProperties());
  }

  private List<FieldValueMapping> processFieldList(List<Field> fieldList) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for(Field innerField : fieldList) {
      completeFieldList.addAll(processField(innerField));
    }
    return completeFieldList;
  }


  private Transformer<FieldValueMapping, FieldValueMapping> fixName(String fieldName, String splitter) {
    return fieldValue-> {
      fieldValue.setFieldName(fieldName + splitter + fieldValue.getFieldName());
      return fieldValue;
    };
  }

  private List<FieldValueMapping> processField(Field innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (innerField instanceof ObjectField) {
      processRecordFieldList(innerField.getName(), ".", extractInternalFields((ObjectField)innerField), completeFieldList, ((ObjectField) innerField).isFieldRequired());
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
        boolean isFieldRequired = ((ObjectField) value).isFieldRequired();
        List<String> requiredInternalFields = ((ObjectField) value).getRequired();
        for (Field arrayElementField : value.getProperties()) {
          List<FieldValueMapping> processedField = processField(arrayElementField);
          processedField.get(0).setParentRequired(isFieldRequired);
          processedField.get(0).setRequired(requiredInternalFields.contains(processedField.get(0).getFieldName()));

          CollectionUtils.collect(
              processedField,
              fixName(innerField.getName(), "[]."),
              completeFieldList);
        }
      } else {
        completeFieldList.add(FieldValueMapping.builder().fieldName(innerField.getName() + "[]").fieldType( value.getType() + "-array").build());
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
      completeFieldList.add(FieldValueMapping.builder().fieldName(innerField.getName() + "[]").fieldType(value.getType() + "-map")
              .required(innerField.isFieldRequired()).build());
    }
    return completeFieldList;
  }

  private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields, List<FieldValueMapping> completeFieldList, boolean isParentRequired) {
    internalFields.forEach(internalField -> {
      if (Objects.nonNull(internalField.getFieldName())) {
        internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      } else {
        internalField.setFieldName(fieldName);
      }
      String[] splittedName = internalField.getFieldName().split("\\.");
      String parentName = splittedName[splittedName.length-2];
      parentName = parentName.replace("[]","");
      if (fieldName.equals(parentName)){
        internalField.setParentRequired(isParentRequired);
      }
      completeFieldList.add(internalField);
    });
  }

}
