package net.coru.kloadgen.extractor.extractors;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;

public class AvroExtractor {

  public static final String ARRAY_POSTFIX = "-array";

  public static final String MAP_POSTFIX = "-map";

  private final Set<Schema.Type> typesSet = EnumSet.of(Schema.Type.INT, Schema.Type.DOUBLE, Schema.Type.FLOAT, Schema.Type.BOOLEAN, Schema.Type.STRING,
                                                       Schema.Type.LONG, Schema.Type.BYTES, Schema.Type.FIXED);

  public List<FieldValueMapping> processSchema(Schema schema) {
    List<FieldValueMapping> attributeList = new ArrayList<>();

    schema.getFields().forEach(field -> processField(field, attributeList));
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(Schema.Field field) {
    return processFieldList(field.schema().getFields());
  }

  private List<FieldValueMapping> processFieldList(List<Schema.Field> fieldList) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for(Schema.Field innerField : fieldList) {
      processField(innerField, completeFieldList);
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractArrayInternalFields(Schema.Field innerField) {
    return extractArrayInternalFields(innerField.name(), innerField.schema());
  }

  private List<FieldValueMapping> extractArrayInternalFields(String fieldName, Schema innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (RECORD.equals(innerField.getElementType().getType())) {
      for (Schema.Field arrayElementField : innerField.getElementType().getFields()) {
        processField(arrayElementField, completeFieldList);
      }
    } else if (MAP.equals(innerField.getElementType().getType())) {
      completeFieldList.addAll(extractMapInternalFields(fieldName, innerField.getElementType()));
    } else if (typesSet.contains(innerField.getElementType().getType())) {
      completeFieldList.add( new FieldValueMapping(fieldName + "[]",innerField.getElementType().getName()+ ARRAY_POSTFIX));
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractMapInternalFields(String fieldName, Schema innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (UNION.equals(innerField.getValueType().getType())) {
      completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getValueType().getTypes()) + MAP_POSTFIX));
    } else {
      completeFieldList.add(new FieldValueMapping(fieldName, innerField.getValueType().getName() + MAP_POSTFIX));
    }
    return completeFieldList;
  }

  public void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
    if (RECORD.equals(innerField.schema().getType())) {
      processRecordFieldList(innerField.name(), ".", extractInternalFields(innerField), completeFieldList);
    } else if (ARRAY.equals(innerField.schema().getType())) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField);
      if (checkIfRecord(innerField.schema().getElementType())) {
        processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
      } else if (checkIfMap(innerField.schema().getElementType())) {
        internalFields.get(0).setFieldType(internalFields.get(0).getFieldType().concat(ARRAY_POSTFIX));
        createArrayType(completeFieldList, internalFields, innerField.name() + "[]");
      } else {
        createArrayType(completeFieldList, internalFields, innerField.name() + "[]");
      }
    } else if (UNION.equals(innerField.schema().getType())) {
      extractUnionRecord(innerField, completeFieldList);
    } else {
      completeFieldList.add(new FieldValueMapping(innerField.name(),innerField.schema().getType().getName()));
    }
  }

  private void extractUnionRecord(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
    Schema recordUnion = getRecordUnion(innerField.schema().getTypes());
    if (null != recordUnion) {
      if (RECORD.equals(recordUnion.getType())) {
        processRecordFieldList(innerField.name(), ".", processFieldList(recordUnion.getFields()), completeFieldList);
      } else if (ARRAY.equals(recordUnion.getType())) {
        extractArray(innerField, completeFieldList, recordUnion);
      } else if (MAP.equals(recordUnion.getType())){
        List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name(), recordUnion);
        if (checkIfRecord(recordUnion.getValueType())) {
          processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
        } else {
          createArrayType(completeFieldList, internalFields, innerField.name()+"[]");
        }
      } else {
        completeFieldList.add(new FieldValueMapping(innerField.name(), recordUnion.getType().getName()));
      }
    } else {
      completeFieldList.add(new FieldValueMapping(innerField.name(), getNotNullType(innerField.schema().getTypes())));
    }
  }

  private void extractArray(Schema.Field innerField, List<FieldValueMapping> completeFieldList, Schema recordUnion) {
    List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), recordUnion);
    if (checkIfRecord(recordUnion.getElementType())) {
      processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
    } else if (checkIfMap(recordUnion.getElementType())) {
      internalFields.get(0).setFieldType(internalFields.get(0).getFieldType().concat(ARRAY_POSTFIX));
      createArrayType(completeFieldList, internalFields, innerField.name() + "[]");
    } else {
      createArrayType(completeFieldList, internalFields, innerField.name() + "[]");
    }
  }

  private void createArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {
    internalFields.get(0).setFieldName(fieldName);
    completeFieldList.add(internalFields.get(0));
  }

  private boolean checkIfRecord(Schema innerSchema) {
    return RECORD.equals(innerSchema.getType());
  }

  private boolean checkIfMap(Schema innerSchema) {
    return MAP.equals(innerSchema.getType());
  }

  private String getNotNullType(List<Schema> types) {
    Schema chosenType = extractTypeName(types.get(0)).equalsIgnoreCase("null") ? types.get(1) : types.get(0);
    chosenType = extractTypeName(types.get(1)).equalsIgnoreCase("array") ? types.get(1) : chosenType;
    String chosenTypeName = extractTypeName(chosenType);

    if (! RandomTool.VALID_TYPES.contains(chosenTypeName)) {
      chosenTypeName = "null";
    } else if ("array".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = "int-array";
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
      internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      completeFieldList.add(internalField);
    });
  }

  private String extractTypeName(Schema schema) {
    return schema.getType().getName();
  }
}
