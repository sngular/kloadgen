package com.sngular.kloadgen.sampler;

import org.apache.kafka.clients.producer.KafkaProducer;

public class AsyncapiSamplerWrapper extends AsyncApiSampler {

  protected AsyncapiSamplerWrapper(final KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  private KafkaProducer kafkaProducer;

  @Override
  public KafkaProducer getKafkaProducer() {
    return kafkaProducer;
  }
}
