package com.sngular.kloadgen.parsedschema;

import com.squareup.wire.schema.internal.parser.ProtoFileElement;

public class ProtobufParsedSchema extends AbstractParsedSchema<ProtoFileElement> {

  public ProtobufParsedSchema(final String name, final ProtoFileElement schema) {
    super(name, "PROTOBUF", schema);
  }

  @Override
  public final ProtoFileElement getRawSchema() {
    return getSchema();
  }
}
