package net.coru.kloadgen.extractor.extractors;

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

  public static final String ARRAY_POSTFIX = "-array";

  public static final String MAP_POSTFIX = "-map";

  public static final String MAP_ARRAY = "[][:]";

  private final Set<Schema.Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private final RandomObject randomObject = new RandomObject();

  public List<FieldValueMapping> processSchema(Schema schema) {
    List<FieldValueMapping> attributeList = new ArrayList<>();
    schema.getFields().forEach(field -> processField(field, attributeList));
    return attributeList;
  }

  public ParsedSchema getParsedSchema(String schema) {
    ParsedSchema parsed = new AvroSchema(schema);
    Schema schemaObj = (Schema) parsed.rawSchema();
    if (schemaObj.getType().equals(Schema.Type.UNION)) {
      Schema lastElement = schemaObj.getTypes().get(schemaObj.getTypes().size() - 1);
      return new AvroSchema(lastElement.toString());
    }
    return parsed;
  }

  public void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
    if (checkIfRecord(innerField.schema())) {
      processRecordFieldList(innerField.name(), ".", processFieldList(innerField.schema().getFields(),true), completeFieldList);
    } else if (checkIfArray(innerField.schema())) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), innerField.schema(), true);
      if (internalFields.size() == 1 && (internalFields.get(0).getFieldName().endsWith(MAP_ARRAY) || internalFields.get(0).getFieldName().endsWith("[][]"))) {
        tweakType(internalFields.get(0), ARRAY_POSTFIX);
      }
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField.schema())) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name() + "[:]", innerField.schema().getValueType(), true);
      completeFieldList.addAll(internalFields);
    } else if (checkIfUnion(innerField.schema())) {
      extractUnionRecord(innerField.name(), innerField.schema(), completeFieldList, false);
    } else {
      addFieldToList(innerField, completeFieldList);
    }
  }

  private void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList, boolean isAncestorRequired) {
    if (checkIfRecord(innerField.schema())) {
      processRecordFieldList(innerField.name(), ".", processFieldList(innerField.schema().getFields(), isAncestorRequired), completeFieldList);
    } else if (checkIfArray(innerField.schema())) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), innerField.schema(), isAncestorRequired);
      if (internalFields.size() == 1 && (internalFields.get(0).getFieldName().endsWith(MAP_ARRAY) || internalFields.get(0).getFieldName().endsWith("[][]"))) {
        tweakType(internalFields.get(0), ARRAY_POSTFIX);
      }
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField.schema())) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name() + "[:]", innerField.schema().getValueType(), isAncestorRequired);
      completeFieldList.addAll(internalFields);
    } else if (checkIfUnion(innerField.schema())) {
      extractUnionRecord(innerField.name(), innerField.schema(), completeFieldList, isAncestorRequired);
    } else {
      addFieldToList(innerField, completeFieldList, isAncestorRequired);
    }
  }

  private void processField(String fieldName, List<FieldValueMapping> completeFieldList, Schema recordUnion, boolean isAncestorRequired) {
    if (checkIfRecord(recordUnion)) {
      processRecordFieldList(fieldName, ".", processFieldList(recordUnion.getFields(), isAncestorRequired), completeFieldList);
    } else if (checkIfArray(recordUnion)) {
      extractArray(fieldName, completeFieldList, recordUnion.getElementType(), isAncestorRequired);
    } else if (checkIfMap(recordUnion)) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(fieldName + "[:]", recordUnion.getValueType(), isAncestorRequired);
      if (internalFields.size() == 1 && internalFields.get(0).getFieldName().endsWith("[:][:]")) {
        tweakType(internalFields.get(0), MAP_POSTFIX);
      }
      completeFieldList.addAll(internalFields);
    } else {
      completeFieldList.add(new FieldValueMapping(fieldName, recordUnion.getType().getName(), 0, "", checkIfRequiredField(recordUnion), isAncestorRequired));
    }
  }

  private List<FieldValueMapping> processFieldList(List<Schema.Field> fieldList, boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Schema.Field innerField : fieldList) {
      processField(innerField, completeFieldList, isAncestorRequired);
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractArrayInternalFields(String fieldName, Schema innerField, boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (checkIfUnion(innerField)) {
      extractUnionRecord(fieldName, innerField, completeFieldList, isAncestorRequired);
    } else if (checkIfRecord(innerField)) {
      for (Schema.Field arrayElementField : innerField.getFields()) {
        processField(arrayElementField, completeFieldList, isAncestorRequired);
      }
      processRecordFieldList(fieldName, ".", completeFieldList);
    } else if (checkIfMap(innerField)) {
      completeFieldList.addAll(extractMapInternalFields(fieldName + "[:]", innerField.getValueType(), isAncestorRequired));
    } else if (checkIfArray(innerField)) {
      completeFieldList.addAll(extractArrayInternalFields(fieldName + "[]", innerField.getElementType(), isAncestorRequired));
    } else if (typesSet.contains(innerField.getType())) {
      completeFieldList.add(new FieldValueMapping(fieldName, innerField.getName() + ARRAY_POSTFIX, 0, "", isAncestorRequired, isAncestorRequired));
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractMapInternalFields(String fieldName, Schema innerField, boolean isAncestorRequired) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (checkIfUnion(innerField)) {
      completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getTypes()) + MAP_POSTFIX, 0, "", checkIfRequiredField(innerField), isAncestorRequired));
    } else if (checkIfRecord(innerField)) {
      if (innerField.getFields().size() > 1) {
        processRecordFieldList(fieldName, ".", processFieldList(innerField.getFields(), isAncestorRequired), completeFieldList);
      } else {
        processRecordFieldList(innerField.getName(), ".", processFieldList(innerField.getFields().get(0).schema().getFields(), isAncestorRequired), completeFieldList);
      }
    } else if (checkIfArray(innerField)) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(fieldName + "[]", innerField.getElementType(), isAncestorRequired);
      internalFields.forEach(field -> {
        if (field.getFieldType().endsWith(ARRAY_POSTFIX) && field.getFieldName().endsWith("[:][]")) {
          tweakType(field, MAP_POSTFIX);
        }
      });
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField) && !checkIfRecord(innerField.getValueType())) {
      List<FieldValueMapping> internalFields = extractMapInternalFields(fieldName + "[:]", innerField.getValueType(), isAncestorRequired);
      completeFieldList.addAll(internalFields);
    } else {
      completeFieldList.add(new FieldValueMapping(fieldName, innerField.getType().getName() + MAP_POSTFIX, 0, "", isAncestorRequired, isAncestorRequired));
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
      completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, fieldValueList, checkIfRequiredField(innerField.schema()), isAncestorRequired));
    } else {
      completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, "", checkIfRequiredField(innerField.schema()), isAncestorRequired));
    }
  }

  public void addFieldToList(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
    String typeName = innerField.schema().getType().getName();
    if (checkIfLogicalType(innerField.schema())) {
      typeName += "_" + innerField.schema().getLogicalType().getName();
    }
    if (checkIfEnumField(innerField.schema().getType())) {
      String fieldValueList = String.join(",", innerField.schema().getEnumSymbols());
      completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, fieldValueList));
    } else {
      completeFieldList.add(new FieldValueMapping(innerField.name(), typeName));
    }
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

  private void extractUnionRecord(String fieldName, Schema innerField, List<FieldValueMapping> completeFieldList, boolean isAncestorRequired) {
    Schema recordUnion = getRecordUnion(innerField.getTypes());
    if (null != recordUnion) {
      if (typesSet.contains(recordUnion.getType())) {
        completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getTypes()), 0, "", false, isAncestorRequired));
      } else if (recordUnion.getType().equals(ARRAY) && typesSet.contains(recordUnion.getElementType().getType())) {
        completeFieldList.add(new FieldValueMapping(fieldName + "[]", getNotNullType(innerField.getTypes()), 0, "", false, isAncestorRequired));
      } else if (recordUnion.getType().equals(MAP) && typesSet.contains(recordUnion.getValueType().getType())) {
        completeFieldList.add(new FieldValueMapping(fieldName + "[:]", getNotNullType(innerField.getTypes()), 0, "", false, isAncestorRequired));
      } else {
        processField(fieldName, completeFieldList, recordUnion, isAncestorRequired);
      }
    } else {
      completeFieldList.add(new FieldValueMapping(innerField.getName(), getNotNullType(innerField.getTypes()), 0, "", checkIfRequiredField(innerField), isAncestorRequired));
    }
  }

  private void extractArray(String fieldName, List<FieldValueMapping> completeFieldList, Schema recordUnion, boolean isAncestorRequired) {
    List<FieldValueMapping> internalFields = extractArrayInternalFields(fieldName, recordUnion, isAncestorRequired);
    if (checkIfRecord(recordUnion)) {
      processRecordFieldList(fieldName, "[].", internalFields, completeFieldList);
    } else if (checkIfMap(recordUnion)) {
      internalFields.forEach(field -> {
        field.setFieldName(field.getFieldName().replace("[:]", MAP_ARRAY));
        if (field.getFieldName().matches(".*\\[:?](\\[:?])?$")) {
          tweakType(field, ARRAY_POSTFIX);
        }
        completeFieldList.add(field);
      });
    } else if (checkIfArray(recordUnion)) {
      tweakType(internalFields.get(0), ARRAY_POSTFIX);
      createArrayType(completeFieldList, internalFields, fieldName + "[]");
    } else {
      renameArrayType(completeFieldList, internalFields, fieldName + "[]");
    }
  }

  private void tweakType(FieldValueMapping internalField, String postfix) {
    internalField.setFieldType(internalField.getFieldType() + postfix);
  }

  private void createArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {
    internalFields.forEach(internalField -> {
      if (!internalField.getFieldName().contains(MAP_ARRAY) && !internalField.getFieldName().contains("[][]")) {
        internalField.setFieldName(internalField.getFieldName().replace(fieldName, fieldName + "[:]"));
      }
    });
    completeFieldList.addAll(internalFields);
  }

  private void renameArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {
    internalFields.forEach(internalField -> internalField.setFieldName(fieldName));
    completeFieldList.addAll(internalFields);
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

  private String getNotNullType(List<Schema> types) {
    Schema chosenType = extractTypeName(types.get(0)).equalsIgnoreCase("null") ? types.get(1) : types.get(0);
    chosenType = extractTypeName(types.get(1)).equalsIgnoreCase("array") ? types.get(1) : chosenType;
    String chosenTypeName = extractTypeName(chosenType);
    if (!randomObject.isTypeValid(chosenTypeName)) {
      chosenTypeName = "null";
    } else if ("array".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getElementType().getName() + ARRAY_POSTFIX;
    } else if ("map".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getValueType().getName() + MAP_POSTFIX;
    }
    return chosenTypeName;
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema schema : types) {
      if (RECORD == schema.getType() || ARRAY == schema.getType() || MAP == schema.getType() || typesSet.contains(schema.getType())) {
        isRecord = schema;
      }
    }
    return isRecord;
  }

  private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields, List<FieldValueMapping> completeFieldList) {
    internalFields.forEach(internalField -> {
      if (internalField.getFieldName().startsWith(fieldName + ".")) {
        internalField.setFieldName(fieldName + internalField.getFieldName().replace(fieldName, splitter.replace(".", "")));
      } else {
        internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      }
      completeFieldList.add(internalField);
    });
  }

  private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields) {
    internalFields.forEach(internalField -> internalField.setFieldName(fieldName + splitter + internalField.getFieldName()));
  }

  private String extractTypeName(Schema schema) {
    return schema.getType().getName();
  }

  private boolean checkIfEnumField(Schema.Type type) {
    return type.equals(ENUM);
  }
}
