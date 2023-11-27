/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;

public interface ApiExtractor {

  AsyncApiFile processFile(final File apiFile);

  AsyncApiFile processNode(final JsonNode apiFile);

  Map<String, AsyncApiServer> getBrokerData(final AsyncApiFile asyncApiFile);

  List<AsyncApiSR> getSchemaRegistryData(final AsyncApiFile asyncApiFile);

  AsyncApiSchema getSchemaData(final AsyncApiFile asyncApiFile, final String topic);

  Map<String, AsyncApiSchema> getSchemaDataMap(final AsyncApiFile asyncApiFile);

}
