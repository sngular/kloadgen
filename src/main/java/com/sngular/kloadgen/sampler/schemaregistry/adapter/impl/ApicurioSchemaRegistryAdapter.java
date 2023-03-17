package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import io.apicurio.registry.rest.v2.beans.ArtifactMetaData;

class ApicurioSchemaRegistryAdapter implements  GenericSchemaRegistryAdapter {

private ArtifactMetaData artifactMetaData = new ArtifactMetaData();
  public String getId() {
    return artifactMetaData.getId();
  }

  public String getVersion() {
    return artifactMetaData.getVersion();
  }

  public String getSchemaType() {
    return artifactMetaData.getType().value();
  }
}
