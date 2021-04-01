/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.coru.kloadgen.util.ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_HEADERS;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.VALUE_NAME_STRATEGY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEYED_MESSAGE_KEY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SUBJECT_NAME;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.MSG_KEY_VALUE;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

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
import net.coru.kloadgen.serializer.AvroSerializer;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.StatelessRandomTool;
import org.apache.commons.lang3.StringUtils;
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

public class KafkaSampler extends AbstractJavaSamplerClient implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient StatelessRandomTool statelessRandomTool = new StatelessRandomTool();

    private transient KafkaProducer<Object, Object> producer;

    private String topic;

    private String msgKeyType;

    private List<String> msgKeyValue;

    private boolean keyMessageFlag = false;

    private transient BaseLoadGenerator generator;

    private transient BaseLoadGenerator keyGenerator;

    private transient Properties props;

    @Override
    public void setupTest(JavaSamplerContext context) {
        props = properties(context);
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
        } else {
            props.put(KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER_CLASS_CONFIG_DEFAULT);
        }

        topic = context.getParameter(KAFKA_TOPIC_CONFIG);
        try {
            producer = new KafkaProducer<>(props);
        } catch (KafkaException e) {
            getNewLogger().error(e.getMessage(), e);
        }
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
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {

        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        JMeterContext jMeterContext = JMeterContextService.getContext();
        EnrichedRecord messageVal = generator.nextMessage();
        List<HeaderMapping> kafkaHeaders = safeGetKafkaHeaders(jMeterContext);

        if (Objects.nonNull(messageVal)) {

            ProducerRecord<Object, Object> producerRecord;
            try {
                producerRecord = getProducerRecord(messageVal, enrichedKeyFlag(jMeterContext), enrichedValueFlag(jMeterContext));
                List<String> headersSB = new ArrayList<>(SamplerUtil.populateHeaders(kafkaHeaders, producerRecord));

                sampleResult.setRequestHeaders(StringUtils.join(headersSB, ","));
                fillSamplerResult(producerRecord, sampleResult);

                Future<RecordMetadata> result = producer.send(producerRecord, (metadata, e) -> {
                    if (e != null) {
                        super.getNewLogger().error("Send failed for record {}", producerRecord, e);
                        throw new KLoadGenException("Failed to sent message due ", e);
                    }
                });

                super.getNewLogger().info("Send message to body: {}", producerRecord.value());
                fillSampleResult(sampleResult, prettyPrint(result.get()), true);
            } catch (Exception e) {
                super.getNewLogger().error("Failed to send message", e);
                fillSampleResult(sampleResult, e.getMessage() != null ? e.getMessage() : "", false);
            }
        } else {
            super.getNewLogger().error("Failed to Generate message");
            fillSampleResult(sampleResult, "Failed to Generate message", false);
        }
        return sampleResult;
    }

    private Boolean enrichedValueFlag(JMeterContext jMeterContext) {
        return props.get(VALUE_SERIALIZER_CLASS_CONFIG).equals(AvroSerializer.class.getName());
    }

    private Boolean enrichedKeyFlag(JMeterContext jMeterContext) {
        return props.get(KEY_SERIALIZER_CLASS_CONFIG).equals(AvroSerializer.class.getName());
    }

    private void fillSamplerResult(ProducerRecord<Object, Object> producerRecord, SampleResult sampleResult) {
        if (Objects.isNull(producerRecord.key())) {
            sampleResult.setSamplerData(String.format("key: null, payload: %s", producerRecord.value().toString()));
        } else {
            sampleResult.setSamplerData(String.format("key: %s, payload: %s", producerRecord.key().toString(),
                producerRecord.value().toString()));
        }
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
