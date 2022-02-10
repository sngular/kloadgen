/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA;

@Slf4j
public class AvroDeserializer implements Deserializer<Object> {

  private static final byte MAGIC_BYTE = 0x0;

  private static final int ID_SIZE = 4;

  private static final Map<String, Object> configs = new HashMap<>();

  private boolean isKey;

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    AvroDeserializer.configs.putAll(configs);
    this.isKey = isKey;
  }

  @Override
  public Object deserialize(String topic, byte[] data) {

    Object result;
    String schemaString;
    Decoder decoder;

    schemaString = (String) (isKey ? configs.get(KEY_SCHEMA) : configs.get(VALUE_SCHEMA));

    if (!schemaString.isEmpty()) {
      Schema.Parser parser = new Schema.Parser();
      Schema avroSchema = parser.parse(schemaString);

      DatumReader<?> reader = new GenericDatumReader<GenericRecord>(avroSchema);

      try {
        log.info("[AvroDeserializer] to deserialize = {}", data);

        ByteArrayInputStream bufferArrayInputStream = new ByteArrayInputStream(data);
        decoder = DecoderFactory.get().binaryDecoder(bufferArrayInputStream,null);
        result = reader.read(null, decoder);
        log.info("[AvroDeserializer] retrieved = {}", result);
      } catch (RuntimeException | IOException ex) {
        throw new SerializationException("Error deserializing Avro message");
      }

      return result;

    } else {
      throw new SerializationException("Error deserializing AVRO message - null schema");
    }
  }

  @Override
  public Object deserialize(String topic, Headers headers, byte[] data) {
    return deserialize(topic,  data);
  }

  @Override
  public void close() {

  }

}
