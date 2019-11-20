package net.coru.kloadgen.input.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

public class SchemaExtractor {

  private SchemaRegistryClient schemaRegistryClient;

  public List<FieldValueMapping> flatPropertiesList(String schemaUrl, String subjectName) throws IOException, RestClientException {
    List<FieldValueMapping> attributeList = new ArrayList<>();
    schemaRegistryClient = new CachedSchemaRegistryClient(schemaUrl, 1000);

    SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(subjectName);
    Schema schema = schemaRegistryClient.getById(schemaMetadata.getId());
    schema.getFields().forEach(field -> processField(field, attributeList));
    return attributeList;
  }

  private List<FieldValueMapping> extractInternalFields(Field field) {
    List<Field> fieldList = field.schema().getFields();
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for(Field innerField : fieldList) {
      processField(innerField, completeFieldList);
    }
    return completeFieldList;
  }


  private List<FieldValueMapping> extractArrayInternalFields(Field innerField) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    for (Field arrayElementField : innerField.schema().getElementType().getFields()) {
      processField(arrayElementField, completeFieldList);
    }
    return completeFieldList;
  }

  private void processField(Field innerField, List<FieldValueMapping> completeFieldList) {
    if (Type.RECORD == innerField.schema().getType()) {
      List<FieldValueMapping> internalFields = extractInternalFields(innerField);
      internalFields.forEach(internalField -> {
        internalField.setFieldName(innerField.name() + "." + internalField.getFieldName());
        completeFieldList.add(internalField);
      });
    } else if (Type.ARRAY == innerField.schema().getType()) {
      List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField);
      internalFields.forEach(internalField -> {
        internalField.setFieldName(innerField.name() + "[]." + internalField.getFieldName());
        completeFieldList.add(internalField);
      });
    } else if (Type.UNION == innerField.schema().getType()) {
      FieldValueMapping internalField = new FieldValueMapping();
      internalField.setFieldName(innerField.name());
      internalField.setValueExpression(getNotNullType(innerField.schema().getTypes()));
      completeFieldList.add(internalField);
    } else {
      FieldValueMapping internalField = new FieldValueMapping();
      internalField.setFieldName(innerField.name());
      internalField.setValueExpression(innerField.schema().getType().getName());
      completeFieldList.add(internalField);
    }
  }

  private String getNotNullType(List<Schema> types) {
    String choosenType = types.get(0).getName().equalsIgnoreCase("null") ? types.get(1).getName() : types.get(0).getName();
    if (!RandomTool.VALID_TYPES.contains(choosenType)) {
      choosenType = "null";
    }
    return choosenType;
  }


}
