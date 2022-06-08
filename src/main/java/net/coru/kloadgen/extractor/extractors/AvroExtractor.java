package net.coru.kloadgen.extractor.extractors;

import static net.coru.kloadgen.extractor.extractors.SchemaExtractorUtil.ARRAY_NAME_POSTFIX;
import static net.coru.kloadgen.extractor.extractors.SchemaExtractorUtil.ARRAY_TYPE_POSTFIX;
import static net.coru.kloadgen.extractor.extractors.SchemaExtractorUtil.MAP_NAME_POSTFIX;
import static net.coru.kloadgen.extractor.extractors.SchemaExtractorUtil.MAP_TYPE_POSTFIX;
import static net.coru.kloadgen.extractor.extractors.SchemaExtractorUtil.postFixNameArray;
import static net.coru.kloadgen.extractor.extractors.SchemaExtractorUtil.postFixNameMap;
import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.BOOLEAN;
import static org.apache.avro.Schema.Type.BYTES;
import static org.apache.avro.Schema.Type.DOUBLE;
import static org.apache.avro.Schema.Type.ENUM;
import static org.apache.avro.Schema.Type.FIXED;
import static org.apache.avro.Schema.Type.FLOAT;
import static org.apache.avro.Schema.Type.INT;
import static org.apache.avro.Schema.Type.LONG;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.NULL;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.STRING;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.avro.Schema;
import org.apache.commons.collections4.IteratorUtils;

public class AvroExtractor {

  private final Set<Schema.Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private final RandomObject randomObject = new RandomObject();

  public List<FieldValueMapping> processSchema(Schema schema) {
    List<FieldValueMapping> attributeList = new ArrayList<>();
    schema.getFields().forEach(field -> processField(field, attributeList, true, true));
    return attributeList;
  }

  public ParsedSchema getParsedSchema(String schema) {
    ParsedSchema parsed = new AvroSchema(schema);
    Schema schemaObj = (Schema) parsed.rawSchema();
    if (checkIfUnion(schemaObj)) {
      Schema lastElement = schemaObj.getTypes().get(schemaObj.getTypes().size() - 1);
      return new AvroSchema(lastElement.toString());
    }
    return parsed;
  }

  public void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList, boolean isAncestorRequired, boolean isAncestor) {
    if (checkIfRecord(innerField.schema())) {
      processRecordFieldList(innerField.name(), ".", processFieldList(innerField.schema().getFields(), isAncestorRequired), completeFieldList);
    } else if (checkIfArray(innerField.schema())) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), innerField.schema(), isAncestorRequired);
      if (internalFields.size() == 1 &&
          (internalFields.get(0).getFieldName().endsWith(ARRAY_NAME_POSTFIX + MAP_NAME_POSTFIX) || internalFields.get(0).getFieldName().endsWith(ARRAY_NAME_POSTFIX + ARRAY_NAME_POSTFIX))) {
        tweakType(internalFields.get(0), ARRAY_TYPE_POSTFIX);
      }
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField.schema())) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(postFixNameMap(innerField.name()), innerField.schema().getValueType(), isAncestorRequired);
      completeFieldList.addAll(internalFields);
    } else if (checkIfUnion(innerField.schema())) {
      if (isAncestor) {
        extractUnionRecord(innerField.name(), innerField.schema(), completeFieldList, false);
      } else {
        extractUnionRecord(innerField.name(), innerField.schema(), completeFieldList, isAncestorRequired);
      }
    } else {
      addFieldToList(innerField, completeFieldList, isAncestorRequired);
    }
  }

  private void processUnionField(String fieldName, List<FieldValueMapping> completeFieldList, Schema recordUnion, boolean isAncestorRequired) {
    if (checkIfRecord(recordUnion)) {
      processRecordFieldList(fieldName, ".", processFieldList(recordUnion.getFields(), isAncestorRequired), completeFieldList);
    } else if (checkIfArray(recordUnion)) {
      extractArray(fieldName, completeFieldList, recordUnion.getElementType(), isAncestorRequired);
    } else if (checkIfMap(recordUnion)) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(postFixNameMap(fieldName), recordUnion.getValueType(), isAncestorRequired);
      if (internalFields.size() == 1 && internalFields.get(0).getFieldName().endsWith(MAP_NAME_POSTFIX + MAP_NAME_POSTFIX)) {
        tweakType(internalFields.get(0), MAP_TYPE_POSTFIX);
      }
      completeFieldList.addAll(internalFields);
    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(recordUnion.getType().getName())
                           .required(checkIfRequiredField(recordUnion))
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );
    }
  }

  private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields, List<FieldValueMapping> completeFieldList) {
    if (completeFieldList.isEmpty()) {
      internalFields.forEach(internalField -> {
        if (internalField.getFieldName().startsWith(fieldName + ".")) {
          internalField.setFieldName(fieldName + internalField.getFieldName().replace(fieldName, splitter.replace(".", "")));
        } else {
          internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
        }
        completeFieldList.add(internalField);
      });
    } else {
      internalFields.forEach(internalField -> internalField.setFieldName(fieldName + splitter + internalField.getFieldName()));
    }
  }

  private List<FieldValueMapping> processFieldList(List<Schema.Field> fieldList, boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Schema.Field innerField : fieldList) {
      processField(innerField, completeFieldList, isAncestorRequired, false);
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractArrayInternalFields(String fieldName, Schema innerField, boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (checkIfUnion(innerField)) {
      extractUnionRecord(fieldName, innerField, completeFieldList, isAncestorRequired);
    } else if (checkIfRecord(innerField)) {
      for (Schema.Field arrayElementField : innerField.getFields()) {
        processField(arrayElementField, completeFieldList, isAncestorRequired, false);
      }
      processRecordFieldList(fieldName, ".", completeFieldList, new ArrayList<>());
    } else if (checkIfMap(innerField)) {
      completeFieldList.addAll(extractMapInternalFields(postFixNameMap(fieldName), innerField.getValueType(), isAncestorRequired));
    } else if (checkIfArray(innerField)) {
      completeFieldList.addAll(extractArrayInternalFields(postFixNameArray(fieldName), innerField.getElementType(), isAncestorRequired));
    } else if (typesSet.contains(innerField.getType())) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(innerField.getName() + ARRAY_TYPE_POSTFIX)
                           .required(isAncestorRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );
    }
    return completeFieldList;
  }

  private void extractArray(String fieldName, List<FieldValueMapping> completeFieldList, Schema recordUnion, boolean isAncestorRequired) {
    List<FieldValueMapping> internalFields = extractArrayInternalFields(fieldName, recordUnion, isAncestorRequired);
    if (checkIfRecord(recordUnion)) {
      processRecordFieldList(fieldName, ARRAY_NAME_POSTFIX + ".", internalFields, completeFieldList);
    } else if (checkIfMap(recordUnion)) {
      internalFields.forEach(field -> {
        field.setFieldName(field.getFieldName().replace(MAP_NAME_POSTFIX, ARRAY_NAME_POSTFIX + MAP_NAME_POSTFIX));
        if (field.getFieldName().matches(".*\\[:?](\\[:?])?$")) {
          tweakType(field, ARRAY_TYPE_POSTFIX);
        }
        completeFieldList.add(field);
      });
    } else if (checkIfArray(recordUnion)) {
      tweakType(internalFields.get(0), ARRAY_TYPE_POSTFIX);
      createArrayType(completeFieldList, internalFields, postFixNameArray(fieldName));
    } else {
      renameArrayType(completeFieldList, internalFields, postFixNameArray(fieldName));
    }
  }

  private List<FieldValueMapping> extractMapInternalFields(String fieldName, Schema innerField, boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (checkIfUnion(innerField)) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(getNotNullType(innerField.getTypes()) + MAP_TYPE_POSTFIX)
                           .required(checkIfRequiredField(innerField))
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );

    } else if (checkIfRecord(innerField)) {
      if (innerField.getFields().size() > 1) {
        processRecordFieldList(fieldName, ".", processFieldList(innerField.getFields(), isAncestorRequired), completeFieldList);
      } else {
        processRecordFieldList(innerField.getName(), ".", processFieldList(innerField.getFields().get(0).schema().getFields(), isAncestorRequired), completeFieldList);
      }
    } else if (checkIfArray(innerField)) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(postFixNameArray(fieldName), innerField.getElementType(), isAncestorRequired);
      internalFields.forEach(field -> {
        if (field.getFieldType().endsWith(ARRAY_TYPE_POSTFIX) && field.getFieldName().endsWith(MAP_NAME_POSTFIX + ARRAY_NAME_POSTFIX)) {
          tweakType(field, MAP_TYPE_POSTFIX);
        }
      });
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField) && !checkIfRecord(innerField.getValueType())) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(postFixNameMap(fieldName), innerField.getValueType(), isAncestorRequired);
      completeFieldList.addAll(internalFields);
    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(innerField.getType().getName() + MAP_TYPE_POSTFIX)
                           .required(isAncestorRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );

    }
    return completeFieldList;
  }

  private void addFieldToList(Schema.Field innerField, List<FieldValueMapping> completeFieldList, boolean isAncestorRequired) {
    String typeName = innerField.schema().getType().getName();
    if (checkIfLogicalType(innerField.schema())) {
      typeName += "_" + innerField.schema().getLogicalType().getName();
    }
    if (checkIfEnumField(innerField.schema().getType())) {
      String fieldValueList = String.join(",", innerField.schema().getEnumSymbols());
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(innerField.name())
                           .fieldType(typeName)
                           .fieldValueList(fieldValueList)
                           .required(checkIfRequiredField(innerField.schema()))
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );

    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(innerField.name())
                           .fieldType(typeName)
                           .required(checkIfRequiredField(innerField.schema()))
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );
    }
  }

  private void extractUnionRecord(String fieldName, Schema innerField, List<FieldValueMapping> completeFieldList, boolean isAncestorRequired) {
    Schema recordUnion = getRecordUnion(innerField.getTypes());
    if (Objects.nonNull(recordUnion)) {
      if (typesSet.contains(recordUnion.getType())) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(fieldName)
                             .fieldType(getNotNullType(innerField.getTypes()))
                             .isAncestorRequired(isAncestorRequired)
                             .build()
        );
      } else if (checkIfArray(recordUnion) && typesSet.contains(recordUnion.getElementType().getType())) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(postFixNameArray(fieldName))
                             .fieldType(getNotNullType(innerField.getTypes()))
                             .isAncestorRequired(isAncestorRequired)
                             .build()
        );

      } else if (checkIfMap(recordUnion) && typesSet.contains(recordUnion.getValueType().getType())) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(postFixNameMap(fieldName))
                             .fieldType(getNotNullType(innerField.getTypes()))
                             .isAncestorRequired(isAncestorRequired)
                             .build()
        );
      } else {
        processUnionField(fieldName, completeFieldList, recordUnion, isAncestorRequired);
      }
    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(innerField.getName())
                           .fieldType(getNotNullType(innerField.getTypes()))
                           .required(checkIfRequiredField(innerField))
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );
    }
  }

  private String getNotNullType(List<Schema> types) {
    Schema chosenType = extractTypeName(types.get(0)).equalsIgnoreCase("null") ? types.get(1) : types.get(0);
    chosenType = extractTypeName(types.get(1)).equalsIgnoreCase("array") ? types.get(1) : chosenType;
    String chosenTypeName = extractTypeName(chosenType);
    if (!randomObject.isTypeValid(chosenTypeName)) {
      chosenTypeName = "null";
    } else if ("array".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getElementType().getName() + ARRAY_TYPE_POSTFIX;
    } else if ("map".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getValueType().getName() + MAP_TYPE_POSTFIX;
    }
    return chosenTypeName;
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema schema : types) {
      if (checkIfRecord(schema) || checkIfArray(schema) || checkIfMap(schema) || typesSet.contains(schema.getType())) {
        isRecord = schema;
      }
    }
    return isRecord;
  }

  private void createArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {
    internalFields.forEach(internalField -> {
      if (!internalField.getFieldName().contains(ARRAY_NAME_POSTFIX + MAP_NAME_POSTFIX) && !internalField.getFieldName().contains(ARRAY_NAME_POSTFIX + ARRAY_NAME_POSTFIX)) {
        internalField.setFieldName(internalField.getFieldName().replace(fieldName, postFixNameMap(fieldName)));
      }
    });
    completeFieldList.addAll(internalFields);
  }

  private void renameArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {
    internalFields.forEach(internalField -> internalField.setFieldName(fieldName));
    completeFieldList.addAll(internalFields);
  }

  private void tweakType(FieldValueMapping internalField, String postfix) {
    internalField.setFieldType(internalField.getFieldType() + postfix);
  }

  private String extractTypeName(Schema schema) {
    return schema.getType().getName();
  }

  private boolean checkIfRecord(Schema innerSchema) {
    return RECORD.equals(innerSchema.getType());
  }

  private boolean checkIfMap(Schema innerSchema) {
    return MAP.equals(innerSchema.getType());
  }

  private boolean checkIfArray(Schema innerSchema) {
    return ARRAY.equals(innerSchema.getType());
  }

  private boolean checkIfUnion(Schema innerSchema) {
    return UNION.equals(innerSchema.getType());
  }

  private boolean checkIfEnumField(Schema.Type type) {
    return type.equals(ENUM);
  }

  private boolean checkIfLogicalType(Schema innerSchema) {
    return Objects.nonNull(innerSchema.getLogicalType());
  }

  private boolean checkIfRequiredField(Schema innerSchema) {
    if (checkIfUnion(innerSchema)) {
      return !IteratorUtils.matchesAny(innerSchema.getTypes().iterator(), type -> type.getType() == NULL);
    } else {
      return true;
    }
  }

}
