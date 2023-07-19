package com.sngular.kloadgen.extractor.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AsyncApiFile {

  List<AsyncApiServer> apiServerList;

  List<AsyncApiSR> apiAsyncApiSRList;

  List<AsyncApiSchema> apiSchemaList;
}
