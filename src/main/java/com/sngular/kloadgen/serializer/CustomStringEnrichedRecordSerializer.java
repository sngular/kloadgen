package com.sngular.kloadgen.serializer;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

public class CustomStringEnrichedRecordSerializer<T extends EnrichedRecord> implements EnrichedRecordSerializer<T> {
  StringSerializer stringSerializer;

  public CustomStringEnrichedRecordSerializer() {
    stringSerializer = new StringSerializer();
  }

  public void configure(Map<String, ?> configs, boolean isKey) {
    stringSerializer.configure(configs, isKey);
  }
  public byte[] serialize(String topic, EnrichedRecord data) {
    return stringSerializer.serialize(topic, data.toString());
  }
}
