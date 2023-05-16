package com.sngular.kloadgen.schemaregistry.adapter.impl;

public abstract class ParsedSchemaAdapter {
  public  abstract <T> T getType();

  public  abstract <T> T getRawSchema();
}
