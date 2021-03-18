/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
public class AvroDeserializer<T extends EnrichedRecord>  implements Deserializer<T> {
  private static final byte MAGIC_BYTE = 0x0;
  private static final int idSize = 4;

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

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
