/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class GenericAvroRecordSerializer<T extends GenericRecord> implements Serializer<T> {

  public GenericAvroRecordSerializer() {
    AvroSerializersUtil.setupLogicalTypesConversion();
  }

  @Override
  public final byte[] serialize(final String topic, final T data) {
    final DatumWriter<T> writer = new GenericDatumWriter<>(data.getSchema());

    byte[] dataBytes = new byte[0];
    final var stream = new ByteArrayOutputStream();
    final Encoder jsonEncoder;
    try {
      jsonEncoder = EncoderFactory.get().jsonEncoder(
          data.getSchema(), stream);
      writer.write(data, jsonEncoder);
      jsonEncoder.flush();
      dataBytes = stream.toByteArray();
    } catch (final IOException ex) {
      log.error("Serialization error:" + ex.getMessage());
    }
    return dataBytes;
  }

  @Override
  public final byte[] serialize(final String topic, final Headers headers, final T data) {
    return serialize(topic, data);
  }
}
