package net.coru.kloadgen.input;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class AvroSchemaProcessor implements Iterator<EnrichedRecord> {

  private SchemaRegistryClient schemaRegistryClient;
  private Schema schema;
  private SchemaMetadata metadata;
  private List<FieldValueMapping> fieldExprMappings;
  private Map<String, Object> context = new HashMap<>();

  public AvroSchemaProcessor(String schemaRegistruUrl, String avroSchemaName, List<FieldValueMapping> fieldExprMappings)
      throws IOException, RestClientException {
    schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistruUrl, 1000);
    schema = getSchemaBySubject(avroSchemaName);
    this.fieldExprMappings = fieldExprMappings;
  }

  @SneakyThrows
  @Override
  public EnrichedRecord next() {
    GenericRecord entity = new GenericData.Record(schema);
    if (!fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
      while (!fieldExpMappingsQueue.isEmpty()) {
        if (cleanUpPath(fieldValueMapping, "").contains("[")) {
          String fieldName = getCleanMethodName(fieldValueMapping, "");
          entity.put(fieldName,
              createObjectArray(entity.getSchema().getField(fieldName).schema().getElementType(), fieldName, calculateArraySize(fieldName),
                  fieldExpMappingsQueue));
          fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {
          String fieldName = getCleanMethodName(fieldValueMapping, "");
          entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue));
          fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else {
          entity.put(fieldValueMapping.getFieldName(), RandomTool
              .generateRandom(fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(),
                  schema.getField(fieldValueMapping.getFieldName()), context));
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        }
      }
    }
    return new EnrichedRecord(metadata, entity);
  }

  private GenericRecord createObject(Schema subSchema, String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws UnexpectedException {
    GenericRecord subEntity = createRecord(subSchema);
    if (null == subEntity) {
      throw new UnexpectedException("Something Odd just happened", new NullPointerException());
    } else {
      subSchema = subEntity.getSchema();
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while(!fieldExpMappingsQueue.isEmpty() && fieldValueMapping.getFieldName().contains(fieldName)) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
        String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
        subEntity.put(fieldNameSubEntity, createObjectArray(extractRecordSchema(subEntity.getSchema().getField(fieldNameSubEntity)),
            fieldNameSubEntity,
            calculateArraySize(cleanFieldName),
            fieldExpMappingsQueue));
      } else if (cleanFieldName.contains(".")) {
        String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
        subEntity.put(fieldNameSubEntity, createObject(subEntity.getSchema().getField(fieldNameSubEntity).schema(),
            fieldNameSubEntity,
            fieldExpMappingsQueue));
      } else {
        fieldExpMappingsQueue.poll();
        subEntity.put(cleanFieldName, RandomTool.generateRandom(
            fieldValueMapping.getFieldType(),
            fieldValueMapping.getValueLength(),
            fieldValueMapping.getFieldValuesList(),
            subSchema.getField(cleanFieldName), context));
      }
      fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
    }
    return subEntity;
  }

  private Schema extractRecordSchema(Field field) {
    if (ARRAY == field.schema().getType()) {
      return field.schema().getElementType();
    } else if (UNION == field.schema().getType()) {
      if (ARRAY == field.schema().getTypes().get(1).getType()) {
        return field.schema().getTypes().get(1).getElementType();
      } else {
        return field.schema().getTypes().get(1);
      }
    } else return null;
  }

  private List<GenericRecord> createObjectArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws UnexpectedException {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for(int i=0; i<arraySize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private GenericRecord createRecord(Schema schema) {
    if (RECORD == schema.getType()) {
      return new GenericData.Record(schema);
    } else if (UNION == schema.getType()) {
      return createRecord(schema.getTypes().get(1));
    } else if (ARRAY == schema.getType()) {
      return createRecord(schema.getElementType());
    } else {
      return null;
    }
  }
  private Integer calculateArraySize(String fieldName) {
    Integer arrayLength = RandomUtils.nextInt(1,10);
    String arrayLengthStr = StringUtils.substringBetween(fieldName,"[", "]");
    if (StringUtils.isNotEmpty(arrayLengthStr) && StringUtils.isNumeric(arrayLengthStr)) {
      arrayLength = Integer.valueOf(arrayLengthStr);
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
    if (cleanPath.matches("^(\\d*\\]).*$")) {
      cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
    }
    return cleanPath;
  }

  private String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains(".")?
        pathToClean.indexOf(".") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*\\]", "");
  }

  private Schema getSchemaBySubject(String avroSubjectName) throws IOException, RestClientException {
    metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSubjectName);
    return schemaRegistryClient.getById(metadata.getId());
  }

  @Override
  public boolean hasNext() {
    return true;
  }
}
