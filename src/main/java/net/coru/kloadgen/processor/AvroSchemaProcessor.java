/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.apache.avro.Schema.Type.*;

public class AvroSchemaProcessor extends SchemaProcessorLib {

  private final Set<Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private Schema schema;

  private SchemaMetadata metadata;

  private List<FieldValueMapping> fieldExprMappings;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private AvroGeneratorTool avroGeneratorTool;

  public void processSchema(ParsedSchema schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
    this.schema = (Schema) schema.rawSchema();
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomObject = new RandomObject();
    randomMap = new RandomMap();
    avroGeneratorTool = new AvroGeneratorTool();

  }

  public void processSchema(Schema schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
    this.schema = schema;
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomObject = new RandomObject();
    randomMap = new RandomMap();
    avroGeneratorTool = new AvroGeneratorTool();

  }

  public EnrichedRecord next() {
    GenericRecord entity = new GenericData.Record(schema);
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;

      while (!fieldExpMappingsQueue.isEmpty()) {
        String cleanPath = cleanUpPath(fieldValueMapping, "");
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        String typeFilter = cleanPath.replaceAll(fieldName, "");

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
            && (generatedProperties == elapsedProperties && generatedProperties > 0) && fieldValueMapping.getParentRequired()) {
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
        } else {
          generatedProperties = 0;
          elapsedProperties = 0;
        }
        fieldExpMappingsQueueCopy.poll();
        generatedProperties++;

        if (isOptionalField(schema.getField(fieldName)) && !fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        } else {

          if (typeFilter.matches("\\[?..*]\\[.*") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array") &&
              !typeFilter.startsWith(".")) {
            if (checkIfArrayMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfMapArray(fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfIsRecordMapArray(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfIsRecordArrayMap(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, entity, fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith("[")) {
            if (checkIfMap(typeFilter, fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfArray(typeFilter, fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfRecordArray(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfRecordMap(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, entity, fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith(".")) {
            entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue));
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
          } else {
            entity.put(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                       avroGeneratorTool.generateObject(
                           entity.getSchema().getField(fieldName),
                           fieldValueMapping.getFieldType(),
                           fieldValueMapping.getValueLength(),
                           fieldValueMapping.getFieldValuesList(),
                           extractConstraints(schema.getField(fieldValueMapping.getFieldName()))
                       )
            );
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
          }
        }
      }
    }
    return new EnrichedRecord(metadata, entity);
  }

  private Map<ConstraintTypeEnum, String> extractConstraints(Schema.Field field) {
    Map<ConstraintTypeEnum, String> constrains = new HashMap<>();

    if (Objects.nonNull(field.schema().getObjectProp("precision"))) {
      constrains.put(ConstraintTypeEnum.PRECISION, field.schema().getObjectProp("precision").toString());
    }

    if (Objects.nonNull(field.schema().getObjectProp("scale"))) {
      constrains.put(ConstraintTypeEnum.SCALE, field.schema().getObjectProp("scale").toString());
    }

    return constrains;
  }

  private FieldValueMapping processFieldValueMappingAsRecordArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));

    entity.put(fieldName, createObjectArray(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(),
                                            fieldName,
                                            arraySize,
                                            fieldExpMappingsQueue));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsRecordMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));

    entity.put(fieldName, createObjectMap(extractType(entity.getSchema().getField(fieldName), MAP).getValueType(),
                                          fieldName,
                                          mapSize,
                                          fieldExpMappingsQueue));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    entity.put(fieldName,
               createArray(fieldName, arraySize, fieldExpMappingsQueue));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    // Add condition that checks (][)
    entity.put(fieldName, createSimpleTypeMap(fieldName, fieldValueMapping.getFieldType(),
                                              calculateMapSize(fieldValueMapping.getFieldName(), fieldName),
                                              fieldValueMapping.getValueLength(),
                                              fieldValueMapping.getFieldValuesList()));
    return fieldExpMappingsQueue.peek();
  }

  private FieldValueMapping processFieldValueMappingAsSimpleArrayMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    var simpleTypeArrayMap = createSimpleTypeArrayMap(fieldName, fieldValueMapping.getFieldType(), arraySize, mapSize, fieldValueMapping.getValueLength(),
                                                      fieldValueMapping.getFieldValuesList());
    entity.put(fieldName, simpleTypeArrayMap);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleMapArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    var mapArray = randomMap.generateMap(fieldValueMapping.getFieldType(), mapSize, fieldValueMapping.getFieldValuesList(), fieldValueMapping.getValueLength(), arraySize,
                                         fieldValueMapping.getConstrains());

    entity.put(fieldName, mapArray);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsRecordArrayMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    Map<String, List> recordMapArray = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                         createObjectArray(extractType(entity.getSchema().getField(fieldName), MAP).getValueType().getElementType(), fieldName, arraySize, temporalQueue));
    }
    recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                       createObjectArray(extractType(entity.getSchema().getField(fieldName), MAP).getValueType().getElementType(), fieldName, arraySize, fieldExpMappingsQueue));
    entity.put(fieldName, recordMapArray);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsRecordMapArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    var recordArrayMap = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordArrayMap.add(createObjectMap(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(), fieldName, mapSize, temporalQueue));
    }
    recordArrayMap.add(createObjectMap(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(), fieldName, arraySize, fieldExpMappingsQueue));
    entity.put(fieldName, recordArrayMap);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private Schema extractType(Field field, Type typeToMatch) {
    Schema realField = field.schema();
    if (UNION.equals(field.schema().getType())) {
      realField = IteratorUtils.find(field.schema().getTypes().iterator(), type -> typeToMatch.equals(type.getType()));
    }
    return realField;
  }

  private Boolean isOptionalField(Field field) {
    if (UNION.equals(field.schema().getType())) {
      return IteratorUtils.matchesAny(field.schema().getTypes().iterator(), type -> type.getType() == NULL);
    }
    return false;
  }

  private GenericRecord createObject(final Schema subSchema, final String rootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    Schema innerSchema = subSchema;
    if (subSchema.getType().equals(MAP)) {
      innerSchema = subSchema.getValueType();
    } else if (subSchema.getType().equals(ARRAY)) {
      innerSchema = innerSchema.getElementType();
    }
    GenericRecord subEntity = createRecord(innerSchema);
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    } else {
      subEntity.getSchema();
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while (!fieldExpMappingsQueue.isEmpty()
           && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + rootFieldName + "$")
               || fieldValueMapping.getFieldName().matches(rootFieldName + "\\..*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\[.*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\..*"))) {
      String cleanPath = cleanUpPath(fieldValueMapping, rootFieldName);
      String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, rootFieldName);
      String typeFilter = cleanPath.replaceAll(fieldNameSubEntity, "");

      generatedProperties++;

      if (((subSchema.getType().equals(RECORD) && isOptionalField(subSchema.getField(fieldNameSubEntity))) ||
           (subSchema.getType().equals(ARRAY) && isOptionalField(subSchema.getField(fieldNameSubEntity))) ||
           (subSchema.getType().equals(MAP) && isOptionalField(subSchema.getValueType().getField(fieldNameSubEntity))))
          && fieldValueMapping.getFieldValuesList().contains("null")) {

        elapsedProperties++;
        FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (((fieldExpMappingsQueue.peek() != null && !Objects.requireNonNull(nextField).getFieldName().contains(rootFieldName))
             || fieldExpMappingsQueue.peek() == null)
            && actualField.getParentRequired()
            && (generatedProperties == elapsedProperties && generatedProperties > 0)) {

          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          if (fieldExpMappingsQueue.peek() == null) {
            fieldExpMappingsQueue.add(fieldValueMapping);
          }
        } else {
          fieldValueMapping = nextField;
        }
      } else {

        if (typeFilter.matches("\\[?..]\\[.*") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array")) {
          if (checkIfMapArray(fieldValueMapping.getFieldType())) {
            processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else if (checkIfArrayMap(fieldValueMapping.getFieldType())) {
            String mapFieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, rootFieldName);
            processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, subEntity, mapFieldNameSubEntity);
          } else if (checkIfIsRecordMapArray(cleanPath)) {
            processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else if (checkIfIsRecordArrayMap(cleanPath)) {
            processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          }
        } else if (typeFilter.startsWith("[")) {
          if (checkIfMap(typeFilter, fieldValueMapping.getFieldType())) {
            String mapFieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, rootFieldName);
            processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, subEntity, mapFieldNameSubEntity);
          } else if (checkIfArray(typeFilter, fieldValueMapping.getFieldType())) {
            processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else if (checkIfRecordMap(cleanPath)) {
            processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else if (checkIfRecordArray(cleanPath)) {
            processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else {
            throw new KLoadGenException("Wrong configuration Map - Array");
          }
        } else if (typeFilter.startsWith(".")) {
          subEntity.put(fieldNameSubEntity, createObject(subEntity.getSchema().getField(fieldNameSubEntity).schema(),
                                                         fieldNameSubEntity,
                                                         fieldExpMappingsQueue));
        } else {
          fieldExpMappingsQueue.poll();
          subEntity.put(fieldNameSubEntity, avroGeneratorTool.generateObject(
                            subEntity.getSchema().getField(fieldNameSubEntity),
                            fieldValueMapping.getFieldType(),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            extractConstraints(subEntity.getSchema().getField(fieldNameSubEntity))
                        )
          );
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return subEntity;
  }

  private Schema extractRecordSchema(Field field) {
    if (ARRAY == field.schema().getType()) {
      return field.schema().getElementType();
    } else if (MAP == field.schema().getType()) {
      return field.schema().getValueType();
    } else if (UNION == field.schema().getType()) {
      return getRecordUnion(field.schema().getTypes());
    } else if (typesSet.contains(field.schema().getType())) {
      return getRecordUnion(field.schema().getTypes());
    } else {
      return null;
    }
  }

  private GenericRecord createRecord(Schema schema) {
    if (RECORD == schema.getType()) {
      return new GenericData.Record(schema);
    } else if (UNION == schema.getType()) {
      return createRecord(getRecordUnion(schema.getTypes()));
    } else if (ARRAY == schema.getType()) {
      return createRecord(schema.getElementType());
    } else if (MAP == schema.getType()) {
      return createRecord(schema.getElementType());
    } else {
      return null;
    }
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema innerSchema : types) {
      if (RECORD == innerSchema.getType() || ARRAY == innerSchema.getType() || MAP == innerSchema.getType() || typesSet.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return isRecord;
  }

  private Object createArray(Schema subSchema, String fieldName, Integer arraySize, Integer fieldValueLength, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    if (ARRAY.equals(subSchema.getType())) {
      if (typesSet.contains(subSchema.getElementType().getType())) {
        return createArray(fieldName, arraySize, fieldExpMappingsQueue);
      } else if (MAP.equals(subSchema.getElementType().getType())) {
        fieldExpMappingsQueue.remove();
        return createSimpleTypeMap(fieldName, subSchema.getElementType().getValueType().getType().getName(), arraySize, fieldValueLength, Collections.emptyList());
      } else {
        return createObjectArray(subSchema.getElementType(), fieldName, arraySize, fieldExpMappingsQueue);
      }
    } else if (MAP.equals(subSchema.getType())) {
      if (ARRAY.equals(subSchema.getValueType().getType())) {
        return createArray(fieldName, arraySize, fieldExpMappingsQueue);
      } else {
        return createObjectArray(subSchema, fieldName, arraySize, fieldExpMappingsQueue);
      }
    } else if (typesSet.contains(subSchema.getType())) {
      return createArray(fieldName, arraySize, fieldExpMappingsQueue);
    } else {
      return createObjectArray(subSchema, fieldName, arraySize, fieldExpMappingsQueue);
    }
  }

  private List<GenericRecord> createObjectArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private Map<String, GenericRecord> createObjectMap(Schema subSchema, String fieldName, Integer mapSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    Map<String, GenericRecord> objectMap = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectMap.put(generateMapKey(), createObject(subSchema, fieldName, temporalQueue));
    }
    objectMap.put(generateMapKey(), createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectMap;
  }

}
