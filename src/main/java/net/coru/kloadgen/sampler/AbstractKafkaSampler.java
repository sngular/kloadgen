/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_HEADERS;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static net.coru.kloadgen.util.PropsKeysHelper.KEYED_MESSAGE_KEY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SUBJECT_NAME;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.MSG_KEY_VALUE;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.StatelessRandomTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;

public abstract class AbstractKafkaSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private final transient StatelessRandomTool statelessRandomTool;
    private transient KafkaProducer<Object, Object> producer;
    private String topic;
    private String msgKeyType;
    private List<String> msgKeyValue;
    private boolean keyMessageFlag = false;
    private transient BaseLoadGenerator generator;
    private transient BaseLoadGenerator keyGenerator;

    AbstractKafkaSampler() {
        this.statelessRandomTool = new StatelessRandomTool();
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        Properties props = properties(context);
        generator = SamplerUtil.configureValueGenerator(props);

        if (FLAG_YES.equals(context.getParameter(KEYED_MESSAGE_KEY))) {
            keyMessageFlag = true;
            if (!Objects.isNull(JMeterContextService.getContext().getVariables().get(KEY_SUBJECT_NAME))) {
                keyGenerator = SamplerUtil.configureKeyGenerator(props);
            } else {
                msgKeyType = context.getParameter(MESSAGE_KEY_KEY_TYPE);
                msgKeyValue = MSG_KEY_VALUE.equalsIgnoreCase(context.getParameter(MESSAGE_KEY_KEY_VALUE))
                    ? emptyList() : singletonList(context.getParameter(MESSAGE_KEY_KEY_VALUE));
            }
        }

        topic = context.getParameter(KAFKA_TOPIC_CONFIG);
        producer = new KafkaProducer<>(props);
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {

        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        JMeterContext jMeterContext = JMeterContextService.getContext();
        EnrichedRecord messageVal = generator.nextMessage();
        List<HeaderMapping> kafkaHeaders = safeGetKafkaHeaders(jMeterContext);

        if (Objects.nonNull(messageVal)) {

            ProducerRecord<Object, Object> producerRecord;
            try {
                producerRecord = getProducerRecord(messageVal);
                List<String> headersSB = new ArrayList<>(SamplerUtil.populateHeaders(kafkaHeaders, producerRecord));

                sampleResult.setRequestHeaders(StringUtils.join(headersSB, ","));
                sampleResult.setSamplerData(producerRecord.value().toString());

                Future<RecordMetadata> result = producer.send(producerRecord, (metadata, e) -> {
                    if (e != null) {
                        logger().error("Send failed for record {}", producerRecord, e);
                        throw new KLoadGenException("Failed to sent message due ", e);
                    }
                });

                logger().info("Send message to body: {}", producerRecord.value());
                fillSampleResult(sampleResult, prettyPrint(result.get()),false);
            } catch (Exception e) {
                logger().error("Failed to send message", e);
                fillSampleResult(sampleResult, e.getMessage() != null ? e.getMessage() : "",false);
            }
        } else {
            logger().error("Failed to Generate message");
            fillSampleResult(sampleResult,"Failed to Generate message",false);
        }
        return sampleResult;
    }

    private ProducerRecord<Object, Object> getProducerRecord(EnrichedRecord messageVal) {
        ProducerRecord<Object, Object> producerRecord;
        if (keyMessageFlag) {
            if (Objects.isNull(keyGenerator)) {
                Object key = statelessRandomTool.generateRandom("key", msgKeyType, 0, msgKeyValue).toString();
                producerRecord = new ProducerRecord<>(topic, key, messageVal);
            } else {
                EnrichedRecord key = keyGenerator.nextMessage();
                producerRecord = new ProducerRecord<>(topic, key.getGenericRecord(), messageVal);
            }
        } else {
            producerRecord = new ProducerRecord<>(topic, messageVal);
        }
        return producerRecord;
    }

    private void fillSampleResult(SampleResult sampleResult, String respondeData, boolean successful) {
        sampleResult.setResponseData(respondeData, StandardCharsets.UTF_8.name());
        sampleResult.setSuccessful(successful);
        sampleResult.sampleEnd();
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (Objects.nonNull(producer)) {
            producer.close();
        }
    }

    protected abstract Properties properties(JavaSamplerContext context);

    protected abstract Logger logger();

    private List<HeaderMapping> safeGetKafkaHeaders(JMeterContext jMeterContext) {
        List<HeaderMapping> headerMappingList = new ArrayList<>();
        Object headers = jMeterContext.getSamplerContext().get(KAFKA_HEADERS);
        if (null != headers) {
            headerMappingList.addAll((List) headers);
        }
        return headerMappingList;
    }

    private String prettyPrint(RecordMetadata recordMetadata) {
        String template = "Topic: %s, partition: %s, offset: %s";
        return String.format(template, recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
    }
}
