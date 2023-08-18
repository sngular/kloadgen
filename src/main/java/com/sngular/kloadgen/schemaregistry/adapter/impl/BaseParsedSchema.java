package com.sngular.kloadgen.schemaregistry.adapter.impl;

import lombok.Getter;
import org.apache.avro.Schema.Parser;

@Getter
public class BaseParsedSchema<T extends ParsedSchemaAdapter> extends Parser {

  private final T parsedSchemaAdapter;

  public BaseParsedSchema(final T parsedSchemaAdapter) {
    this.parsedSchemaAdapter = parsedSchemaAdapter;
  }

}
