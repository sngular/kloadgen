package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

public class BaseParsedSchema <T extends ParsedSchemaAdapter>{

  private final T parsedSchemaAdapter;

  public BaseParsedSchema(final T parsedSchemaAdapter) {this.parsedSchemaAdapter = parsedSchemaAdapter;}

public T getParsedSchemaAdapter(){
    return parsedSchemaAdapter;
}
}
