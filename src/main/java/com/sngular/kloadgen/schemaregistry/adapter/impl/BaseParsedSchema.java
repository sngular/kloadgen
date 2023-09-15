package com.sngular.kloadgen.schemaregistry.adapter.impl;

import org.apache.avro.Schema.Parser;

public class BaseParsedSchema<T extends ParsedSchemaAdapter> extends Parser {

  private final T parsedSchemaAdapter;

  public BaseParsedSchema(final T parsedSchemaAdapter) {
    this.parsedSchemaAdapter = parsedSchemaAdapter;
  }

  public T getParsedSchemaAdapter(){
    return parsedSchemaAdapter;
  }
}
