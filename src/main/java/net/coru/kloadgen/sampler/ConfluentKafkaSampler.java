
/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.coru.kloadgen.util.ProducerKeysHelper.ACKS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BATCH_SIZE_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BOOTSTRAP_SERVERS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BUFFER_MEMORY_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.COMPRESSION_TYPE_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_NO;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAAS_ENABLED;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_HEADERS;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.KERBEROS_ENABLED;
import static net.coru.kloadgen.util.ProducerKeysHelper.LINGER_MS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.RECEIVE_BUFFER_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME;
import static net.coru.kloadgen.util.ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.SASL_MECHANISM;
import static net.coru.kloadgen.util.ProducerKeysHelper.SASL_MECHANISM_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.SEND_BUFFER_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.SSL_ENABLED;
import static net.coru.kloadgen.util.ProducerKeysHelper.TOPIC_NAME_STRATEGY;
import static net.coru.kloadgen.util.ProducerKeysHelper.VALUE_NAME_STRATEGY;
import static net.coru.kloadgen.util.ProducerKeysHelper.ZOOKEEPER_SERVERS;
import static net.coru.kloadgen.util.ProducerKeysHelper.ZOOKEEPER_SERVERS_DEFAULT;
import static net.coru.kloadgen.util.PropsKeysHelper.*;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.loadgen.impl.AvroLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.ProducerKeysHelper;
import net.coru.kloadgen.util.StatelessRandomTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

@Slf4j
public class ConfluentKafkaSampler extends AbstractJavaSamplerClient implements Serializable {

    private static final long serialVersionUID = 1L;

    private KafkaProducer<String, Object> producer;

    private String topic;

    private String msg_key_type;

    private List<String> msg_key_value;

    private boolean key_message_flag = false;

    private final ObjectMapper objectMapperJson = new ObjectMapper();

    private final StatelessRandomTool statelessRandomTool;

    private final BaseLoadGenerator generator;

    public ConfluentKafkaSampler() {
        generator = new AvroLoadGenerator();
        statelessRandomTool = new StatelessRandomTool();
    }

    @Override
    public Arguments getDefaultParameters() {

        Arguments defaultParameters = SamplerUtil.getCommonDefaultParameters();
        defaultParameters.addArgument(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG_DEFAULT);
        return defaultParameters;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {

        Properties props = SamplerUtil.setupCommonProperties(context, generator);

        props.put(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, context.getParameter(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG));

        if (Objects.nonNull(context.getParameter(VALUE_NAME_STRATEGY))) {
            props.put(VALUE_NAME_STRATEGY, context.getParameter(VALUE_NAME_STRATEGY));
        }

        if (FLAG_YES.equals(context.getParameter(KEYED_MESSAGE_KEY))) {
            key_message_flag= true;
            msg_key_type = context.getParameter(MESSAGE_KEY_KEY_TYPE);
            msg_key_value = MSG_KEY_VALUE.equalsIgnoreCase(context.getParameter(MESSAGE_KEY_KEY_VALUE))
                    ? emptyList() : singletonList(context.getParameter(MESSAGE_KEY_KEY_VALUE));
        }

        topic = context.getParameter(KAFKA_TOPIC_CONFIG);
        producer = new KafkaProducer<>(props);
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        JMeterContext jMeterContext = JMeterContextService.getContext();
        EnrichedRecord messageVal = generator.nextMessage();
        List<HeaderMapping> kafkaHeaders = safeGetKafkaHeaders(jMeterContext);
        if (Objects.nonNull(messageVal)) {
            ProducerRecord<String, Object> producerRecord;
            try {
                if (key_message_flag) {
                    String key = statelessRandomTool.generateRandom("key", msg_key_type, 0, msg_key_value).toString();
                    producerRecord = new ProducerRecord<>(topic, key, messageVal.getGenericRecord());
                } else {
                    producerRecord = new ProducerRecord<>(topic, messageVal.getGenericRecord());
                }
                Map<String, String> jsonTypes = new HashMap<>();
                jsonTypes.put("contentType", "java.lang.String");
                producerRecord.headers()
                    .add("spring_json_header_types", objectMapperJson.writeValueAsBytes(jsonTypes));
                List<String> headersSB = new ArrayList<>();
                headersSB.add("spring_json_header_types".concat(objectMapperJson.writeValueAsString(jsonTypes)));
                headersSB.addAll(SamplerUtil.populateHeaders(kafkaHeaders, producerRecord));

                sampleResult.setRequestHeaders(StringUtils.join(headersSB, ","));
                sampleResult.setSamplerData(producerRecord.value().toString());

                Future<RecordMetadata> result = producer.send(producerRecord, (metadata, e) -> {
                    if (e != null) {
                        log.error("Send failed for record {}", producerRecord, e);
                        throw new KLoadGenException("Failed to sent message due ", e);
                    }
                });

                log.info("Send message to body: {}", producerRecord.value());
                sampleResult.setResponseData(prettyPrint(result.get()), StandardCharsets.UTF_8.name());
                sampleResult.setSuccessful(true);
                sampleResult.sampleEnd();

            } catch (Exception e) {
                log.error("Failed to send message", e);
                sampleResult.setResponseData(e.getMessage(), StandardCharsets.UTF_8.name());
                sampleResult.setSuccessful(false);
                sampleResult.sampleEnd();
            }
        } else {
            log.error("Failed to Generate message");
            sampleResult.setResponseData("Failed to Generate message", StandardCharsets.UTF_8.name());
            sampleResult.setSuccessful(false);
            sampleResult.sampleEnd();
        }
        return sampleResult;
    }

    private String prettyPrint(RecordMetadata recordMetadata) {
        String template = "Topic: %s, partition: %s, offset: %s";
        return String.format(template, recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
    }

    private List<HeaderMapping> safeGetKafkaHeaders(JMeterContext jMeterContext) {
        List<HeaderMapping> headerMappingList = new ArrayList<>();
        Object headers = jMeterContext.getSamplerContext().get(KAFKA_HEADERS);
        if (null != headers) {
            headerMappingList.addAll((List) headers);
        }
        return headerMappingList;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        producer.close();
    }
}
