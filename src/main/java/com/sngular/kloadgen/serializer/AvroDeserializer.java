/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.sngular.kloadgen.util.PropsKeysHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class AvroDeserializer implements Deserializer<Object> {

  private static final byte MAGIC_BYTE = 0x0;

  private static final int ID_SIZE = 32;

  private static final Map<String, Object> CONFIGS = new HashMap<>();

  private boolean isKey;

  @Override
  public final void configure(final Map<String, ?> configs, final boolean isKey) {
    AvroDeserializer.CONFIGS.putAll(configs);
    this.isKey = isKey;
  }

  @Override
  public final Object deserialize(final String topic, final byte[] data) {

    final Object result;
    final String schemaString;
    final Decoder decoder;

    schemaString = (String) (isKey ? CONFIGS.get(PropsKeysHelper.KEY_SCHEMA) : CONFIGS.get(PropsKeysHelper.VALUE_SCHEMA));

    if (!schemaString.isEmpty()) {
      final var parser = new Schema.Parser();
      final var avroSchema = parser.parse(schemaString);

      final var buffer = getByteBuffer(data);
      final var reader = new GenericDatumReader<>(avroSchema);

      final int length = buffer.limit() - 5;
      final int start = buffer.position() + buffer.arrayOffset();

      try {
        decoder = DecoderFactory.get().binaryDecoder(buffer.array(), start, length, null);
        result = reader.read(null, decoder);
      } catch (final IOException ex) {
        throw new SerializationException("Error deserializing Avro message");
      }

      return result;

    } else {
      throw new SerializationException("Error deserializing AVRO message - null schema");
    }
  }

  @Override
  public final Object deserialize(final String topic, final Headers headers, final byte[] data) {
    return deserialize(topic, data);
  }

  private ByteBuffer getByteBuffer(final byte[] payload) {
    final var buffer = ByteBuffer.wrap(payload);
    if (buffer.get() != MAGIC_BYTE) {
      throw new SerializationException("Unknown magic byte!");
    } else {
      buffer.position(buffer.position() + ID_SIZE);
      return buffer;
    }
  }

  @Override
  public void close() {
    // No need to be implemented
  }
}
