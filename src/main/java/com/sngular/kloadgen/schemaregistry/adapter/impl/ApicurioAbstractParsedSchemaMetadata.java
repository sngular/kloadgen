package com.sngular.kloadgen.schemaregistry.adapter.impl;

import com.sngular.kloadgen.parsedschema.ParsedSchema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApicurioAbstractParsedSchemaMetadata extends AbstractParsedSchemaAdapter {

  private Object schema;

  private String rawSchema;

  private String type;

  public ApicurioAbstractParsedSchemaMetadata(final ParsedSchema parsedSchema) {
    this.rawSchema = parsedSchema.canonicalString();
    this.type = parsedSchema.schemaType();
    this.schema = parsedSchema.rawSchema();
  }

  public static AbstractParsedSchemaAdapter parse(final ParsedSchema parsedSchema) {
    return new ApicurioAbstractParsedSchemaMetadata(parsedSchema);
  }
}
