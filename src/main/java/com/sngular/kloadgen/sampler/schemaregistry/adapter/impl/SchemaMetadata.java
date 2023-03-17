package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import lombok.Builder;

@Builder
public class SchemaMetadata {

  private String id;

  private String version;

  private String schemaType;
}
