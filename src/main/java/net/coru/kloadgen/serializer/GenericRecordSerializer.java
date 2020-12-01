/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class GenericRecordSerializer<T extends GenericRecord>  implements Serializer<T> {

  @Override
  public byte[] serialize(String topic, T record) {

    DatumWriter<T> writer = new SpecificDatumWriter<>(record.getSchema());
    byte[] data = new byte[0];
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Encoder jsonEncoder;
    try {
      jsonEncoder = EncoderFactory.get().jsonEncoder(
          record.getSchema(), stream);
      writer.write(record, jsonEncoder);
      jsonEncoder.flush();
      data = stream.toByteArray();
    } catch (IOException e) {
      log.error("Serialization error:" + e.getMessage());
    }
    return data;
  }

  @Override
  public byte[] serialize(String topic, Headers headers, T data) {
    return serialize(topic, data);
  }
}
