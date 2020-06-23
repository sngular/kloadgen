package net.coru.kloadgen.processor;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class AvroSchemaProcessor {

  private Schema schema;

  private SchemaMetadata metadata;

  private List<FieldValueMapping> fieldExprMappings;

  private AvroRandomTool randomToolAvro;

  private final Set<Type> typesSet = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING,
      Type.LONG, Type.BYTES, Type.FIXED);

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
          if(fieldValueMapping.getFieldType().contains("map")) {
            entity.put(fieldName, createObjectMap(fieldValueMapping.getFieldType(),
                calculateSize(fieldName),
                fieldValueMapping.getFieldValuesList(),schema.getField(fieldValueMapping.getFieldName())));
          }
          entity.put(fieldName,
              createObjectArray(entity.getSchema().getField(fieldName).schema().getElementType(), fieldName, calculateSize(fieldName),
                  fieldExpMappingsQueue));
          fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {
          String fieldName = getCleanMethodName(fieldValueMapping, "");
          entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue));
          fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else {
          entity.put(fieldValueMapping.getFieldName(),
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

  private GenericRecord createObject(final Schema subSchema, final String fieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws KLoadGenException {
    Schema schema = subSchema;
    GenericRecord subEntity = createRecord(schema);
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    } else {
      schema = subEntity.getSchema();
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while(!fieldExpMappingsQueue.isEmpty() && fieldValueMapping.getFieldName().contains(fieldName)) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
        if (fieldValueMapping.getFieldType().contains("map")){
          fieldExpMappingsQueue.poll();
          String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createObjectMap(fieldValueMapping.getFieldType(),
              calculateSize(cleanFieldName),
              fieldValueMapping.getFieldValuesList(),schema.getField(cleanFieldName)));
        } else {
          String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
          subEntity.put(fieldNameSubEntity, createObjectArray(extractRecordSchema(subEntity.getSchema().getField(fieldNameSubEntity)),
              fieldNameSubEntity,
              calculateSize(cleanFieldName),
              fieldExpMappingsQueue));
        }
      }else if (cleanFieldName.contains(".")) {
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
            schema.getField(cleanFieldName)));
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
    }else return null;
  }

  private List<GenericRecord> createObjectArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws KLoadGenException {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for(int i=0; i<arraySize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private Object createObjectMap(String fieldType, Integer arraySize, List<String> fieldExpMappings, Field field)
      throws KLoadGenException {
    return randomToolAvro.generateRandomMap(fieldType, arraySize, fieldExpMappings, field, arraySize);
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
    } else if (typesSet.contains(schema.getType())) {
      return createRecord(schema.getElementType());
    } else{
      return null;
    }
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

  private Integer calculateSize(String fieldName) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String arrayLengthStr = StringUtils.substringBetween(fieldName, "[", "]");
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
        pathToClean.indexOf(".") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }

  private String getCleanMethodNameMap(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains("[")?
        pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }
}
