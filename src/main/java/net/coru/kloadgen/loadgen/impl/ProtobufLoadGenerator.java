package net.coru.kloadgen.loadgen.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
public class ProtobufLoadGenerator implements SRLoadGenerator, BaseLoadGenerator {

  private final SchemaProcessor protobufSchemaProcessor;

  public ProtobufLoadGenerator() {
    protobufSchemaProcessor = new SchemaProcessor();
  }

  @Override
  public final void setUpGenerator(
      final Map<String, String> originals, final String avroSchemaName,
      final List<FieldValueMapping> fieldExprMappings) {
    try {
      final var schema = retrieveSchema(originals, avroSchemaName);
      this.protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schema.getRight(), schema.getLeft(), fieldExprMappings);
    } catch (IOException | RestClientException exc) {
      log.error("Please make sure that properties data type and expression function return type are"
                + " compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public final void setUpGenerator(final String schema, final List<FieldValueMapping> fieldExprMappings) {
    final ProtobufSchema protobufSchema = new ProtobufSchema(schema);
    this.protobufSchemaProcessor
        .processSchema(SchemaTypeEnum.PROTOBUF, protobufSchema,
                       new SchemaMetadata(1, 1, "PROTOBUF", Collections.emptyList(), schema), fieldExprMappings);
  }

  @Override
  public final EnrichedRecord nextMessage() {

    return (EnrichedRecord) protobufSchemaProcessor.next();
  }

}
