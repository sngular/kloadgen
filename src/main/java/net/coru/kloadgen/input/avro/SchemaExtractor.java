package net.coru.kloadgen.input.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.jmeter.threads.JMeterContextService;

import java.io.IOException;
import java.util.*;

import static net.coru.kloadgen.util.SchemaRegistryKeys.*;
import static org.apache.avro.Schema.Type.RECORD;

public class SchemaExtractor {

  private Set<Type> typesSet = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING);

  public List<FieldValueMapping> flatPropertiesList(String subjectName) throws IOException, RestClientException {
    Map<String, String> originals = new HashMap<>();

    String schemaUrl = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_URL);

    String username = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_USERNAME_KEY);
    String password = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_PASSWORD_KEY);

    if (!SCHEMA_REGISTRY_USERNAME_DEFAULT.equalsIgnoreCase(username)) {
      originals.put("basic.auth.credentials.source", "USER_INFO");
      originals.put("schema.registry.basic.auth.user.info", username + ":" + password);
    }

    List<FieldValueMapping> attributeList = new ArrayList<>();
    SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(schemaUrl, 1000, originals);

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
    } else if (Type.UNION == innerField.schema().getType()) {
      Schema recordUnion = getRecordUnion(innerField.schema().getTypes());
      if (null != recordUnion) {
        if (recordUnion.getType() == RECORD) {
            processRecordFieldList(innerField.name(), ".", processFieldList(recordUnion.getFields()), completeFieldList);
        } else {
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
      if (RECORD == schema.getType() || Type.ARRAY == schema.getType()) {
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
