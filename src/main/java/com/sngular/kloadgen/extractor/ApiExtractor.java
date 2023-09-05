package com.sngular.kloadgen.extractor;

import java.io.File;
import java.util.List;
import java.util.Map;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;

public interface ApiExtractor {

  AsyncApiFile processFile(final File apiFile);

  List<AsyncApiServer> getBrokerData(final AsyncApiFile asyncApiFile);

  List<AsyncApiSR> getSchemaRegistryData(final AsyncApiFile asyncApiFile);

  AsyncApiSchema getSchemaData(final AsyncApiFile asyncApiFile, final String topic);

  Map<String, AsyncApiSchema> getSchemaDataMap(final AsyncApiFile asyncApiFile);
}
