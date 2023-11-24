package com.sngular.kloadgen.serializer;

import java.util.Map;

import org.apache.kafka.common.serialization.StringSerializer;

public final class CustomStringEnrichedRecordSerializer<T extends EnrichedRecord> implements EnrichedRecordSerializer<T> {
  private final StringSerializer stringSerializer;

  public CustomStringEnrichedRecordSerializer() {
    stringSerializer = new StringSerializer();
  }

  public void configure(final Map<String, ?> configs, final boolean isKey) {
    stringSerializer.configure(configs, isKey);
  }

  public byte[] serialize(final String topic, final EnrichedRecord data) {
    return stringSerializer.serialize(topic, data.toString());
  }
}
