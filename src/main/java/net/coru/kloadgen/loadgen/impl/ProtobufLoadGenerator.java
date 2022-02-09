package net.coru.kloadgen.loadgen.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.ProtobufSchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
public class ProtobufLoadGenerator extends AbstractLoadGenerator implements BaseLoadGenerator {

  private final ProtobufSchemaProcessor protobufSchemaProcessor;

  public ProtobufLoadGenerator() {
    protobufSchemaProcessor = new ProtobufSchemaProcessor();
  }

  @Override
  public void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings) {
    try {
      var schema = retrieveSchema(originals, avroSchemaName);
      this.protobufSchemaProcessor.processSchema(schema.getRight(), schema.getLeft(), fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings) {
    try {
      ProtobufSchema protobufSchema = new ProtobufSchema(schema);
      this.protobufSchemaProcessor.processSchema(protobufSchema, new SchemaMetadata(1, 1, "PROTO", Collections.emptyList(), schema), fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public EnrichedRecord nextMessage() {

      return protobufSchemaProcessor.next();
  }

}
