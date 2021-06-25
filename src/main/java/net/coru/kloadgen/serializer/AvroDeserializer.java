/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class AvroDeserializer<T extends EnrichedRecord> implements Deserializer<T> {

  private static final byte MAGIC_BYTE = 0x0;

  private static final int idSize = 4;

  private static final Map<String, Object> configs = new HashMap<>();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    AvroDeserializer.configs.putAll(configs);
  }

  @Override
  public T deserialize(String s, byte[] data) {

    T result = null;

      return result;

  }

  @Override
  public T deserialize(String topic, Headers headers, byte[] data) {
    return deserialize(topic,  data);
  }

  @Override
  public void close() {

  }
}
