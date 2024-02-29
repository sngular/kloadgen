/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class AsyncApiFile {

  JsonNode asyncApiFileNode;

  @Singular("apiServer")
  transient Map<String, AsyncApiServer> apiServerMap;

  @Singular("apiAsyncApiSR")
  transient List<AsyncApiSR> apiAsyncApiSRList;

  @Singular("apiSchema")
  transient Map<String, AsyncApiSchema> apiSchemaList;
}
