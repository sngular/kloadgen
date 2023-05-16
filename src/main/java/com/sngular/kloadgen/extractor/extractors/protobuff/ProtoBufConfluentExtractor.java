package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.squareup.wire.schema.internal.parser.TypeElement;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtoBufConfluentExtractor extends AbstractProtoFileExtractor implements Extractor<ProtobufSchema> {

  public final List<FieldValueMapping> processSchema(final ProtobufSchema schemaReceived) {
    final var schema = schemaReceived.rawSchema();
    final List<FieldValueMapping> attributeList = new ArrayList<>();
    final HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    schema.getTypes().forEach(field -> processField(field, attributeList, schema.getImports(), true, nestedTypes));
    return attributeList;
  }

  public final List<String> getSchemaNameList(final String schema) {
    return List.of(new ProtobufSchema(schema).name());
  }

}