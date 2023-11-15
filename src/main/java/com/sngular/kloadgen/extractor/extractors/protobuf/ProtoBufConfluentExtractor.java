package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtoBufConfluentExtractor extends AbstractProtoFileExtractor implements Extractor<ParsedSchema> {

  public final List<FieldValueMapping> processSchema(final ParsedSchema schemaReceived) {
    return processSchemaDefault((ProtoFileElement) schemaReceived.rawSchema());
  }

  public final List<String> getSchemaNameList(final String schema) {
    return List.of(new ProtobufSchema(schema).name());
  }

}