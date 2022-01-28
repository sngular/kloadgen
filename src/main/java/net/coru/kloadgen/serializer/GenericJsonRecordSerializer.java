/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharSet;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class GenericJsonRecordSerializer<T extends ObjectNode> implements Serializer<T> {

  @Override
  public byte[] serialize(String topic , T record) {

    byte[] data = new byte[0];
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature() , true);
      data = mapper.writeValueAsString(record).getBytes(CharSet.ASCII_ALPHA.toString());
    } catch (IOException e) {
      log.error("Serialization error:" + e.getMessage());
    }
    return data;
  }

  @Override
  public byte[] serialize(String topic , Headers headers , T data) {
    return new byte[0];
  }
}
