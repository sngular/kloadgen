package net.coru.kloadgen.input.avro;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.MAP;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.jmeter.threads.JMeterContextService;

public class SchemaExtractor {

  private Set<Type> typesSet = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING);

  public List<FieldValueMapping> flatPropertiesList(String subjectName) throws IOException, RestClientException {
    Map<String, String> originals = new HashMap<>();

    Properties properties = JMeterContextService.getContext().getProperties();
    if (Objects.nonNull(properties.getProperty(SCHEMA_REGISTRY_URL))) {
      originals.put(SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(SCHEMA_REGISTRY_URL));

      if (FLAG_YES.equals(properties.getProperty(SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE
            .equals(properties.getProperty(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BASIC_AUTH_CREDENTIALS_SOURCE,
              properties.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(USER_INFO_CONFIG, properties.getProperty(USER_INFO_CONFIG));
        } else if (SCHEMA_REGISTRY_AUTH_BEARER_KEY
            .equals(properties.getProperty(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BEARER_AUTH_CREDENTIALS_SOURCE,
              properties.getProperty(BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(BEARER_AUTH_TOKEN_CONFIG, properties.getProperty(BEARER_AUTH_TOKEN_CONFIG));
        }
      }
    }

    List<FieldValueMapping> attributeList = new ArrayList<>();
    SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(originals.get(SCHEMA_REGISTRY_URL_CONFIG), 1000, originals);

    SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(subjectName);
    Schema schema = schemaRegistryClient.getById(schemaMetadata.getId());
    schema.getFields().forEach(field -> processField(field, attributeList));
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(Field field) {
    return processFieldList(field.schema().getFields());
  }

  private List<FieldValueMapping> processFieldList(List<Field> fieldList) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for(Field innerField : fieldList) {
      processField(innerField, completeFieldList);
    }
    return completeFieldList;
  }


  private List<FieldValueMapping> extractArrayInternalFields(Field innerField) {
    return extractArrayInternalFields(innerField.name(), innerField.schema());
  }

  private List<FieldValueMapping> extractArrayInternalFields(String fieldName, Schema innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    if (RECORD == innerField.getElementType().getType()) {
      for (Field arrayElementField : innerField.getElementType().getFields()) {
        processField(arrayElementField, completeFieldList);
      }
    } else if (typesSet.contains(innerField.getElementType().getType())) {
      completeFieldList.add( new FieldValueMapping(fieldName,innerField.getElementType().getName()+"-array"));
    }
    return completeFieldList;
  }

  private void processField(Field innerField, List<FieldValueMapping> completeFieldList) {
    if (RECORD == innerField.schema().getType()) {
      processRecordFieldList(innerField.name(), ".", extractInternalFields(innerField), completeFieldList);
    } else if (Type.ARRAY == innerField.schema().getType()) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField);
      if (internalFields.size() >1) {
        processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
      } else {
        internalFields.get(0).setFieldName(innerField.name());
        completeFieldList.add(internalFields.get(0));
      }
    }else if (Type.MAP == innerField.schema().getType()) {
      System.out.println("Mapa");
    } else if (Type.UNION == innerField.schema().getType()) {
      Schema recordUnion = getRecordUnion(innerField.schema().getTypes());
      if (null != recordUnion) {
        if (recordUnion.getType() == RECORD) {
            processRecordFieldList(innerField.name(), ".", processFieldList(recordUnion.getFields()), completeFieldList);
        } else if(recordUnion.getType() == Type.ARRAY){
          List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), recordUnion);
          if (internalFields.size() >1) {
            processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
          } else {
            internalFields.get(0).setFieldName(innerField.name());
            completeFieldList.add(internalFields.get(0));
          }
        }else if(recordUnion.getType() == Type.MAP){
          System.out.println("Hola map");
          List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), recordUnion);
          if (internalFields.size() >1) {
            processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
          } else {
            internalFields.get(0).setFieldName(innerField.name());
            completeFieldList.add(internalFields.get(0));
          }
        }
      } else {
        completeFieldList.add( new FieldValueMapping(innerField.name(), getNotNullType(innerField.schema().getTypes())));
      }
    } else {
      completeFieldList.add( new FieldValueMapping(innerField.name(),innerField.schema().getType().getName()));
    }
  }

  private String getNotNullType(List<Schema> types) {
    Schema chosenType = extractTypeName(types.get(0)).equalsIgnoreCase("null") ? types.get(1) : types.get(0);
    chosenType = extractTypeName(types.get(1)).equalsIgnoreCase("array") ? types.get(1) : chosenType;
    String chosenTypeName = extractTypeName(chosenType);

    if (!RandomTool.VALID_TYPES.contains(chosenTypeName)) {
      chosenTypeName = "null";
    } else if ("array".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = "int-array";
    } else if ("map".equalsIgnoreCase(chosenTypeName)) {
      chosenTypeName = chosenType.getValueType().getName() + "-map";
    }
    return chosenTypeName;
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema schema : types) {
      if (RECORD == schema.getType() || Type.ARRAY == schema.getType() || Type.MAP == schema.getType()) {
        isRecord = schema;
      }
    }
    return isRecord;
  }

  private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields, List<FieldValueMapping> completeFieldList) {
    internalFields.forEach(internalField -> {
      internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
      completeFieldList.add(internalField);
    });
  }

  private String extractTypeName(Schema schema) {
    return schema.getType().getName();
  }
}
