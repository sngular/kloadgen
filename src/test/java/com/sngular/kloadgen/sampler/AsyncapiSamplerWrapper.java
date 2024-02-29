package com.sngular.kloadgen.sampler;

import org.apache.kafka.clients.producer.KafkaProducer;

public class AsyncapiSamplerWrapper extends AsyncApiSampler {

  private KafkaProducer kafkaProducer;

  protected AsyncapiSamplerWrapper(final KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  @Override
  public final KafkaProducer getKafkaProducer() {
    return kafkaProducer;
  }
}
