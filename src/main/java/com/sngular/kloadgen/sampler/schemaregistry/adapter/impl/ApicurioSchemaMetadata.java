package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.apicurio.registry.types.ArtifactState;

public class ApicurioSchemaMetadata extends SchemaMetadata{
  private String name;
  private String description;
  private String createdBy;
  private Date createdOn;
  private String modifiedBy;
  private Date modifiedOn;
  private Long globalId;
  private ArtifactState state;
  private List<String> labels = new ArrayList<String>();
  private Map<String, String> properties;
  private String groupId;
  private Long contentId;

  ApicurioSchemaMetadata(final String id, final String version, final String schemaType) {
    super(id, version, schemaType);
  }
}
