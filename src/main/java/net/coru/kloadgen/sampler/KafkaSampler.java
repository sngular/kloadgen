
package net.coru.kloadgen.sampler;

import static net.coru.kloadgen.util.ProducerKeysHelper.SAMPLE_ENTITY;
import static net.coru.kloadgen.util.ProducerKeysHelper.SASL_MECHANISM;
import static net.coru.kloadgen.util.ProducerKeysHelper.SCHEMA_REGISTRY_URL;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.ProducerKeysHelper;
import net.coru.kloadgen.util.PropsKeysHelper;
import net.coru.kloadgen.util.RandomTool;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

@Slf4j
public class KafkaSampler extends AbstractJavaSamplerClient implements Serializable {

    private KafkaProducer<String, Object> producer;
    private String topic;
    private String msg_key_placeHolder;
    private boolean key_message_flag = false;

    @Override
    public Arguments getDefaultParameters() {

        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ProducerKeysHelper.BOOTSTRAP_SERVERS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerKeysHelper.KAFKA_TOPIC_CONFIG, ProducerKeysHelper.KAFKA_TOPIC_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.COMPRESSION_TYPE_CONFIG, ProducerKeysHelper.COMPRESSION_TYPE_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.BATCH_SIZE_CONFIG, ProducerKeysHelper.BATCH_SIZE_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.LINGER_MS_CONFIG, ProducerKeysHelper.LINGER_MS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.BUFFER_MEMORY_CONFIG, ProducerKeysHelper.BUFFER_MEMORY_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.ACKS_CONFIG, ProducerKeysHelper.ACKS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.SEND_BUFFER_CONFIG, ProducerKeysHelper.SEND_BUFFER_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.RECEIVE_BUFFER_CONFIG, ProducerKeysHelper.RECEIVE_BUFFER_CONFIG_DEFAULT);
        defaultParameters.addArgument(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.name);
        defaultParameters.addArgument(ProducerKeysHelper.KERBEROS_ENABLED, ProducerKeysHelper.FLAG_NO);
        defaultParameters.addArgument(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG, ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG, ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME, ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME_DEFAULT);
        defaultParameters.addArgument(SASL_MECHANISM, ProducerKeysHelper.SASL_MECHANISM_DEFAULT);

        defaultParameters.addArgument(ProducerKeysHelper.SSL_ENABLED, ProducerKeysHelper.FLAG_NO);
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
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "net.coru.kloadgen.serializer.AvroSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "net.coru.kloadgen.serializer.AvroSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, context.getParameter(ProducerConfig.ACKS_CONFIG));
        props.put(ProducerConfig.SEND_BUFFER_CONFIG, context.getParameter(ProducerConfig.SEND_BUFFER_CONFIG));
        props.put(ProducerConfig.RECEIVE_BUFFER_CONFIG, context.getParameter(ProducerConfig.RECEIVE_BUFFER_CONFIG));
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, context.getParameter(ProducerConfig.BATCH_SIZE_CONFIG));
        props.put(ProducerConfig.LINGER_MS_CONFIG, context.getParameter(ProducerConfig.LINGER_MS_CONFIG));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, context.getParameter(ProducerConfig.BUFFER_MEMORY_CONFIG));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, context.getParameter(ProducerConfig.COMPRESSION_TYPE_CONFIG));
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, context.getParameter(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        props.put(SASL_MECHANISM, context.getParameter(SASL_MECHANISM));
        Optional<String> schemaRegistryContext = Optional.of(JMeterContextService.getContext().getVariables().get(SCHEMA_REGISTRY_URL));
        schemaRegistryContext.ifPresent( cont -> props.put(SCHEMA_REGISTRY_URL, cont));
        Iterator<String> parameters = context.getParameterNamesIterator();
        parameters.forEachRemaining(parameter -> {
            if (parameter.startsWith("_")) {
                props.put(parameter.substring(1), context.getParameter(parameter));
            }
        });

        String sslEnabled = context.getParameter(ProducerKeysHelper.SSL_ENABLED);

        if (sslEnabled != null && sslEnabled.equals(ProducerKeysHelper.FLAG_YES)) {

            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        }

        String kerberosEnabled = context.getParameter(ProducerKeysHelper.KERBEROS_ENABLED);
        if (kerberosEnabled != null && kerberosEnabled.equals(ProducerKeysHelper.FLAG_YES)) {
            System.setProperty(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG, context.getParameter(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG));
            System.setProperty(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG, context.getParameter(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG));
            props.put(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME, context.getParameter(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME));
        }

        if ("YES".equals(context.getParameter(PropsKeysHelper.KEYED_MESSAGE_KEY))) {
            key_message_flag= true;
            msg_key_placeHolder = UUID.randomUUID().toString();
        }
        topic = context.getParameter(ProducerKeysHelper.KAFKA_TOPIC_CONFIG);
        producer = new KafkaProducer<>(props);

    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        JMeterContext jMeterContext = JMeterContextService.getContext();
        EnrichedRecord messageVal = (EnrichedRecord)jMeterContext.getVariables().getObject(SAMPLE_ENTITY);
        List<HeaderMapping> kafkaHeaders = (List<HeaderMapping>) jMeterContext.getSamplerContext().get(ProducerKeysHelper.KAFKA_HEADERS);

        ProducerRecord<String, Object> producerRecord;
        try {
            if (key_message_flag) {
                producerRecord = new ProducerRecord<>(topic, msg_key_placeHolder, messageVal);
            } else {
                producerRecord = new ProducerRecord<>(topic, messageVal);
            }

            for (HeaderMapping kafkaHeader : kafkaHeaders) {
                producerRecord.headers().add(kafkaHeader.getHeaderName(),
                    RandomTool.generateRandom(kafkaHeader.getHeaderValue(),
                        10,
                        Collections.emptyList()).toString().getBytes());
            }

            log.info("Send message {}", producerRecord.value());
            Future messageSent = producer.send(producerRecord);
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
