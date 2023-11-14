package com.sngular.kloadgen.schemaregistry.adapter.impl;

import com.sngular.kloadgen.parsedschema.ParsedSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.Getter;
import org.apache.avro.Schema;

@Getter
public final class ConfluentAbstractParsedSchemaMetadata extends AbstractParsedSchemaAdapter {

  private String schemaType;

  private String name;

  private String canonicalString;

  private Object rawSchema;

  private ConfluentAbstractParsedSchemaMetadata(final ParsedSchema parsedSchema) {
    this.schemaType = parsedSchema.schemaType();
    this.name = parsedSchema.name();
    this.canonicalString = parsedSchema.canonicalString();
    this.rawSchema = parsedSchema.rawSchema();
  }

  private ConfluentAbstractParsedSchemaMetadata(final Schema schema) {
    this.schemaType = schema.getType().getName();
    this.name = schema.getName();
  }

  public ConfluentAbstractParsedSchemaMetadata(final ProtobufSchema schema) {
    this.schemaType = schema.schemaType();
    this.name = schema.name();
    this.rawSchema = schema.rawSchema();
    this.canonicalString = schema.canonicalString();
  }

  public ConfluentAbstractParsedSchemaMetadata() {

  }

  public static AbstractParsedSchemaAdapter parse(final ParsedSchema parsedSchema) {
    return new ConfluentAbstractParsedSchemaMetadata(parsedSchema);
  }

  public static AbstractParsedSchemaAdapter parse(final Schema schema) {
    return new ConfluentAbstractParsedSchemaMetadata(schema);
  }

  public static AbstractParsedSchemaAdapter parse(final ProtobufSchema schema) {
    return new ConfluentAbstractParsedSchemaMetadata(schema);
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
