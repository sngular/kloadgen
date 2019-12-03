
package net.coru.kloadgen.sampler;

import static net.coru.kloadgen.util.ProducerKeys.SAMPLE_ENTITY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.ProducerKeys;
import net.coru.kloadgen.util.PropsKeys;
import net.coru.kloadgen.util.RandomTool;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

@Slf4j
public class ConfluentKafkaSampler extends AbstractJavaSamplerClient implements Serializable {

    private KafkaProducer<String, Object> producer;
    private String topic;
    private String msg_key_placeHolder;
    private boolean key_message_flag = false;

    ObjectMapper objectMapperJson = new ObjectMapper();

    @Override
    public Arguments getDefaultParameters() {

        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ProducerKeys.BOOTSTRAP_SERVERS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerKeys.ZOOKEEPER_SERVERS, ProducerKeys.ZOOKEEPER_SERVERS_DEFAULT);
        defaultParameters.addArgument(ProducerKeys.KAFKA_TOPIC_CONFIG, ProducerKeys.KAFKA_TOPIC_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.COMPRESSION_TYPE_CONFIG, ProducerKeys.COMPRESSION_TYPE_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.BATCH_SIZE_CONFIG, ProducerKeys.BATCH_SIZE_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.LINGER_MS_CONFIG, ProducerKeys.LINGER_MS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.BUFFER_MEMORY_CONFIG, ProducerKeys.BUFFER_MEMORY_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.ACKS_CONFIG, ProducerKeys.ACKS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.SEND_BUFFER_CONFIG, ProducerKeys.SEND_BUFFER_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.RECEIVE_BUFFER_CONFIG, ProducerKeys.RECEIVE_BUFFER_CONFIG_DEFAULT);
        defaultParameters.addArgument(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.name);
        defaultParameters.addArgument(PropsKeys.KEYED_MESSAGE_KEY, PropsKeys.KEYED_MESSAGE_DEFAULT);
        defaultParameters.addArgument(PropsKeys.MESSAGE_KEY_PLACEHOLDER_KEY, PropsKeys.MSG_KEY_PLACEHOLDER);
        defaultParameters.addArgument(PropsKeys.MESSAGE_VAL_PLACEHOLDER_KEY, PropsKeys.MSG_PLACEHOLDER);
        defaultParameters.addArgument(ProducerKeys.KERBEROS_ENABLED, ProducerKeys.FLAG_NO);
        defaultParameters.addArgument(ProducerKeys.JAVA_SEC_AUTH_LOGIN_CONFIG, ProducerKeys.JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerKeys.JAVA_SEC_KRB5_CONFIG, ProducerKeys.JAVA_SEC_KRB5_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerKeys.SASL_KERBEROS_SERVICE_NAME, ProducerKeys.SASL_KERBEROS_SERVICE_NAME_DEFAULT);
        defaultParameters.addArgument(ProducerKeys.SASL_MECHANISM, ProducerKeys.SASL_MECHANISM_DEFAULT);
        defaultParameters.addArgument(ProducerKeys.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, ProducerKeys.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG_DEFAULT);

        defaultParameters.addArgument(ProducerKeys.SSL_ENABLED, ProducerKeys.FLAG_NO);
        defaultParameters.addArgument(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "<Key Password>");
        defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "<Keystore Location>");
        defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "<Keystore Password>");
        defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "<Truststore Location>");
        defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "<Truststore Password>");

        return defaultParameters;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, context.getParameter(ProducerConfig.ACKS_CONFIG));
        props.put(ProducerConfig.SEND_BUFFER_CONFIG, context.getParameter(ProducerConfig.SEND_BUFFER_CONFIG));
        props.put(ProducerConfig.RECEIVE_BUFFER_CONFIG, context.getParameter(ProducerConfig.RECEIVE_BUFFER_CONFIG));
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, context.getParameter(ProducerConfig.BATCH_SIZE_CONFIG));
        props.put(ProducerConfig.LINGER_MS_CONFIG, context.getParameter(ProducerConfig.LINGER_MS_CONFIG));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, context.getParameter(ProducerConfig.BUFFER_MEMORY_CONFIG));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, context.getParameter(ProducerConfig.COMPRESSION_TYPE_CONFIG));
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, context.getParameter(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        props.put(ProducerKeys.SASL_MECHANISM, context.getParameter(ProducerKeys.SASL_MECHANISM));
        props.put(ProducerKeys.SCHEMA_REGISTRY_URL, JMeterUtils.getProperty("schemaUrl"));
        props.put(ProducerKeys.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, context.getParameter(ProducerKeys.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG));

        Iterator<String> parameters = context.getParameterNamesIterator();
        parameters.forEachRemaining(parameter -> {
            if (parameter.startsWith("_")) {
                props.put(parameter.substring(1), context.getParameter(parameter));
            }
        });

        String sslEnabled = context.getParameter(ProducerKeys.SSL_ENABLED);

        if (sslEnabled != null && sslEnabled.equals(ProducerKeys.FLAG_YES)) {

            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        }

        String kerberosEnabled = context.getParameter(ProducerKeys.KERBEROS_ENABLED);
        if (kerberosEnabled != null && kerberosEnabled.equals(ProducerKeys.FLAG_YES)) {
            System.setProperty(ProducerKeys.JAVA_SEC_AUTH_LOGIN_CONFIG, context.getParameter(ProducerKeys.JAVA_SEC_AUTH_LOGIN_CONFIG));
            System.setProperty(ProducerKeys.JAVA_SEC_KRB5_CONFIG, context.getParameter(ProducerKeys.JAVA_SEC_KRB5_CONFIG));
            props.put(ProducerKeys.SASL_KERBEROS_SERVICE_NAME, context.getParameter(ProducerKeys.SASL_KERBEROS_SERVICE_NAME));
        }

        if ("YES".equals(context.getParameter(PropsKeys.KEYED_MESSAGE_KEY))) {
            key_message_flag= true;
            msg_key_placeHolder = UUID.randomUUID().toString();
        }
        topic = context.getParameter(ProducerKeys.KAFKA_TOPIC_CONFIG);
        producer = new KafkaProducer<>(props);

    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        JMeterContext jMeterContext = JMeterContextService.getContext();
        EnrichedRecord messageVal = (EnrichedRecord)jMeterContext.getVariables().getObject(SAMPLE_ENTITY);
        List<FieldValueMapping> kafkaHeaders = (List<FieldValueMapping>) jMeterContext.getSamplerContext().get(ProducerKeys.KAFKA_HEADERS);

        ProducerRecord<String, Object> producerRecord;
        try {
            if (key_message_flag) {
                producerRecord = new ProducerRecord<>(topic, msg_key_placeHolder, messageVal.getGenericRecord());
            } else {
                producerRecord = new ProducerRecord<>(topic, messageVal.getGenericRecord());
            }
            Map<String, String> jsonTypes = new HashMap<>();
            jsonTypes.put("itx_deprecation_date", "java.lang.String");
            jsonTypes.put("contentType", "java.lang.String");
            producerRecord.headers()
                .add("spring_json_header_types", objectMapperJson.writeValueAsBytes(jsonTypes));
            for (FieldValueMapping kafkaHeader : kafkaHeaders) {
                producerRecord.headers().add(kafkaHeader.getFieldName(),
                    objectMapperJson.writeValueAsBytes(RandomTool.generateRandom(kafkaHeader.getValueExpression(),
                        10, Collections.emptyList())));
            }

            log.info("Send message {}", producerRecord.value());
            Future<RecordMetadata> messageSent = producer.send(producerRecord);
            producer.flush();
            if (!messageSent.isDone()) {
                throw new IOException("Message not sent");
            }
            sampleResult.setResponseData(messageVal.toString(), StandardCharsets.UTF_8.name());
            sampleResult.setSuccessful(true);
            sampleResult.sampleEnd();

        } catch (Exception e) {
            log.error("Failed to send message", e);
            sampleResult.setResponseData(e.getMessage(), StandardCharsets.UTF_8.name());
            sampleResult.setSuccessful(false);
            sampleResult.sampleEnd();
        }
        return sampleResult;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        producer.close();
    }
}
