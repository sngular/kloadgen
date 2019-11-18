package net.coru.kloadgen.input.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

public class SchemaExtractor {

  private SchemaRegistryClient schemaRegistryClient;

  public List<FieldValueMapping> flatPropertiesList(String schemaUrl, String subjectName) throws IOException, RestClientException {
    List<FieldValueMapping> attributeList = new ArrayList<>();
    schemaRegistryClient = new CachedSchemaRegistryClient(schemaUrl, 1000);

    SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(subjectName);
    Schema schema = schemaRegistryClient.getById(schemaMetadata.getId());
    schema.getFields().forEach(field -> {
      if (Type.RECORD == field.schema().getType()) {
        List<FieldValueMapping> internalFields = extractInternalFields(field);
        internalFields.forEach(internalField -> {
          internalField.setFieldName(field.name() + "." + internalField.getFieldName());
          attributeList.add(internalField);
        });
      } else {
        FieldValueMapping expressionMapping = new FieldValueMapping();
        expressionMapping.setFieldName(field.name());
        expressionMapping.setValueExpression(field.schema().getType().getName());
        attributeList.add(expressionMapping);
      }
    });
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(Schema.Field field) {
    List<Schema.Field> fieldList = field.schema().getFields();
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for(Schema.Field innerField : fieldList) {
      if (Type.RECORD == innerField.schema().getType()) {
        List<FieldValueMapping> internalFields = extractInternalFields(innerField);
        internalFields.forEach(internalField -> {
          internalField.setFieldName(innerField.name() + "." + internalField.getFieldName());
          completeFieldList.add(internalField);
        });
      } else {
        FieldValueMapping internalField = new FieldValueMapping();
        internalField.setFieldName(innerField.name());
        internalField.setValueExpression(innerField.schema().getType().getName());
        completeFieldList.add(internalField);
      }
    }
    return completeFieldList;
  }

}
