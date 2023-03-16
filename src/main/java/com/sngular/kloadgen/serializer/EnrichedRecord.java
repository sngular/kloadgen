/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import lombok.Builder;
import lombok.Value;
import com.sngular.kloadgen.sampler.schemaregistry.schema.KloadSchemaMetadata;

@Value
@Builder
public class EnrichedRecord {

  KloadSchemaMetadata schemaMetadata;

  Object genericRecord;
}

