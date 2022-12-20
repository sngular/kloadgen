/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.sngular.kloadgen.model.json.Schema;

public interface SchemaParser {

  Schema parse(String jsonSchema);

  Schema parse(JsonNode jsonNode);
}
