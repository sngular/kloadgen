package com.sngular.kloadgen.extractor.model;

import java.util.Map;

public record AsyncApiSR(String name, String schemaRegistryUrl,
                         String schemaRegistryVendor) implements AsyncApiAbstract {

  @Override
  public Object[] getProperties() {
    return new Object[]{schemaRegistryUrl, schemaRegistryVendor};
  }

  @Override
  public Map<String, String> getPropertiesMap() {
    return Map.of("url", schemaRegistryUrl, "type", schemaRegistryVendor);
  }
}
