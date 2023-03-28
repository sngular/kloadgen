package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

public abstract class ParsedSchemaAdapter {
  public  abstract <T> T getType();

  public  abstract <T> T getRawSchema();
}
