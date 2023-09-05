package com.sngular.kloadgen.extractor.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AsyncApiFile {

  List<AsyncApiServer> apiServerList;

  List<AsyncApiSR> apiAsyncApiSRList;

  Map<String, AsyncApiSchema> apiSchemaList;
}
