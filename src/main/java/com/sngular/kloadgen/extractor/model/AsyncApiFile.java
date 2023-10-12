package com.sngular.kloadgen.extractor.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class AsyncApiFile {

  @Singular("apiServer")
  Map<String, AsyncApiServer> apiServerMap;

  @Singular("apiAsyncApiSR")
  List<AsyncApiSR> apiAsyncApiSRList;

  @Singular("apiSchema")
  Map<String, AsyncApiSchema> apiSchemaList;
}
