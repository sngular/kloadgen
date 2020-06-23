package net.coru.kloadgen.processor;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
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
import org.apache.jmeter.threads.JMeterContextService;

public class AvroSchemaProcessor implements Iterator<EnrichedRecord> {

  private final SchemaRegistryClient schemaRegistryClient;

  private final Schema schema;

  private SchemaMetadata metadata;

  private final List<FieldValueMapping> fieldExprMappings;

  private final AvroRandomTool randomToolAvro;

  private final Set<Type> typesSet = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING,
      Type.LONG, Type.BYTES, Type.FIXED);

  public AvroSchemaProcessor(String avroSchemaName, List<FieldValueMapping> fieldExprMappings)
      throws IOException, RestClientException, KLoadGenException {
    Map<String, String> originals = new HashMap<>();
    Properties ctxProperties = JMeterContextService.getContext().getProperties();

    if (Objects.nonNull(ctxProperties.getProperty(SCHEMA_REGISTRY_URL))) {
      originals.put(SCHEMA_REGISTRY_URL_CONFIG, ctxProperties.getProperty(SCHEMA_REGISTRY_URL));

      if (FLAG_YES.equals(ctxProperties.getProperty(SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE
            .equals(ctxProperties.getProperty(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BASIC_AUTH_CREDENTIALS_SOURCE,
              ctxProperties.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(USER_INFO_CONFIG, ctxProperties.getProperty(USER_INFO_CONFIG));
        } else {
          originals.put(BEARER_AUTH_CREDENTIALS_SOURCE,
              ctxProperties.getProperty(BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(BEARER_AUTH_TOKEN_CONFIG, ctxProperties.getProperty(BEARER_AUTH_TOKEN_CONFIG));
        }
      }
      schemaRegistryClient = new CachedSchemaRegistryClient(originals.get(SCHEMA_REGISTRY_URL_CONFIG), 1000, originals);
    } else {
      throw new KLoadGenException("No Schema Registry URL in System");
    }
    schema = getSchemaBySubject(avroSchemaName);
    randomToolAvro = new AvroRandomTool();
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

  private Schema getSchemaBySubject(String avroSubjectName) throws IOException, RestClientException {
    metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSubjectName);
    return schemaRegistryClient.getById(metadata.getId());
  }

  @Override
  public boolean hasNext() {
    return true;
  }
}
