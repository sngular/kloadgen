package com.sngular.kloadgen.schemaregistry.adapter.impl;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.Getter;
import org.apache.avro.Schema;

@Getter
public class ConfluentParsedSchemaMetadata extends ParsedSchemaAdapter {

  private String schemaType;

  private String name;

  private String canonicalString;

  private Object rawSchema;

  private ConfluentParsedSchemaMetadata(final ParsedSchema parsedSchema) {
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

  public ConfluentParsedSchemaMetadata() {

  }

  public static ParsedSchemaAdapter parse(final ParsedSchema parsedSchema) {
    return new ConfluentParsedSchemaMetadata(parsedSchema);
  }

  public static ParsedSchemaAdapter parse(final Schema schema) {
    return new ConfluentParsedSchemaMetadata(schema);
  }

  public static ParsedSchemaAdapter parse(final ProtobufSchema schema) {
    return new ConfluentParsedSchemaMetadata(schema);
  }

  @Override
  public final String getType() {
    return this.schemaType;
  }

  @Override
  public final Object getRawSchema() {
    return this.rawSchema;
  }
}
