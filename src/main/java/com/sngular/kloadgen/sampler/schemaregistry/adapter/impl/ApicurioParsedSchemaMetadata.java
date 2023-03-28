package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import com.sngular.kloadgen.sampler.schemaregistry.schema.ApicurioParsedSchema;
import lombok.Getter;

@Getter
public class ApicurioParsedSchemaMetadata extends ParsedSchemaAdapter {

  private final String type;

  private final Object schema;

  private final String rawSchema;

  public ApicurioParsedSchemaMetadata(final ApicurioParsedSchema apicurioParsedSchema) {
    this.type = apicurioParsedSchema.getType().toString();
    this.schema = apicurioParsedSchema.getSchema();
    this.rawSchema = apicurioParsedSchema.getRawSchema();
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public Object getRawSchema() {
    return this.rawSchema;
  }
}
