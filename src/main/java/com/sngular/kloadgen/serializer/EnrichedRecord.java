/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EnrichedRecord {

  SchemaMetadataAdapter schemaMetadata;

  Object genericRecord;
}

