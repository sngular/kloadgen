package com.sngular.kloadgen.sampler.schemaregistry.schema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApicurioParsedSchema {

  private String type;

  private Object schema;

  private String rawSchema;
}
