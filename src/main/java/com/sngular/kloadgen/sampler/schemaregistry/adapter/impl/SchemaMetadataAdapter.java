package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import java.util.List;

public abstract class SchemaMetadataAdapter {

  public abstract <T> T getId();

  public abstract <T> T getGlobalId();

  public abstract <T> T getType();

  public abstract <T> T getVersion();

  public abstract <T> List<T> getReferences();
}
