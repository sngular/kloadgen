/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import static net.coru.kloadgen.processor.SchemaProcessorLib.calculateSize;
import static net.coru.kloadgen.processor.SchemaProcessorLib.checkIfArrayMap;
import static net.coru.kloadgen.processor.SchemaProcessorLib.checkIfMap;
import static net.coru.kloadgen.processor.SchemaProcessorLib.checkIfMapArray;
import static net.coru.kloadgen.processor.SchemaProcessorLib.cleanUpPath;
import static net.coru.kloadgen.processor.SchemaProcessorLib.generateRandomList;
import static net.coru.kloadgen.processor.SchemaProcessorLib.generateRandomMap;
import static net.coru.kloadgen.processor.SchemaProcessorLib.getCleanMethodName;
import static net.coru.kloadgen.processor.SchemaProcessorLib.getCleanMethodNameMap;
import static net.coru.kloadgen.processor.SchemaProcessorLib.getSafeGetElement;
import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.AvroRandomTool;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public class AvroSchemaProcessor {

  private Schema schema;

  private SchemaMetadata metadata;

  private List<FieldValueMapping> fieldExprMappings;

  private AvroRandomTool randomToolAvro;

  private final Set<Type> typesSet = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING,
      Type.LONG, Type.BYTES, Type.FIXED);

  public void processSchema(ParsedSchema schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
    this.schema = (Schema) schema.rawSchema();
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomToolAvro = new AvroRandomTool();
  }

  public void processSchema(Schema schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
    this.schema = schema;
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomToolAvro = new AvroRandomTool();
  }

  public EnrichedRecord next() {
    GenericRecord entity = new GenericData.Record(schema);
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
      while (!fieldExpMappingsQueue.isEmpty()) {
        String cleanPath = cleanUpPath(fieldValueMapping, "");
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        if (cleanPath.contains("][")) {
          if (checkIfArrayMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
            fieldValueMapping = processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, entity, fieldName);
          } else if (checkIfMapArray(fieldValueMapping.getFieldType())) {
            fieldValueMapping = processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, entity, fieldName);
          } else {
            throw new KLoadGenException("Wrong configuration Map - Array");
          }
        } else if (cleanPath.contains("[")) {
          if (checkIfMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
            fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, entity, fieldName);
          } else if (checkIfMapArray(fieldValueMapping.getFieldType())) {
            fieldValueMapping = processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, entity, fieldName);
          } else {
            throw new KLoadGenException("Wrong configuration Map - Array");
          }
        } else if (cleanPath.contains(".")) {
          entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue));
          fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else {
          entity.put(Objects.requireNonNull(fieldValueMapping).getFieldName(),
              randomToolAvro.generateRandom(fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(),
                  fieldValueMapping.getFieldValuesList(),
                  schema.getField(fieldValueMapping.getFieldName())));
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        }
      }
    }
    return new EnrichedRecord(metadata, entity);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    entity.put(fieldName, createSimpleTypeMap(fieldName, fieldValueMapping.getFieldType(),
                                              calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                              fieldValueMapping.getFieldValuesList()));
    return fieldExpMappingsQueue.peek();
  }

  private FieldValueMapping processFieldValueMappingAsSimpleMapArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    entity.put(fieldName,
               createArray(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(),
                           fieldName,
                           calculateSize(fieldValueMapping.getFieldName(), fieldName),
                           fieldExpMappingsQueue));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleArrayMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    entity.put(fieldName,
               createArray(extractType(entity.getSchema().getField(fieldName), MAP).getElementType(),
                           fieldName,
                           calculateSize(fieldValueMapping.getFieldName(), fieldName),
                           fieldExpMappingsQueue));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  private FieldValueMapping processFieldValueMappingAsSimpleArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, GenericRecord entity, String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    entity.put(fieldName,
               createSimpleTypeMapArray(fieldName, fieldValueMapping.getFieldType(),
                                        calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                        fieldValueMapping.getValueLength(),
                                        fieldValueMapping.getFieldValuesList()));
    return fieldExpMappingsQueue.peek();
  }

  private Schema extractType(Field field, Type typeToMatch) {
    Schema realField = field.schema();
    if (UNION.equals(field.schema().getType())){
      realField = IteratorUtils.find(field.schema().getTypes().iterator(), type -> typeToMatch.equals(type.getType()));
    }
    return realField;
  }

  private GenericRecord createObject(final Schema subSchema, final String fieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    Schema innerSchema = subSchema;
    GenericRecord subEntity = createRecord(innerSchema);
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    } else {
      innerSchema = subEntity.getSchema();
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while (!fieldExpMappingsQueue.isEmpty()
            && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + fieldName + "$")
            || fieldValueMapping.getFieldName().matches(fieldName + "\\..*")
            || fieldValueMapping.getFieldName().matches(".*" + fieldName + "\\[.*")
            || fieldValueMapping.getFieldName().matches(".*" + fieldName + "\\..*"))) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
        if (checkIfMap(fieldValueMapping.getFieldType())){
          fieldExpMappingsQueue.poll();
          String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createSimpleTypeMap(fieldName, fieldValueMapping.getFieldType(),
                                                                calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                                fieldValueMapping.getFieldValuesList()));
        } else if (checkIfMapArray(fieldValueMapping.getFieldType())){
          fieldExpMappingsQueue.poll();
          String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createSimpleTypeMapArray(fieldName, fieldValueMapping.getFieldType(),
                                                                     calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                                     fieldValueMapping.getValueLength(),
                                                                     fieldValueMapping.getFieldValuesList()));
        } else if (checkIfArrayMap(fieldValueMapping.getFieldType())){
          fieldExpMappingsQueue.poll();
          String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createSimpleTypeArrayMap(fieldName, fieldValueMapping.getFieldType(),
                                                                     calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                                     fieldValueMapping.getValueLength(),
                                                                     fieldValueMapping.getFieldValuesList()));
        } else {
          String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createArray(extractRecordSchema(subEntity.getSchema().getField(fieldNameSubEntity)),
              fieldNameSubEntity,
              calculateSize(fieldValueMapping.getFieldName(), fieldNameSubEntity),
              fieldExpMappingsQueue));
        }
      } else if (cleanFieldName.contains(".")) {
        String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
        subEntity.put(fieldNameSubEntity, createObject(subEntity.getSchema().getField(fieldNameSubEntity).schema(),
            fieldNameSubEntity,
            fieldExpMappingsQueue));
      } else {
        fieldExpMappingsQueue.poll();
        subEntity.put(cleanFieldName, randomToolAvro.generateRandom(
            fieldValueMapping.getFieldType(),
            fieldValueMapping.getValueLength(),
            fieldValueMapping.getFieldValuesList(),
            innerSchema.getField(cleanFieldName)));
      }
      fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
    }
    return subEntity;
  }

  private Schema extractRecordSchema(Field field) {
    if (ARRAY == field.schema().getType()) {
      return field.schema().getElementType();
    } else if (MAP == field.schema().getType()) {
      return field.schema().getElementType();
    } else if (UNION == field.schema().getType()) {
      return getRecordUnion(field.schema().getTypes());
    } else if (typesSet.contains(field.schema().getType())){
      return getRecordUnion(field.schema().getTypes());
    } else return null;
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


  private Object createArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    if (ARRAY.equals(subSchema.getType())) {
      if (typesSet.contains(subSchema.getElementType().getType())) {
        return createArray(fieldName,arraySize, fieldExpMappingsQueue);
      } else {
        return createObjectArray(subSchema.getElementType(), fieldName, arraySize, fieldExpMappingsQueue);
      }
    } else if (typesSet.contains(subSchema.getType())) {
      return createArray(fieldName, arraySize, fieldExpMappingsQueue);
    } else {
      return createObjectArray(subSchema, fieldName, arraySize, fieldExpMappingsQueue);
    }
  }

  private List<GenericRecord> createObjectArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for(int i=0; i<arraySize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private Object createArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    return generateRandomList(fieldName, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList());
  }

  private Object createSimpleTypeMap(String fieldName, String fieldType, Integer mapSize, List<String> fieldExpMappings) {
    return generateRandomMap(fieldName, fieldType, mapSize, fieldExpMappings);
  }

  private Object createSimpleTypeMapArray(String fieldName, String fieldType, Integer arraySize, Integer mapSize, List<String> fieldExpMappings) {
    return generateRandomMap(fieldName, fieldType, mapSize, fieldExpMappings);
  }

  private List<Map> createSimpleTypeArrayMap(String fieldName, String fieldType, Integer arraySize, Integer mapSize, List<String> fieldExpMappings) {
    var result = new ArrayList<Map>();
    while (arraySize > 0) {
      result.add(generateRandomMap(fieldName, fieldType, mapSize, fieldExpMappings));
      arraySize--;
    }
    return result;
  }

}
