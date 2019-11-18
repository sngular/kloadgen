package net.coru.kloadgen.input;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
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
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueue =  new ArrayDeque(fieldExprMappings);
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while(!fieldExpMappingsQueue.isEmpty()) {
      if (cleanUpPath(fieldValueMapping, "").contains(".")) {
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        entity.put(fieldName, createObject(entity, fieldName, fieldExpMappingsQueue));
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

  private Object createObject(GenericRecord entity, String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    Schema subSchema = entity.getSchema().getField(fieldName).schema();
    GenericRecord subEntity = new GenericData.Record(subSchema);
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    while(!fieldExpMappingsQueue.isEmpty() && fieldValueMapping.getFieldName().contains(fieldName)) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      if (cleanFieldName.contains(".")) {
        String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
        subEntity.put(fieldNameSubEntity, createObject(subEntity, fieldNameSubEntity, fieldExpMappingsQueue));
      } else {
        fieldExpMappingsQueue.poll();
        subEntity.put(cleanFieldName, RandomTool.generateRandom(fieldValueMapping.getValueExpression(), subSchema.getField(cleanFieldName)));
      }
      fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
    }
    return subEntity;
  }

  private FieldValueMapping getSafeGetElement(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  private String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName) {
    int startPosition = 0;
    if (StringUtils.isNotEmpty(fieldName)) {
      startPosition = fieldValueMapping.getFieldName().indexOf(fieldName) + fieldName.length() + 1;
    }
    return fieldValueMapping.getFieldName().substring(startPosition);
  }

  private String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains(".")?
        pathToClean.indexOf(".") : 0;
    return pathToClean.substring(0, endOfField);
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
