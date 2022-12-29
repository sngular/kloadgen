/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class GenericJsonRecordSerializer<T extends ObjectNode> implements Serializer<T> {

  @Override
  public final byte[] serialize(final String topic, final T data) {

    byte[] dataBytes = new byte[0];
    try {
      final var mapper = new ObjectMapper();
      mapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
      dataBytes = mapper.writeValueAsString(data).getBytes(StandardCharsets.US_ASCII);
    } catch (final IOException ex) {
      log.error("Serialization error:" + ex.getMessage());
    }
    return dataBytes;
  }

}
