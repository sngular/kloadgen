package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import java.util.List;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import lombok.Getter;

@Getter
public class ConfluentParsedSchemaMetadata extends ParsedSchemaAdapter {

  private String schemaType;

  private String name;

  private String canonicalString;

  private List<SchemaReference> references;
  private  Object rawSchema;

  private ConfluentParsedSchemaMetadata(ParsedSchema parsedSchema){
    this.schemaType = parsedSchema.schemaType();
    this.name = parsedSchema.name();
    this.canonicalString = parsedSchema.canonicalString();
    this.references = parsedSchema.references();
    this.rawSchema = parsedSchema.rawSchema();
  }

  public static ParsedSchemaAdapter parse(final ParsedSchema parsedSchema) {
    return new ConfluentParsedSchemaMetadata(parsedSchema);
  }

  @Override
  public String getType() {
    return this.getSchemaType();
  }

  @Override
  public Object getRawSchema() {
    return this.rawSchema;
  }
}
