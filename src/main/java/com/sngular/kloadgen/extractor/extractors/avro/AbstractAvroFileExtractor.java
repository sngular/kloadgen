package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.sngular.kloadgen.extractor.extractors.SchemaExtractorUtil;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.randomtool.random.RandomObject;
import org.apache.avro.Schema;
import org.apache.commons.collections4.IteratorUtils;

public abstract class AbstractAvroFileExtractor {

  private final Set<Schema.Type> typesSet = EnumSet.of(Schema.Type.INT, Schema.Type.DOUBLE, Schema.Type.FLOAT, Schema.Type.BOOLEAN, Schema.Type.STRING, Schema.Type.LONG,
                                                       Schema.Type.BYTES, Schema.Type.FIXED);

  private final RandomObject randomObject = new RandomObject();

  protected AbstractAvroFileExtractor() {
  }

  public final List<FieldValueMapping> processSchemaDefault(final Schema schemaReceived) {
    final var attributeList = new ArrayList<FieldValueMapping>();
    Schema aux = null;
    if (checkIfUnion(schemaReceived)) {
      aux = schemaReceived.getTypes().get(schemaReceived.getTypes().size() - 1);
    } else if (checkIfRecord(schemaReceived)) {
      aux = schemaReceived;
    }
    if (aux != null && (checkIfUnion(schemaReceived) || checkIfRecord(schemaReceived))) {
      aux.getFields().forEach(field -> processField(field, attributeList, true, true));
    }
    return attributeList;
  }

  public final List<String> getSchemaNameList(final Schema schema) {
    return new ArrayList<>(extractSchemaNames(schema));
  }

  public final void processField(
      final Schema.Field innerField, final List<FieldValueMapping> completeFieldList, final boolean isAncestorRequired, final boolean isAncestor) {
    if (checkIfRecord(innerField.schema())) {
      processRecordFieldList(innerField.name(), ".", processFieldList(innerField.schema().getFields(), isAncestorRequired), completeFieldList);
    } else if (checkIfArray(innerField.schema())) {
      final var internalFields = extractArrayInternalFields(innerField.name(), innerField.schema(), isAncestorRequired);
      if (internalFields.size() == 1
          && (internalFields.get(0).getFieldName().endsWith(SchemaExtractorUtil.ARRAY_NAME_POSTFIX + SchemaExtractorUtil.MAP_NAME_POSTFIX)
              || internalFields.get(0).getFieldName().endsWith(SchemaExtractorUtil.ARRAY_NAME_POSTFIX + SchemaExtractorUtil.ARRAY_NAME_POSTFIX))) {
        tweakType(internalFields.get(0), SchemaExtractorUtil.ARRAY_TYPE_POSTFIX);
      }
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField.schema())) {
      final var internalFields = extractMapInternalFields(SchemaExtractorUtil.postFixNameMap(innerField.name()), innerField.schema().getValueType(), isAncestorRequired);
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

  private void processUnionField(final String fieldName, final List<FieldValueMapping> completeFieldList, final Schema recordUnion, final boolean isAncestorRequired) {
    if (checkIfRecord(recordUnion)) {
      processRecordFieldList(fieldName, ".", processFieldList(recordUnion.getFields(), isAncestorRequired), completeFieldList);
    } else if (checkIfArray(recordUnion)) {
      extractArray(fieldName, completeFieldList, recordUnion.getElementType(), isAncestorRequired);
    } else if (checkIfMap(recordUnion)) {
      final var internalFields = extractMapInternalFields(SchemaExtractorUtil.postFixNameMap(fieldName), recordUnion.getValueType(), isAncestorRequired);
      if (internalFields.size() == 1 && internalFields.get(0).getFieldName().endsWith(SchemaExtractorUtil.MAP_NAME_POSTFIX + SchemaExtractorUtil.MAP_NAME_POSTFIX)) {
        tweakType(internalFields.get(0), SchemaExtractorUtil.MAP_TYPE_POSTFIX);
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

  private void processRecordFieldList(
      final String fieldName, final String splitter, final List<FieldValueMapping> internalFields,
      final List<FieldValueMapping> completeFieldList) {
    internalFields.forEach(internalField -> {
      if (internalField.getFieldName().startsWith(fieldName + ".")) {
        internalField.setFieldName(fieldName + internalField.getFieldName().replace(fieldName, splitter.replace(".", "")));
      } else {
        internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      }
      completeFieldList.add(internalField);
    });
  }

  private List<FieldValueMapping> processFieldList(final List<Schema.Field> fieldList, final boolean isAncestorRequired) {
    final var completeFieldList = new ArrayList<FieldValueMapping>();
    for (Schema.Field innerField : fieldList) {
      processField(innerField, completeFieldList, isAncestorRequired, false);
    }
    return completeFieldList;
  }

  private List<FieldValueMapping> extractArrayInternalFields(final String fieldName, final Schema innerField, final boolean isAncestorRequired) {
    final var completeFieldList = new ArrayList<FieldValueMapping>();
    if (checkIfUnion(innerField)) {
      extractUnionRecord(fieldName, innerField, completeFieldList, isAncestorRequired);
    } else if (checkIfRecord(innerField)) {
      for (Schema.Field arrayElementField : innerField.getFields()) {
        processField(arrayElementField, completeFieldList, isAncestorRequired, false);
      }
      processRecordFieldList(fieldName, ".", completeFieldList, new ArrayList<>());
    } else if (checkIfMap(innerField)) {
      completeFieldList.addAll(extractMapInternalFields(SchemaExtractorUtil.postFixNameMap(fieldName), innerField.getValueType(), isAncestorRequired));
    } else if (checkIfArray(innerField)) {
      completeFieldList.addAll(extractArrayInternalFields(SchemaExtractorUtil.postFixNameArray(fieldName), innerField.getElementType(), isAncestorRequired));
    } else if (typesSet.contains(innerField.getType())) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(innerField.getName() + SchemaExtractorUtil.ARRAY_TYPE_POSTFIX)
                           .required(isAncestorRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );
    }
    return completeFieldList;
  }

  private void extractArray(final String fieldName, final List<FieldValueMapping> completeFieldList, final Schema recordUnion, final boolean isAncestorRequired) {
    final var internalFields = extractArrayInternalFields(fieldName, recordUnion, isAncestorRequired);
    if (checkIfRecord(recordUnion)) {
      processRecordFieldList(fieldName, SchemaExtractorUtil.ARRAY_NAME_POSTFIX + ".", internalFields, completeFieldList);
    } else if (checkIfMap(recordUnion)) {
      internalFields.forEach(field -> {
        field.setFieldName(field.getFieldName().replace(SchemaExtractorUtil.MAP_NAME_POSTFIX, SchemaExtractorUtil.ARRAY_NAME_POSTFIX + SchemaExtractorUtil.MAP_NAME_POSTFIX));
        if (field.getFieldName().matches(".*\\[:?](\\[:?])?$")) {
          tweakType(field, SchemaExtractorUtil.ARRAY_TYPE_POSTFIX);
        }
        completeFieldList.add(field);
      });
    } else if (checkIfArray(recordUnion)) {
      tweakType(internalFields.get(0), SchemaExtractorUtil.ARRAY_TYPE_POSTFIX);
      createArrayType(completeFieldList, internalFields, SchemaExtractorUtil.postFixNameArray(fieldName));
    } else {
      renameArrayType(completeFieldList, internalFields, SchemaExtractorUtil.postFixNameArray(fieldName));
    }
  }

  private List<FieldValueMapping> extractMapInternalFields(final String fieldName, final Schema innerField, final boolean isAncestorRequired) {
    final var completeFieldList = new ArrayList<FieldValueMapping>();
    if (checkIfUnion(innerField)) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(getNotNullType(innerField.getTypes()) + SchemaExtractorUtil.MAP_TYPE_POSTFIX)
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
      final var internalFields = extractArrayInternalFields(SchemaExtractorUtil.postFixNameArray(fieldName), innerField.getElementType(), isAncestorRequired);
      internalFields.forEach(field -> {
        if (field.getFieldType().endsWith(SchemaExtractorUtil.ARRAY_TYPE_POSTFIX)
            && field.getFieldName().endsWith(SchemaExtractorUtil.MAP_NAME_POSTFIX + SchemaExtractorUtil.ARRAY_NAME_POSTFIX)) {
          tweakType(field, SchemaExtractorUtil.MAP_TYPE_POSTFIX);
        }
      });
      completeFieldList.addAll(internalFields);
    } else if (checkIfMap(innerField) && !checkIfRecord(innerField.getValueType())) {
      final var internalFields = extractMapInternalFields(SchemaExtractorUtil.postFixNameMap(fieldName), innerField.getValueType(), isAncestorRequired);
      completeFieldList.addAll(internalFields);
    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(fieldName)
                           .fieldType(innerField.getType().getName() + SchemaExtractorUtil.MAP_TYPE_POSTFIX)
                           .required(isAncestorRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build()
      );

    }
    return completeFieldList;
  }

  private void addFieldToList(final Schema.Field innerField, final List<FieldValueMapping> completeFieldList, final boolean isAncestorRequired) {
    var typeName = innerField.schema().getType().getName();
    if (checkIfLogicalType(innerField.schema())) {
      typeName += "_" + innerField.schema().getLogicalType().getName();
    }
    if (checkIfEnum(innerField.schema())) {
      final var fieldValueList = String.join(",", innerField.schema().getEnumSymbols());
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

  private void extractUnionRecord(final String fieldName, final Schema innerField, final List<FieldValueMapping> completeFieldList, final boolean isAncestorRequired) {
    final var recordUnion = getRecordUnion(innerField.getTypes());
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
                             .fieldName(SchemaExtractorUtil.postFixNameArray(fieldName))
                             .fieldType(getNotNullType(innerField.getTypes()))
                             .isAncestorRequired(isAncestorRequired)
                             .build()
        );

      } else if (checkIfMap(recordUnion) && typesSet.contains(recordUnion.getValueType().getType())) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(SchemaExtractorUtil.postFixNameMap(fieldName))
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

  private String getNotNullType(final List<Schema> types) {
    var chosenType = extractTypeName(types.get(0)).equalsIgnoreCase("null") ? types.get(1) : types.get(0);
    chosenType = extractTypeName(types.get(1)).equalsIgnoreCase("array") ? types.get(1) : chosenType;
    String chosenTypeName = extractTypeName(chosenType);
    if (!randomObject.isTypeValid(chosenTypeName)) {
      chosenTypeName = "null";
    } else if ("array".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getElementType().getName() + SchemaExtractorUtil.ARRAY_TYPE_POSTFIX;
    } else if ("map".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getValueType().getName() + SchemaExtractorUtil.MAP_TYPE_POSTFIX;
    }
    return chosenTypeName;
  }

  private Schema getRecordUnion(final List<Schema> types) {
    Schema isRecord = null;
    for (Schema schema : types) {
      if (checkIfRecord(schema) || checkIfArray(schema) || checkIfMap(schema) || checkIfEnum(schema) || typesSet.contains(schema.getType())) {
        isRecord = schema;
      }
    }
    return isRecord;
  }

  private void createArrayType(final List<FieldValueMapping> completeFieldList, final List<FieldValueMapping> internalFields, final String fieldName) {
    internalFields.forEach(internalField -> {
      if (!internalField.getFieldName().contains(SchemaExtractorUtil.ARRAY_NAME_POSTFIX + SchemaExtractorUtil.MAP_NAME_POSTFIX)
          && !internalField.getFieldName().contains(SchemaExtractorUtil.ARRAY_NAME_POSTFIX + SchemaExtractorUtil.ARRAY_NAME_POSTFIX)) {
        internalField.setFieldName(internalField.getFieldName().replace(fieldName, SchemaExtractorUtil.postFixNameMap(fieldName)));
      }
    });
    completeFieldList.addAll(internalFields);
  }

  private void renameArrayType(final List<FieldValueMapping> completeFieldList, final List<FieldValueMapping> internalFields, final String fieldName) {
    internalFields.forEach(internalField -> internalField.setFieldName(fieldName));
    completeFieldList.addAll(internalFields);
  }

  private void tweakType(final FieldValueMapping internalField, final String postfix) {
    internalField.setFieldType(internalField.getFieldType() + postfix);
  }

  private String extractTypeName(final Schema schema) {
    return schema.getType().getName();
  }

  private boolean checkIfRecord(final Schema innerSchema) {
    return Schema.Type.RECORD.equals(innerSchema.getType());
  }

  private boolean checkIfMap(final Schema innerSchema) {
    return Schema.Type.MAP.equals(innerSchema.getType());
  }

  private boolean checkIfArray(final Schema innerSchema) {
    return Schema.Type.ARRAY.equals(innerSchema.getType());
  }

  private boolean checkIfUnion(final Schema innerSchema) {
    return Schema.Type.UNION.equals(innerSchema.getType());
  }

  private boolean checkIfEnum(final Schema type) {
    return Schema.Type.ENUM.equals(type.getType());
  }

  private boolean checkIfLogicalType(final Schema innerSchema) {
    return Objects.nonNull(innerSchema.getLogicalType());
  }

  private boolean checkIfRequiredField(final Schema innerSchema) {
    boolean result = Boolean.TRUE;
    if (checkIfUnion(innerSchema)) {
      result = !IteratorUtils.matchesAny(innerSchema.getTypes().iterator(), type -> type.getType() == Schema.Type.NULL);
    }
    return result;
  }

  private Set<String> extractSchemaNames(final Schema schema) {
    final Set<String> schemaNames = new HashSet<>();
    if (checkIfRecord(schema)) {
      schemaNames.add(schema.getName());
    } else if (checkIfArray(schema)) {
      schemaNames.addAll(extractSchemaNames(schema.getElementType()));
    } else if (checkIfUnion(schema)) {
      schema.getTypes().forEach(schemaIt -> schemaNames.addAll(extractSchemaNames(schemaIt)));
    }
    return schemaNames;
  }

}
