package com.sngular.kloadgen.schemaregistry.adapter.impl;

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

}
