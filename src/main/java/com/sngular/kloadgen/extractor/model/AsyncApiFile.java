package com.sngular.kloadgen.extractor.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class AsyncApiFile {

  Map<String, AsyncApiServer> apiServerMap;

  List<AsyncApiSR> apiAsyncApiSRList;

  Map<String, AsyncApiSchema> apiSchemaList;
}
