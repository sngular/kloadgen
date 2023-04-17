package com.sngular.kloadgen.sampler.schemaregistry.schema;

import com.sngular.kloadgen.sampler.schemaregistry.adapter.impl.ApicurioParsedSchemaMetadata;
import com.sngular.kloadgen.sampler.schemaregistry.adapter.impl.ParsedSchemaAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApicurioParsedSchema extends ParsedSchemaAdapter {

  private String type;

  private Object schema;

  private String rawSchema;

  @SuppressWarnings("UnnecessarilyFullyQualified")
  private ApicurioParsedSchema(final ApicurioParsedSchema apicurioParsedSchema){
    this.rawSchema= apicurioParsedSchema.getRawSchema();
    this.schema = apicurioParsedSchema.getSchema();
    this.type = apicurioParsedSchema.getType();

  }

  public static ParsedSchemaAdapter parse(final ApicurioParsedSchema schema) {
    return new ApicurioParsedSchema(schema);
  }
}
