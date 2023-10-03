package com.sngular.kloadgen.schemaregistry.adapter.impl;

import lombok.Builder;

@Builder
public final class BaseSchemaMetadata<T extends SchemaMetadataAdapter> {

  private final T schemaMetadataAdapter;

  public BaseSchemaMetadata(final T schemaMetadataAdapter) {
    this.schemaMetadataAdapter = schemaMetadataAdapter;
  }

  public T getSchemaMetadataAdapter() {
    return schemaMetadataAdapter;
  }
}
