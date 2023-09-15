package com.sngular.kloadgen.schemaregistry.adapter.impl;

import java.util.List;

public interface SchemaMetadataAdapter {

  <T> T getId();

  <T> T getGlobalId();

  <T> T getType();

  <T> T getVersion();

  <T> List<T> getReferences();
}
