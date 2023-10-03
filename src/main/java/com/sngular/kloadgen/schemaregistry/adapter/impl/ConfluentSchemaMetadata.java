package com.sngular.kloadgen.schemaregistry.adapter.impl;

import java.util.List;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import lombok.Getter;

@Getter
public class ConfluentSchemaMetadata implements SchemaMetadataAdapter {

  private final Integer id;

  private final Integer version;

  private final String schemaType;

  private final String schema;

  private final List<SchemaReference> references;

  private ConfluentSchemaMetadata(final SchemaMetadata schemaMetadata) {
    this.id = schemaMetadata.getId();
    this.version = schemaMetadata.getVersion();
    this.schemaType = schemaMetadata.getSchemaType();
    this.schema = schemaMetadata.getSchema();
    this.references = schemaMetadata.getReferences();
  }

  public static ConfluentSchemaMetadata parse(final SchemaMetadata schemaMetadata) {
    return new ConfluentSchemaMetadata(schemaMetadata);
  }

  @Override
  public final Integer getGlobalId() {
    return this.getId();
  }

  @Override
  public final String getType() {
    return this.schemaType;
  }
}
