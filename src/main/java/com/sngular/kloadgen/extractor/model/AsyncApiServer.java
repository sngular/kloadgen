package com.sngular.kloadgen.extractor.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AsyncApiServer implements AsyncApiAbstract {

  String name;

  String url;

  String protocol;

  String description;

  @Override
  public Object[] getProperties() {
    return new Object[0];
  }
}
