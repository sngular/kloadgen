package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import java.util.List;

import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;

public class ConfluentSchemaMetadata extends SchemaMetadata{

  private String schema;
  private List<SchemaReference> references;

  ConfluentSchemaMetadata(final String id, final String version, final String schemaType) {
    super(id, version, schemaType);
  }
}
