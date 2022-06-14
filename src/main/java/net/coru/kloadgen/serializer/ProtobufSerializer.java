/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import io.confluent.kafka.schemaregistry.protobuf.MessageIndexes;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class ProtobufSerializer<T extends EnrichedRecord> implements Serializer<T> {

  private static final byte MAGIC_BYTE = 0x0;

  private static final int ID_SIZE = 4;

  @Override
  public byte[] serialize(String topic, T data) {
    try {
      byte[] result = null;

      if (data != null) {
        log.debug("data='{}'", data);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(MAGIC_BYTE);
        byteArrayOutputStream.write(ByteBuffer.allocate(ID_SIZE).putInt(data.getSchemaMetadata().getId()).array());
        Descriptor descriptor = ((DynamicMessage) data.getGenericRecord()).getDescriptorForType();
        ProtobufSchema schema = new ProtobufSchema(descriptor);
        MessageIndexes indexes = schema.toMessageIndexes(descriptor.getFullName());
        byteArrayOutputStream.write(indexes.toByteArray());
        ((DynamicMessage) data.getGenericRecord()).writeTo(byteArrayOutputStream);
        result = byteArrayOutputStream.toByteArray();
        log.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
      }
      return result;
    } catch (IOException ex) {
      throw new SerializationException("Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
    }
  }

  @Override
  public byte[] serialize(String topic, Headers headers, T data) {
    return serialize(topic, data);
  }

}

