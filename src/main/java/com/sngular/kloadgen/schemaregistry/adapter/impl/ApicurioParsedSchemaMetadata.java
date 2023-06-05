package com.sngular.kloadgen.schemaregistry.adapter.impl;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApicurioParsedSchemaMetadata extends ParsedSchemaAdapter {

  private Object schema;

  private String rawSchema;

  private String type;

  public ApicurioParsedSchemaMetadata(ParsedSchema parsedSchema){
    this.rawSchema = parsedSchema.canonicalString();
    this.type = parsedSchema.schemaType();
    this.schema = parsedSchema.rawSchema();
  }
  public static ParsedSchemaAdapter parse(final ParsedSchema parsedSchema) {
    return new ApicurioParsedSchemaMetadata(parsedSchema);
  }
}
