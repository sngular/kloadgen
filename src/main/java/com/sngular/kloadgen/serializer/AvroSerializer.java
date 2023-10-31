/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class AvroSerializer<T extends EnrichedRecord> implements Serializer<T> {

  private static final byte MAGIC_BYTE = 0x0;

  private static final int ID_SIZE = 32;

  public AvroSerializer() {
    AvroSerializersUtil.setupLogicalTypesConversion();
  }

  @Override
  public final byte[] serialize(final String topic, final T data) {
    try {
      byte[] result = null;

      if (data != null) {
        log.debug("data='{}'", data);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(MAGIC_BYTE);
        byteArrayOutputStream.write(ByteBuffer.allocate(ID_SIZE).put(data.getSchemaMetadata().getId().toString().getBytes()).array());
        final var binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
        final var datumWriter = new GenericDatumWriter<>(((GenericRecord) data.getGenericRecord()).getSchema());
        datumWriter.write(data.getGenericRecord(), binaryEncoder);

        binaryEncoder.flush();
        byteArrayOutputStream.close();

        result = byteArrayOutputStream.toByteArray();
        log.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
      }
      return result;
    } catch (final IOException ex) {
      throw new SerializationException(
          "Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
    }
  }

  @Override
  public final byte[] serialize(final String topic, final Headers headers, final T data) {
    return serialize(topic, data);
  }

}
