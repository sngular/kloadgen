package com.sngular.kloadgen.schemaregistry.adapter.impl;

import org.apache.avro.Schema.Parser;

public class BaseParsedSchema<T extends AbstractParsedSchemaAdapter> extends Parser {

  private final T parsedSchemaAdapter;

  public BaseParsedSchema(final T parsedSchemaAdapter) {
    this.parsedSchemaAdapter = parsedSchemaAdapter;
  }

  public final T getParsedSchemaAdapter() {
    return parsedSchemaAdapter;
  }
}
