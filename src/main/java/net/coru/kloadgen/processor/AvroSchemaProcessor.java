/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import static org.apache .avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
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
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

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

  @SneakyThrows
  public EnrichedRecord next() {
    GenericRecord entity = new GenericData.Record(schema);
    if (!fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
      while (!fieldExpMappingsQueue.isEmpty()) {
        if (cleanUpPath(fieldValueMapping, "").contains("[")) {
          String fieldName = getCleanMethodName(fieldValueMapping, "");
          if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map")) {
            fieldExpMappingsQueue.remove();
            entity.put(fieldName, createSimpleTypeMap(fieldValueMapping.getFieldType(),
                calculateSize(fieldValueMapping.getFieldName(), fieldName),
                fieldValueMapping.getFieldValuesList()));
            fieldValueMapping = fieldExpMappingsQueue.peek();
          } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
            fieldExpMappingsQueue.remove();
            entity.put(fieldName, createSimpleTypeMapArray(fieldValueMapping.getFieldType(),
                calculateSize(fieldValueMapping.getFieldName(), fieldName),
                fieldValueMapping.getValueLength(),
                fieldValueMapping.getFieldValuesList()));
            fieldValueMapping = fieldExpMappingsQueue.peek();
          } else {
            entity.put(fieldName,
                createArray(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(),
                    fieldName,
                    calculateSize(fieldValueMapping.getFieldName(), fieldName),
                    fieldExpMappingsQueue));
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
          }
        } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {
          String fieldName = getCleanMethodName(fieldValueMapping, "");
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
    while(!fieldExpMappingsQueue.isEmpty() && Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName)) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
        if (fieldValueMapping.getFieldType().endsWith("map")){
          fieldExpMappingsQueue.poll();
          String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createSimpleTypeMap(fieldValueMapping.getFieldType(),
              calculateSize(fieldValueMapping.getFieldName(), fieldName),
              fieldValueMapping.getFieldValuesList()));
        } else if (fieldValueMapping.getFieldType().endsWith("map-array")){
          fieldExpMappingsQueue.poll();
          String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createSimpleTypeMapArray(fieldValueMapping.getFieldType(),
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

  private Object createArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    if (typesSet.contains(subSchema.getType())) {
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
      return randomToolAvro.generateRandomList(fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(), arraySize);
    } else {
      return createObjectArray(subSchema, fieldName, arraySize, fieldExpMappingsQueue);
    }
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

  private List<GenericRecord> createObjectArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for(int i=0; i<arraySize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private Object createSimpleTypeMap(String fieldType, Integer arraySize, List<String> fieldExpMappings) {
    return randomToolAvro.generateRandomMap(fieldType, arraySize, fieldExpMappings, arraySize);
  }

  private Object createSimpleTypeMapArray(String fieldType, Integer arraySize, Integer mapSize, List<String> fieldExpMappings) {
    return randomToolAvro.generateRandomMap(fieldType, mapSize, fieldExpMappings, arraySize);
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

  private Integer calculateSize(String fieldName, String methodName) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String tempString = fieldName.substring(
        fieldName.lastIndexOf(methodName));
    String arrayLengthStr = StringUtils.substringBetween(tempString, "[", "]");
    if (StringUtils.isNotEmpty(arrayLengthStr) && StringUtils.isNumeric(arrayLengthStr)) {
      arrayLength = Integer.parseInt(arrayLengthStr);
    }
    return arrayLength;
  }

  private FieldValueMapping getSafeGetElement(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  private String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName) {
    int startPosition = 0;
    String cleanPath;
    if (StringUtils.isNotEmpty(fieldName)) {
      startPosition = fieldValueMapping.getFieldName().indexOf(fieldName) + fieldName.length() + 1;
    }
    cleanPath = fieldValueMapping.getFieldName().substring(startPosition);
    if (cleanPath.matches("^(\\d*]).*$")) {
      cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
    }
    return cleanPath;
  }

  private String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains(".")?
        pathToClean.indexOf(".") : pathToClean.contains("[") ? pathToClean.indexOf("[") : pathToClean.length();
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }

  private String getCleanMethodNameMap(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains("[")?
        pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }
}
