/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.serializer.EnrichedRecord;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;


import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static net.coru.kloadgen.util.PropsKeysHelper.*;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_VALUE;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;


@Slf4j
public class KafkaConsumerSampler extends AbstractJavaSamplerClient implements Serializable {

    private BaseLoadGenerator generator;
    private EnrichedRecord messageVal;
    private Long timeout;
    private KafkaConsumer consumer;

    @Override
    public Arguments getDefaultParameters() {
        return SamplerUtil.getCommonConsumerDefaultParameters();
    }

    public Properties properties (JavaSamplerContext context) {
        Properties props = SamplerUtil.setupCommonConsumerProperties(context);
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "net.coru.kloadgen.serializer.AvroDeserializer");
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
        consumer = new KafkaConsumer(props);
        consumer.subscribe(singletonList(topic));
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        try {
            boolean running = true;
            Instant startTime = Instant.now();
            while(running ) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
                if (!records.isEmpty()) {
                    running=false;
                    ConsumerRecord<String, Object> record = records.iterator().next();
                    fillSampleResult(sampleResult, prettyfy(record),true);

                }
                Instant endTime = Instant.now();
                if ( Duration.between(startTime,endTime).toMillis() > timeout) {
                    throw new KLoadGenException("Time Out in Consumer");
                }
            }

        } catch(KLoadGenException e) {
            logger().error("Failed to receive message", e);
            fillSampleResult(sampleResult, e.getMessage() != null ? e.getMessage() : "",false);

        } finally {
            consumer.close();
        }
        return sampleResult;
    }

    private String prettyfy(ConsumerRecord<String, Object> record) {
        return "{ key:" + record.key() + ", value:" + record.value() +"}";
    }

    private void fillSampleResult(SampleResult sampleResult, String respondeData, boolean successful) {
        sampleResult.setResponseData(respondeData, StandardCharsets.UTF_8.name());
        sampleResult.setSuccessful(successful);
        sampleResult.sampleEnd();
    }
}
