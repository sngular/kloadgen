package net.coru.kloadgen.input;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class AvroSchemaProcessor implements Iterator {

  private SchemaRegistryClient schemaRegistryClient;
  private Schema schema;
  private List<FieldValueMapping> fieldExprMappings;

  public AvroSchemaProcessor(String schemaRegistruUrl, String avroSchemaName, List<FieldValueMapping> fieldExprMappings)
      throws IOException, RestClientException {
    schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistruUrl, 1000);
    schema = getSchemaBySubject(avroSchemaName);
    this.fieldExprMappings = fieldExprMappings;
  }

  @Override
  public Object next() {
    GenericRecord entity = new GenericData.Record(schema);
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueue =  new ArrayDeque<>(fieldExprMappings);
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while(!fieldExpMappingsQueue.isEmpty()) {
      if (cleanUpPath(fieldValueMapping, "").contains("[")) {
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        entity.put(fieldName, createObjectArray(entity.getSchema().getField(fieldName).schema().getElementType(),
            fieldName,
            calculateArraySize(fieldName),
            fieldExpMappingsQueue));
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue));
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      } else {
        entity.put(fieldValueMapping.getFieldName(),
            RandomTool.generateRandom(fieldValueMapping.getValueExpression(),
                schema.getField(fieldValueMapping.getFieldName())));
        fieldExpMappingsQueue.remove();
        fieldValueMapping = fieldExpMappingsQueue.peek();
      }
    }
    return entity;
  }

  private GenericRecord createObject(Schema subSchema, String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    GenericRecord subEntity = new GenericData.Record(subSchema);
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while(!fieldExpMappingsQueue.isEmpty() && fieldValueMapping.getFieldName().contains(fieldName)) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      if (cleanFieldName.contains("[")) {
        String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
        subEntity.put(fieldNameSubEntity, createObjectArray(subEntity.getSchema().getField(fieldNameSubEntity).schema().getElementType(),
            fieldNameSubEntity,
            calculateArraySize(fieldName),
            fieldExpMappingsQueue));
      } else if (cleanFieldName.contains(".")) {
        String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
        subEntity.put(fieldNameSubEntity, createObject(subEntity.getSchema().getField(fieldNameSubEntity).schema(),
            fieldNameSubEntity,
            fieldExpMappingsQueue));
      } else {
        fieldExpMappingsQueue.poll();
        subEntity.put(cleanFieldName, RandomTool.generateRandom(fieldValueMapping.getValueExpression(), subSchema.getField(cleanFieldName)));
      }
      fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
    }
    return subEntity;
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

  private Integer calculateArraySize(String fieldName) {
    Integer arrayLength = RandomUtils.nextInt(1,10);
    String arrayLengthStr = StringUtils.substringBetween(fieldName,"[", "]");
    if (StringUtils.isEmpty(arrayLengthStr) && StringUtils.isNumeric(arrayLengthStr)) {
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
    SchemaMetadata metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSubjectName);
    return schemaRegistryClient.getById(metadata.getId());
  }

  @Override
  public boolean hasNext() {
    return true;
  }
}
