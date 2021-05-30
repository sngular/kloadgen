/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class KafkaConsumerSampler extends AbstractJavaSamplerClient implements Serializable {

  private Long timeout;
  private KafkaConsumer<Object, Object> consumer;

  @Override
  public Arguments getDefaultParameters() {
    return SamplerUtil.getCommonConsumerDefaultParameters();
  }

  public Properties properties (JavaSamplerContext context) {
    Properties props = SamplerUtil.setupCommonConsumerProperties(context);
    props.put(MAX_POLL_RECORDS_CONFIG,"1");
    props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(SESSION_TIMEOUT_MS_CONFIG,"10000");
    timeout = Long.parseLong(props.getProperty("timeout.millis"));
    log.debug("Populated properties: {}", props);
    return props;
  }

  protected Logger logger() {
    return KafkaConsumerSampler.log;
  }

  @Override
  public void setupTest(JavaSamplerContext context) {
    Properties props = properties(context);

    String topic = context.getParameter(KAFKA_TOPIC_CONFIG);
    consumer = new KafkaConsumer<>(props);
    Collection<TopicPartition> partitionList = CollectionUtils.collect(
            consumer.partitionsFor(topic),
            transform());
    consumer.assign(partitionList);
    consumer.seekToBeginning(partitionList);
  }

  private Transformer<PartitionInfo, TopicPartition> transform() {
    return partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
  }

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    SampleResult sampleResult = new SampleResult();
    sampleResult.sampleStart();
    try {
      boolean running = true;
      Instant startTime = Instant.now();
      while (running) {
        ConsumerRecords<Object, Object> records = consumer.poll(Duration.of(5, ChronoUnit.SECONDS));
        if (!records.isEmpty()) {
          running=false;
          ConsumerRecord<Object, Object> record = records.iterator().next();
          fillSampleResult(sampleResult, prettify(record), true);
        }

        Instant endTime = Instant.now();
        if ( Duration.between(startTime,endTime).toMillis() > timeout) {
          throw new KLoadGenException("Time Out in Consumer");
        }
      }
    } catch (Exception e) {
      logger().error("Failed to receive message", e);
      fillSampleResult(sampleResult, e.getMessage() != null ? e.getMessage() : "", false);
    } finally {
      if (Objects.nonNull(consumer) ) {
        consumer.close();
      }
    }
    return sampleResult;
  }

  private String prettify(ConsumerRecord<Object, Object> record) {
    return "{ key: " + record.key() + ", value: " + record.value() +" }";
  }

  private void fillSampleResult(SampleResult sampleResult, String responseData, boolean successful) {
    sampleResult.setResponseData(responseData, StandardCharsets.UTF_8.name());
    sampleResult.setSuccessful(successful);
    sampleResult.sampleEnd();
  }
}
