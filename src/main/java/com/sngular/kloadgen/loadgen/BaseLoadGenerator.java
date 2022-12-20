/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.loadgen;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.serializer.EnrichedRecord;

public interface BaseLoadGenerator {

  void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings);

  void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings);

  EnrichedRecord nextMessage();
}
