/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import lombok.Builder;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

@Value
@Builder
public class EnrichedRecord implements GenericRecord {

  SchemaMetadataAdapter schemaMetadata;

  Object genericRecord;

  public String toString() {
    return genericRecord.toString();
  }

  @Override
  public void put(String key, Object v) {
    ((GenericRecord) genericRecord).put(key, v);
  }

  @Override
  public Object get(String key) {
    return ((GenericRecord) genericRecord).get(key);
  }

  @Override
  public void put(int i, Object v) {
    ((GenericRecord) genericRecord).put(i, v);
  }

  @Override
  public Object get(int i) {
    return ((GenericRecord) genericRecord).get(i);
  }

  @Override
  public Schema getSchema() {
    return ((GenericRecord) genericRecord).getSchema();
  }
}

