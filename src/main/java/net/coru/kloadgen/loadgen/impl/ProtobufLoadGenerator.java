package net.coru.kloadgen.loadgen.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
public class ProtobufLoadGenerator extends AbstractLoadGenerator implements BaseLoadGenerator {

  private final SchemaProcessor protobufSchemaProcessor;

  public ProtobufLoadGenerator() {
    protobufSchemaProcessor = new SchemaProcessor();
  }

  @Override
  public void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings) {
    try {
      var schema = retrieveSchema(originals, avroSchemaName);
      this.protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schema.getRight(), schema.getLeft(), fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings) {
    try {
      ProtobufSchema protobufSchema = new ProtobufSchema(schema);
      this.protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, protobufSchema, new SchemaMetadata(1, 1, "PROTOBUF", Collections.emptyList(), schema), fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public EnrichedRecord nextMessage() {

    return (EnrichedRecord) protobufSchemaProcessor.next();
  }

}
