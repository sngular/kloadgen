package com.sngular.kloadgen.testutil;

import java.io.File;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;

public final class SchemaParseUtil {

  private SchemaParseUtil() {
  }

  public static ParsedSchema getParsedSchema(final String schema, final String type) {
    return new JsonSchema(schema);
  }

  public static ParsedSchema getParsedSchema(final File schema, final String type) {
    return new JsonSchema(schema);
  }

}
