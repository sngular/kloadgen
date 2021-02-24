/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;

@Slf4j
public class KafkaSampler extends AbstractKafkaSampler {

    public KafkaSampler() {
        super();
    }

    @Override
    public Arguments getDefaultParameters() {
        return SamplerUtil.getCommonDefaultParameters();
    }

    protected Properties properties(JavaSamplerContext context) {
        Properties props = SamplerUtil.setupCommonProperties(context);
        props.put(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG,
            context.getParameter(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, "false"));
        if (Objects.nonNull(context.getParameter(VALUE_NAME_STRATEGY))) {
            props.put(VALUE_NAME_STRATEGY, context.getParameter(VALUE_NAME_STRATEGY));
        }
        return props;
    }

    @Override
    protected Logger logger() {
        return log;
    }

    private ProducerRecord<Object, Object> getProducerRecord(EnrichedRecord messageVal, boolean keyFlag, boolean valueFlag) {
        ProducerRecord<Object, Object> producerRecord;
        if (keyMessageFlag) {
            if (Objects.isNull(keyGenerator)) {
                Object key = statelessRandomTool.generateRandom("key", msgKeyType, 0, msgKeyValue).toString();
                producerRecord = new ProducerRecord<>(topic, key, getObject(messageVal, valueFlag));
            } else {
                EnrichedRecord key = keyGenerator.nextMessage();
                producerRecord = new ProducerRecord<>(topic, getObject(key, keyFlag), getObject(messageVal, valueFlag));
            }
        } else {
            producerRecord = new ProducerRecord<>(topic, getObject(messageVal, valueFlag));
        }
        return producerRecord;
    }

    private Object getObject(EnrichedRecord messageVal, boolean valueFlag) {
        return valueFlag ? messageVal : messageVal.getGenericRecord();
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
