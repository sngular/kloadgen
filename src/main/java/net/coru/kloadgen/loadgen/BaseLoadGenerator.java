/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen;

import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors;
import com.google.protobuf.TextFormat;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;

public interface BaseLoadGenerator {

    void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings);

    void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings);

    EnrichedRecord nextMessage();
}
