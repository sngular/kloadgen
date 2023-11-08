package com.sngular.kloadgen.schemaregistry.adapter.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.apicurio.registry.rest.v2.beans.ArtifactMetaData;
import io.apicurio.registry.types.ArtifactState;
import lombok.Getter;

@Getter
public class ApicurioSchemaMetadata implements SchemaMetadataAdapter {

  private final String id;

  private final String version;

  private final String schemaType;

  private final String name;

  private final String description;

  private final String createdBy;

  private final Date createdOn;

  private final String modifiedBy;

  private final Date modifiedOn;

  private final Long globalId;

  private final ArtifactState state;

  private final List<String> labels;

  private final Map<String, String> properties;

  private final String groupId;

  private final Long contentId;

  public ApicurioSchemaMetadata(final ArtifactMetaData artifactMetaData) {
    this.id = artifactMetaData.getId();
    this.version = artifactMetaData.getVersion();
    this.schemaType = artifactMetaData.getType().toString();
    this.name = artifactMetaData.getName();
    this.description = artifactMetaData.getDescription();
    this.createdBy = artifactMetaData.getCreatedBy();
    this.createdOn = artifactMetaData.getCreatedOn();
    this.modifiedBy = artifactMetaData.getModifiedBy();
    this.modifiedOn = artifactMetaData.getModifiedOn();
    this.globalId = artifactMetaData.getGlobalId();
    this.state = artifactMetaData.getState();
    this.labels = artifactMetaData.getLabels();
    this.properties = artifactMetaData.getProperties();
    this.groupId = artifactMetaData.getGroupId();
    this.contentId = artifactMetaData.getContentId();
  }

  @Override
  public final String getType() {
    return this.schemaType;
  }

  @Override
  public final <T> List<T> getReferences() {
    return Collections.emptyList();
  }

}
