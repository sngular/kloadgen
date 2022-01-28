/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.Value;

@Value
public class EnrichedRecord {

  SchemaMetadata schemaMetadata;

  Object genericRecord;
}
