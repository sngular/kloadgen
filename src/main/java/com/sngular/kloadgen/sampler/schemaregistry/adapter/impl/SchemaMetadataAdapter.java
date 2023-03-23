package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

public abstract class SchemaMetadataAdapter {

  public abstract <T> T getId();

  public abstract <T> T getGlobalId();

  public abstract <T> T getType();

  public abstract <T> T getVersion();
}
