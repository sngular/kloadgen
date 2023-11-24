package com.sngular.kloadgen.schemaregistry.adapter.impl;

import com.sngular.kloadgen.parsedschema.IParsedSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.Getter;
import org.apache.avro.Schema;

@Getter
public final class ConfluentParsedSchemaMetadata extends AbstractParsedSchemaAdapter {

  private final String schemaType;

  private final String name;

  private String canonicalString;

  private Object rawSchema;

  private ConfluentParsedSchemaMetadata(final IParsedSchema parsedSchema) {
    this.schemaType = parsedSchema.schemaType();
    this.name = parsedSchema.name();
    this.canonicalString = parsedSchema.canonicalString();
    this.rawSchema = parsedSchema.rawSchema();
  }

  private ConfluentParsedSchemaMetadata(final Schema schema) {
    this.schemaType = schema.getType().getName();
    this.name = schema.getName();
  }

  public ConfluentParsedSchemaMetadata(final ProtobufSchema schema) {
    this.schemaType = schema.schemaType();
    this.name = schema.name();
    this.rawSchema = schema.rawSchema();
    this.canonicalString = schema.canonicalString();
  }

  public static AbstractParsedSchemaAdapter parse(final IParsedSchema parsedSchema) {
    return new ConfluentParsedSchemaMetadata(parsedSchema);
  }

  public static AbstractParsedSchemaAdapter parse(final Schema schema) {
    return new ConfluentParsedSchemaMetadata(schema);
  }

  public static AbstractParsedSchemaAdapter parse(final ProtobufSchema schema) {
    return new ConfluentParsedSchemaMetadata(schema);
  }

  @Override
  public String getType() {
    return this.schemaType;
  }

  @Override
  public Object getRawSchema() {
    return this.rawSchema;
  }
}
