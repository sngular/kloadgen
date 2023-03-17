package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;

public class ConfluenceSchemaRegistryAdapter implements GenericSchemaRegistryAdapter{

  private SchemaMetadata schemaMetadata = new SchemaMetadata(getId(), getVersion(), getSchemaType());

  public Integer getId(){
    return schemaMetadata.getId();
  }
  public Integer getVersion(){
    return schemaMetadata.getVersion();
  }

  public String getSchemaType(){
    return schemaMetadata.getSchemaType();
  }
}
