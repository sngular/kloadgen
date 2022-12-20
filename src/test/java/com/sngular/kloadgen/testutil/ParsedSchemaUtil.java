package com.sngular.kloadgen.testutil;

import java.util.List;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;

public final class ParsedSchemaUtil implements ParsedSchema {

  @Override
  public String schemaType() {
    return null;
  }

  @Override
  public String name() {
    return null;
  }

  @Override
  public String canonicalString() {
    return null;
  }

  @Override
  public List<SchemaReference> references() {
    return null;
  }

  @Override
  public List<String> isBackwardCompatible(final ParsedSchema parsedSchema) {
    return null;
  }

  @Override
  public Object rawSchema() {
    return null;
  }

}
