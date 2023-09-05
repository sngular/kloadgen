package com.sngular.kloadgen.extractor.model;

import lombok.Data;
import lombok.Value;

@Value
@Data
public class AsyncApiSR implements AsyncApiAbstract {

  String schemaRegistryUrl;

  String schemaRegistryVendor;

  @Override
  public Object[] getProperties() {
    return new Object[] {schemaRegistryUrl, schemaRegistryVendor};
  }
}
