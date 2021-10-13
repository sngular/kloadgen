package net.coru.kloadgen.strategy;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;

public class RecordSubjectNameStrategy implements io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy {

  @Deprecated
  public String getSubjectName(String topic, boolean isKey, Object value) {
    return subjectName(topic, isKey, new AvroSchema(AvroSchemaUtils.getSchema(value)));
  }

  @Override
  public String subjectName(String topic, boolean isKey, ParsedSchema parsedSchema) {
    if (parsedSchema == null) {
      return null;
    }
    return getRecordName(parsedSchema, isKey);
  }

  protected String getRecordName(ParsedSchema schema, boolean isKey) {
    String name = ((AvroSchema) schema).rawSchema().getObjectProps().get("subject").toString();
    if (name != null) {
      return name;
    }

    // isKey is only used to produce more helpful error messages
    if (isKey) {
      throw new SerializationException("In configuration "
          + AbstractKafkaSchemaSerDeConfig.KEY_SUBJECT_NAME_STRATEGY + " = "
          + getClass().getName() + ", the message key must only be a record schema");
    } else {
      throw new SerializationException("In configuration "
          + AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY + " = "
          + getClass().getName() + ", the message value must only be a record schema");
    }
  }

  @Override
  public void configure(Map<String, ?> configs) {

  }

}
