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

import java.util.*;

public final class AvroSchemaProcessor {

  public static final String WRONG_CONFIGURATION_MAP_ARRAY = "Wrong configuration Map - Array";

  private final Set<Type> typesSet = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT,
                                                Type.BOOLEAN, Type.STRING, Type.LONG, Type.BYTES, Type.FIXED);

  private Schema schema;

  private SchemaMetadata metadata;

  private List<FieldValueMapping> fieldExprMappings;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private AvroGeneratorTool avroGeneratorTool;

  public void processSchema(final ParsedSchema schema, final SchemaMetadata metadata,
      final List<FieldValueMapping> fieldExprMappings) {
    this.schema = (Schema) schema.rawSchema();
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomObject = new RandomObject();
    randomMap = new RandomMap();
    avroGeneratorTool = new AvroGeneratorTool();

  }

  public void processSchema(final Schema schema, final SchemaMetadata metadata,
      final List<FieldValueMapping> fieldExprMappings) {
    this.schema = schema;
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomObject = new RandomObject();
    randomMap = new RandomMap();
    avroGeneratorTool = new AvroGeneratorTool();

  }

  public EnrichedRecord next() {
    final GenericRecord entity = new GenericData.Record(schema);
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;

      while (!fieldExpMappingsQueue.isEmpty()) {
        int level = 0;
        final String cleanPath = SchemaProcessorLib.cleanUpPath(fieldValueMapping, level);
        final String fieldName = SchemaProcessorLib.getCleanMethodName(fieldValueMapping, level);
        final String typeFilter = cleanPath.replaceAll(fieldName, "");

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek()
                                                                                   .getFieldName().contains(fieldName))
            && generatedProperties == elapsedProperties && generatedProperties > 0
            && Boolean.TRUE.equals(Objects.requireNonNull(fieldValueMapping).getAncestorRequired())) {
          fieldValueMapping.setRequired(true);
          final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          fieldExpMappingsQueueCopy.poll();
        } else {
          generatedProperties = 0;
          elapsedProperties = 0;
          fieldExpMappingsQueueCopy.poll();
        }
        generatedProperties++;

        if (Boolean.TRUE.equals(isOptionalField(schema.getField(fieldName))
                                && !Objects.requireNonNull(fieldValueMapping).getRequired())
            && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        } else {

          if (typeFilter.matches("\\[?..*]\\[.*") && !Objects.requireNonNull(fieldValueMapping)
                                                             .getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array")
              && !typeFilter.startsWith(".")) {
            if (SchemaProcessorLib.checkIfArrayMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, entity, fieldName);
            } else if (SchemaProcessorLib.checkIfMapArray(fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (SchemaProcessorLib.checkIfIsRecordMapArray(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, entity, fieldName, level);
            } else if (SchemaProcessorLib.checkIfIsRecordArrayMap(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, entity, fieldName, level);
            } else {
              throw new KLoadGenException(WRONG_CONFIGURATION_MAP_ARRAY);
            }
          } else if (typeFilter.startsWith("[")) {
            if (SchemaProcessorLib.checkIfMap(typeFilter, Objects.requireNonNull(fieldValueMapping).getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, entity, fieldName);
            } else if (SchemaProcessorLib.checkIfArray(typeFilter, fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (SchemaProcessorLib.checkIfRecordArray(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, entity, fieldName, level);
            } else if (SchemaProcessorLib.checkIfRecordMap(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, entity, fieldName, level);
            } else {
              throw new KLoadGenException(WRONG_CONFIGURATION_MAP_ARRAY);
            }
          } else if (typeFilter.startsWith(".")) {
            entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue, level));
            fieldValueMapping = SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
          } else {
            entity.put(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                       avroGeneratorTool.generateObject(
                           entity.getSchema().getField(fieldName),
                           fieldValueMapping,
                           extractConstraints(schema.getField(fieldValueMapping.getFieldName()))
                       )
            );
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
          }
        }
      }
    }
    return EnrichedRecord.builder().schemaMetadata(metadata).genericRecord(entity).build();
  }

  private Map<ConstraintTypeEnum, String> extractConstraints(final Schema.Field field) {
    final Map<ConstraintTypeEnum, String> constraints = new EnumMap<>(ConstraintTypeEnum.class);

    if (Objects.nonNull(field.schema().getObjectProp("precision"))) {
      constraints.put(ConstraintTypeEnum.PRECISION, field.schema().getObjectProp("precision").toString());
    }

    if (Objects.nonNull(field.schema().getObjectProp("scale"))) {
      constraints.put(ConstraintTypeEnum.SCALE, field.schema().getObjectProp("scale").toString());
    }

    return constraints;
  }

  private FieldValueMapping processFieldValueMappingAsRecordArray(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue,
      final GenericRecord entity, final String fieldName, final int level) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(),
                                                               SchemaProcessorLib.getCleanMethodName(fieldValueMapping, level));

    entity.put(fieldName, createObjectArray(extractType(entity.getSchema().getField(fieldName),
                                                        Type.ARRAY).getElementType(),
                                            fieldName,
                                            arraySize,
                                            fieldExpMappingsQueue, level));
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsRecordMap(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue,
      final GenericRecord entity, final String fieldName, final int level) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), SchemaProcessorLib.getCleanMethodName(fieldValueMapping, level));

    entity.put(fieldName, createObjectMap(extractType(entity.getSchema().getField(fieldName), Type.MAP).getValueType(),
                                          fieldName,
                                          mapSize,
                                          fieldExpMappingsQueue, level));
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleArray(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final GenericRecord entity, final String fieldName) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    entity.put(fieldName,
               SchemaProcessorLib.createArray(fieldName, arraySize, fieldExpMappingsQueue));
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleMap(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final GenericRecord entity, final String fieldName) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    // Add condition that checks (][)
    entity.put(fieldName, SchemaProcessorLib.createSimpleTypeMap(fieldName, fieldValueMapping.getFieldType(),
                                                                 SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName),
                                              fieldValueMapping.getValueLength(),
                                              fieldValueMapping.getFieldValuesList()));
    return fieldExpMappingsQueue.peek();
  }

  private FieldValueMapping processFieldValueMappingAsSimpleArrayMap(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final GenericRecord entity,
      final String fieldName) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    final var simpleTypeArrayMap = SchemaProcessorLib.createSimpleTypeArrayMap(fieldName, fieldValueMapping.getFieldType(), arraySize, mapSize, fieldValueMapping.getValueLength(),
                                                      fieldValueMapping.getFieldValuesList());
    entity.put(fieldName, simpleTypeArrayMap);
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleMapArray(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final GenericRecord entity,
      final String fieldName) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    final var mapArray = randomMap.generateMap(fieldValueMapping.getFieldType(), mapSize, fieldValueMapping.getFieldValuesList(), fieldValueMapping.getValueLength(), arraySize,
                                         fieldValueMapping.getConstraints());

    entity.put(fieldName, mapArray);
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsRecordArrayMap(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final GenericRecord entity,
      final String fieldName, final int level) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    final Map<String, List<GenericRecord>> recordMapArray = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      final ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                         createObjectArray(extractType(entity.getSchema().getField(fieldName), Type.MAP).getValueType().getElementType(), fieldName, arraySize, temporalQueue, level));
    }
    recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                       createObjectArray(extractType(entity.getSchema().getField(fieldName), Type.MAP).getValueType().getElementType(), fieldName, arraySize,
                                         fieldExpMappingsQueue, level));
    entity.put(fieldName, recordMapArray);
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsRecordMapArray(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final GenericRecord entity,
      final String fieldName, final int level) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    final var recordArrayMap = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      final ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordArrayMap.add(createObjectMap(extractType(entity.getSchema().getField(fieldName), Type.ARRAY).getElementType(), fieldName, mapSize, temporalQueue, level));
    }
    recordArrayMap.add(createObjectMap(extractType(entity.getSchema().getField(fieldName), Type.ARRAY).getElementType(), fieldName, arraySize, fieldExpMappingsQueue, level));
    entity.put(fieldName, recordArrayMap);
    return SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
  }

  private Schema extractType(final Field field, final Type typeToMatch) {
    Schema realField = field.schema();
    if (Type.UNION.equals(field.schema().getType())) {
      realField = IteratorUtils.find(field.schema().getTypes().iterator(), type -> typeToMatch.equals(type.getType()));
    }
    return realField;
  }

  private Boolean isOptionalField(final Field field) {
    boolean result = false;
    if (Type.UNION.equals(field.schema().getType())) {
      result = IteratorUtils.matchesAny(field.schema().getTypes().iterator(), type -> type.getType() == Type.NULL);
    }
    return result;
  }

  private GenericRecord createObject(final Schema subSchema, final String rootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int rootLevel) {
    Schema innerSchema = subSchema;
    if (subSchema.getType().equals(Type.MAP)) {
      innerSchema = subSchema.getValueType();
    } else if (subSchema.getType().equals(Type.ARRAY)) {
      innerSchema = innerSchema.getElementType();
    }
    final GenericRecord subEntity = createRecord(innerSchema);
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    } else {
      subEntity.getSchema();
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;
    int level = rootLevel + 1;

    while (!fieldExpMappingsQueue.isEmpty()
           && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + rootFieldName + "$")
               || fieldValueMapping.getFieldName().matches(rootFieldName + "\\..*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\[.*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\..*"))) {
      final String cleanPath = SchemaProcessorLib.cleanUpPath(fieldValueMapping, level);
      final String fieldNameSubEntity = SchemaProcessorLib.getCleanMethodName(fieldValueMapping, level);
      final String typeFilter = cleanPath.replaceAll(fieldNameSubEntity, "");

      generatedProperties++;

      if ((subSchema.getType().equals(Type.RECORD) && isOptionalField(subSchema.getField(fieldNameSubEntity))
           || subSchema.getType().equals(Type.ARRAY) && isOptionalField(subSchema.getField(fieldNameSubEntity))
           || subSchema.getType().equals(Type.MAP) && isOptionalField(subSchema.getValueType().getField(fieldNameSubEntity)))
          && fieldValueMapping.getFieldValuesList().contains("null")) {

        elapsedProperties++;
        final FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        final FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (Boolean.TRUE.equals((fieldExpMappingsQueue.peek() == null || !Objects.requireNonNull(nextField).getFieldName().contains(rootFieldName))
                                && Objects.requireNonNull(actualField).getAncestorRequired())
            && generatedProperties == elapsedProperties && generatedProperties > 0) {

          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          if (fieldExpMappingsQueue.peek() == null) {
            fieldExpMappingsQueue.add(fieldValueMapping);
          }
        } else {
          fieldValueMapping = nextField;
        }
      } else {
        if (typeFilter.matches("\\[?..]\\[.*") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array")
            && !typeFilter.startsWith(".")) {
          if (SchemaProcessorLib.checkIfMapArray(fieldValueMapping.getFieldType())) {
            processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else if (SchemaProcessorLib.checkIfArrayMap(fieldValueMapping.getFieldType())) {
            final String mapFieldNameSubEntity = SchemaProcessorLib.getMapCleanMethodName(fieldValueMapping, level);
            processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, subEntity, mapFieldNameSubEntity);
          } else if (SchemaProcessorLib.checkIfIsRecordMapArray(cleanPath)) {
            processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity, level);
          } else if (SchemaProcessorLib.checkIfIsRecordArrayMap(cleanPath)) {
            processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, subEntity, fieldNameSubEntity, level);
          }
        } else if (typeFilter.startsWith("[")) {
          if (SchemaProcessorLib.checkIfMap(typeFilter, fieldValueMapping.getFieldType())) {
            final String mapFieldNameSubEntity = SchemaProcessorLib.getMapCleanMethodName(fieldValueMapping, level);
            processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, subEntity, mapFieldNameSubEntity);
          } else if (SchemaProcessorLib.checkIfArray(typeFilter, fieldValueMapping.getFieldType())) {
            processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity);
          } else if (SchemaProcessorLib.checkIfRecordMap(cleanPath)) {
            processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, subEntity, fieldNameSubEntity, level);
          } else if (SchemaProcessorLib.checkIfRecordArray(cleanPath)) {
            processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, subEntity, fieldNameSubEntity, level);
          } else {
            throw new KLoadGenException(WRONG_CONFIGURATION_MAP_ARRAY);
          }
        } else if (typeFilter.startsWith(".")) {
          subEntity.put(fieldNameSubEntity, createObject(subEntity.getSchema().getField(fieldNameSubEntity).schema(),
                                                         fieldNameSubEntity,
                                                         fieldExpMappingsQueue, level));
        } else {
          fieldExpMappingsQueue.poll();
          subEntity.put(fieldNameSubEntity, avroGeneratorTool.generateObject(
                            subEntity.getSchema().getField(fieldNameSubEntity),
                            fieldValueMapping,
                            extractConstraints(subEntity.getSchema().getField(fieldNameSubEntity))
                        )
          );
        }
        fieldValueMapping = SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return subEntity;
  }

  private GenericRecord createRecord(final Schema schema) {
    if (Type.RECORD == schema.getType()) {
      return new GenericData.Record(schema);
    } else if (Type.UNION == schema.getType()) {
      return createRecord(getRecordUnion(schema.getTypes()));
    } else if (Type.ARRAY == schema.getType()) {
      return createRecord(schema.getElementType());
    } else if (Type.MAP == schema.getType()) {
      return createRecord(schema.getElementType());
    } else {
      return null;

    }
  }

  private Schema getRecordUnion(final List<Schema> types) {
    Schema isRecord = null;
    for (Schema innerSchema : types) {
      if (Type.RECORD == innerSchema.getType() || Type.ARRAY == innerSchema.getType() || Type.MAP == innerSchema.getType() || typesSet.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return isRecord;
  }

  private List<GenericRecord> createObjectArray(final Schema subSchema, final String fieldName, final Integer arraySize,
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int level) {
    final List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      final ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue, level));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue, level));
    return objectArray;
  }

  private Map<String, GenericRecord> createObjectMap(final Schema subSchema, final String fieldName, final Integer mapSize,
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int level) {
    final Map<String, GenericRecord> objectMap = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      final ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectMap.put(SchemaProcessorLib.generateMapKey(), createObject(subSchema, fieldName, temporalQueue, level));
    }
    objectMap.put(SchemaProcessorLib.generateMapKey(), createObject(subSchema, fieldName, fieldExpMappingsQueue, level));
    return objectMap;
  }

}
