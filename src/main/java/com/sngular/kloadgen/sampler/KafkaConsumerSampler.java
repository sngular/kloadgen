/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.sampler;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

import com.sngular.kloadgen.util.ProducerKeysHelper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class KafkaConsumerSampler extends AbstractJavaSamplerClient implements Serializable {

  private Long timeout;

  private transient KafkaConsumer<Object, Object> consumer;

  @Override
  public final void setupTest(final JavaSamplerContext context) {

    final var props = properties(context);
    final var topic = context.getParameter(ProducerKeysHelper.KAFKA_TOPIC_CONFIG);
    consumer = new KafkaConsumer<>(props);

    consumer.subscribe(Collections.singletonList(topic));
  }

  public final Properties properties(final JavaSamplerContext context) {
    final var props = SamplerUtil.setupCommonConsumerProperties(context);
    //todo: obtener unas properties u otras en funcion de si el schema registry es de Confluent o de Apicurio
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    timeout = Long.parseLong(props.getProperty("timeout.millis"));
    log.debug("Populated properties: {}", props);
    return props;
  }

  @Override
  public final void teardownTest(final JavaSamplerContext context) {
    if (Objects.nonNull(consumer)) {
      consumer.close();
    }
  }

  @Override
  public final Arguments getDefaultParameters() {
    return SamplerUtil.getCommonConsumerDefaultParameters();
  }

  @Override
  public final SampleResult runTest(final JavaSamplerContext javaSamplerContext) {
    var sampleResult = new SampleResult();
    sampleResult.sampleStart();
    final var thread = javaSamplerContext.getJMeterContext().getThread();
    try {
      boolean running = true;
      final var startTime = Instant.now();
      while (running) {
        final var records = consumer.poll(Duration.of(5, ChronoUnit.SECONDS));

        if (!records.isEmpty()) {
          running = false;
          final var consumerRecord = records.iterator().next();
          fillSampleResult(sampleResult, prettify(consumerRecord), true);
          consumer.commitSync();
        }

        final var endTime = Instant.now();
        if (Duration.between(startTime, endTime).toMillis() > timeout) {
          running = false;
          sampleResult = null;
          if (Objects.nonNull(thread)) {
            thread.stop();
          }
        }
      }
    } catch (final IllegalStateException ex) {
      log.error("Failed to receive message", ex);
      fillSampleResult(sampleResult, ex.getMessage() != null ? ex.getMessage() : "",
                       false);
    }
    return sampleResult;
  }

  private void fillSampleResult(final SampleResult sampleResult, final String responseData, final boolean successful) {
    if (Objects.nonNull(sampleResult)) {
      sampleResult.setResponseData(responseData, StandardCharsets.UTF_8.name());
      sampleResult.setSuccessful(successful);
      sampleResult.sampleEnd();
    }
  }

  private String prettify(final ConsumerRecord<Object, Object> consumerRecord) {
    return "{ partition: " + consumerRecord.partition() + ", message: { key: " + consumerRecord.key()
           + ", value: " + consumerRecord.value().toString() + " }}";
  }

}
