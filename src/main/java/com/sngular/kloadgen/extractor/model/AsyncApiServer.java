package com.sngular.kloadgen.extractor.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AsyncApiServer {

  String name;

  String url;

  String protocol;

  String description;
}
