package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import lombok.Builder;

@Builder
public class BaseSchemaMetadata<T extends SchemaMetadataAdapter> {

  private final T schemaMetadataAdapter;

  public BaseSchemaMetadata(T schemaMetadataAdapter) {
    this.schemaMetadataAdapter = schemaMetadataAdapter;
  }

  public T getSchemaMetadataAdapter() {
    return schemaMetadataAdapter;
  }
}
