package com.sngular.kloadgen.extractor;

import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ApiExtractor {

  AsyncApiFile processFile(final File apiFile);

  Map<String, AsyncApiServer> getBrokerData(final AsyncApiFile asyncApiFile);

  List<AsyncApiSR> getSchemaRegistryData(final AsyncApiFile asyncApiFile);

  AsyncApiSchema getSchemaData(final AsyncApiFile asyncApiFile, final String topic);

  Map<String, AsyncApiSchema> getSchemaDataMap(final AsyncApiFile asyncApiFile);

}
