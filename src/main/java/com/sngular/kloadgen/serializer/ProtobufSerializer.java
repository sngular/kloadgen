/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import io.confluent.kafka.schemaregistry.protobuf.MessageIndexes;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class ProtobufSerializer<T extends EnrichedRecord> implements Serializer<T> {

  private static final byte MAGIC_BYTE = 0x0;

  private static final int ID_SIZE = 4;

  @Override
  public final byte[] serialize(final String topic, final T data) {
    try {
      byte[] result = null;

      if (data != null) {
        log.debug("data='{}'", data);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(MAGIC_BYTE);
        byteArrayOutputStream.write(ByteBuffer.allocate(ID_SIZE).put(Arrays.copyOfRange(data.getSchemaMetadata().getId().toString().getBytes(), 0, ID_SIZE)).array());
        final Descriptor descriptor = ((DynamicMessage) data.getGenericRecord()).getDescriptorForType();
        final ProtobufSchema schema = new ProtobufSchema(descriptor);
        final MessageIndexes indexes = schema.toMessageIndexes(descriptor.getFullName());
        byteArrayOutputStream.write(indexes.toByteArray());
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

