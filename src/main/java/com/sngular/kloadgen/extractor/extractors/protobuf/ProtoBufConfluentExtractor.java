package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtoBufConfluentExtractor extends AbstractProtoFileExtractor implements Extractor<AbstractParsedSchema<ProtoFileElement>> {

  public final List<FieldValueMapping> processSchema(final AbstractParsedSchema<ProtoFileElement> schemaReceived) {
    return processSchemaDefault(schemaReceived.getRawSchema());
  }

  public final List<String> getSchemaNameList(final String schema) {
    return List.of(new ProtobufSchema(schema).name());
  }

}