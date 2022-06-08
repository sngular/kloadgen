/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class ProtobufSerializer<T extends EnrichedRecord> implements Serializer<T> {

  @Override
  public final byte[] serialize(final String topic, final T data) {
    try {
      byte[] result = null;

      if (data != null) {
        log.debug("data='{}'", data);

        final var byteArrayOutputStream = new ByteArrayOutputStream();

        ((DynamicMessage) data.getGenericRecord()).writeTo(byteArrayOutputStream);

        result = byteArrayOutputStream.toByteArray();
        log.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
      }
      return result;
    } catch (final IOException ex) {
      throw new SerializationException("Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
    }
  }

  @Override
  public final byte[] serialize(final String topic, final Headers headers, final T data) {
    return serialize(topic, data);
  }

}

