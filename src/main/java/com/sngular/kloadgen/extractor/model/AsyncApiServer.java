package com.sngular.kloadgen.extractor.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class AsyncApiServer implements AsyncApiAbstract {

  String name;

  String url;

  String protocol;

  String description;

  @Override
  public Object[] getProperties() {
    return new Object[] {name, url, protocol, description};
  }

  @Override
  public Map<String, String> getPropertiesMap() {
    return Map.of("name", name, "url", url, "protocol", protocol);
  }
}
