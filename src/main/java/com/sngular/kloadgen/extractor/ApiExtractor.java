package com.sngular.kloadgen.extractor;

import java.io.File;
import java.util.List;

import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;

public interface ApiExtractor {

  AsyncApiFile processFile(File apiFile);

  List<AsyncApiServer> getBrokerData(AsyncApiFile asyncApiFile);

  List<AsyncApiSR> getSchemaRegistryData(AsyncApiFile asyncApiFile);

  List<AsyncApiSchema> getSchemaData(AsyncApiFile asyncApiFile);
}
