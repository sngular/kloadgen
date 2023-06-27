package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtoBufConfluentExtractor extends AbstractProtoFileExtractor implements Extractor<ProtobufSchema> {

  public final List<FieldValueMapping> processSchema(final ProtobufSchema schemaReceived) {
    return  processSchemaDefault(schemaReceived.rawSchema());
  }

  public final List<String> getSchemaNameList(final String schema) {
    return List.of(new ProtobufSchema(schema).name());
  }

}