package com.sngular.kloadgen.extractor.model;

public record AsyncApiSR(String name, String schemaRegistryUrl,
                         String schemaRegistryVendor) implements AsyncApiAbstract {

  @Override
  public Object[] getProperties() {
    return new Object[]{schemaRegistryUrl, schemaRegistryVendor};
  }
}
