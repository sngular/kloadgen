package com.sngular.kloadgen.schemaregistry.adapter.impl;

import java.util.List;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import lombok.Getter;

@Getter
public class                                                                                                                                                                                                                             ConfluentSchemaMetadata implements SchemaMetadataAdapter {

  private Integer id;

  private Integer version;

  private String schemaType;

  private String schema;

  private List<SchemaReference> references;

  private ConfluentSchemaMetadata(SchemaMetadata schemaMetadata) {
    this.id = schemaMetadata.getId();
    this.version = schemaMetadata.getVersion();
    this.schemaType = schemaMetadata.getSchemaType();
    this.schema = schemaMetadata.getSchema();
    this.references = schemaMetadata.getReferences();
  }

  public static ConfluentSchemaMetadata parse(SchemaMetadata schemaMetadata) {
    return new ConfluentSchemaMetadata(schemaMetadata);
  }

  @Override
  public Integer getGlobalId() {
    return this.getId();
  }

  @Override
  public String getType() {
    return this.schemaType;
  }
}
