package net.coru.kloadgen.sampler;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.emptyList;
import static net.coru.kloadgen.util.ProducerKeysHelper.ACKS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BATCH_SIZE_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BOOTSTRAP_SERVERS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BUFFER_MEMORY_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.COMPRESSION_TYPE_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_NO;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAAS_ENABLED;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG_DEFAULT;
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
import static net.coru.kloadgen.util.PropsKeysHelper.AVRO_SCHEMA;
import static net.coru.kloadgen.util.PropsKeysHelper.AVRO_SUBJECT_NAME;
import static net.coru.kloadgen.util.PropsKeysHelper.KEYED_MESSAGE_DEFAULT;
import static net.coru.kloadgen.util.PropsKeysHelper.KEYED_MESSAGE_KEY;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.MSG_KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.MSG_KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.loadgen.impl.AvroLoadGenerator;
import net.coru.kloadgen.loadgen.impl.JsonLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.util.ProducerKeysHelper;
import net.coru.kloadgen.util.StatelessRandomTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

public final class SamplerUtil {

  private static final StatelessRandomTool statelessRandomTool = new StatelessRandomTool();

  private SamplerUtil() { }

  public static Arguments getCommonDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG_DEFAULT);
    defaultParameters.addArgument(ZOOKEEPER_SERVERS, ZOOKEEPER_SERVERS_DEFAULT);
    defaultParameters.addArgument(KAFKA_TOPIC_CONFIG, KAFKA_TOPIC_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.LINGER_MS_CONFIG, LINGER_MS_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.BUFFER_MEMORY_CONFIG, BUFFER_MEMORY_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.ACKS_CONFIG, ACKS_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.SEND_BUFFER_CONFIG, SEND_BUFFER_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerConfig.RECEIVE_BUFFER_CONFIG, RECEIVE_BUFFER_CONFIG_DEFAULT);
    defaultParameters.addArgument(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.name);
    defaultParameters.addArgument(KEYED_MESSAGE_KEY, KEYED_MESSAGE_DEFAULT);
    defaultParameters.addArgument(MESSAGE_KEY_KEY_TYPE, MSG_KEY_TYPE);
    defaultParameters.addArgument(MESSAGE_KEY_KEY_VALUE, MSG_KEY_VALUE);
    defaultParameters.addArgument(KERBEROS_ENABLED, FLAG_NO);
    defaultParameters.addArgument(JAAS_ENABLED, FLAG_NO);
    defaultParameters.addArgument(JAVA_SEC_AUTH_LOGIN_CONFIG, JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
    defaultParameters.addArgument(JAVA_SEC_KRB5_CONFIG, JAVA_SEC_KRB5_CONFIG_DEFAULT);
    defaultParameters.addArgument(SASL_KERBEROS_SERVICE_NAME, SASL_KERBEROS_SERVICE_NAME_DEFAULT);
    defaultParameters.addArgument(SASL_MECHANISM, SASL_MECHANISM_DEFAULT);
    defaultParameters.addArgument(VALUE_NAME_STRATEGY, TOPIC_NAME_STRATEGY);
    defaultParameters.addArgument(SSL_ENABLED, FLAG_NO);
    defaultParameters.addArgument(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "<Key Password>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "<Keystore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "<Keystore Password>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "<Truststore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "<Truststore Password>");

    defaultParameters.addArgument(ProducerConfig.CLIENT_ID_CONFIG, "");
    defaultParameters.addArgument(ProducerConfig.SECURITY_PROVIDERS_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, SslConfigs.DEFAULT_SSL_ENABLED_PROTOCOLS);
    defaultParameters.addArgument(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_KEYMANGER_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE);
    defaultParameters.addArgument(SslConfigs.SSL_PROVIDER_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, SslConfigs.DEFAULT_SSL_PROTOCOL);
    defaultParameters.addArgument(ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, "false");

    return defaultParameters;
  }

  public static Properties setupCommonProperties(JavaSamplerContext context) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    if (Objects.nonNull(context.getParameter(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))) {
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, context.getParameter(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
    }
    if (Objects.nonNull(context.getParameter(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))) {
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, context.getParameter(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }
    props.put(ProducerConfig.ACKS_CONFIG, context.getParameter(ProducerConfig.ACKS_CONFIG));
    props.put(ProducerConfig.SEND_BUFFER_CONFIG, context.getParameter(ProducerConfig.SEND_BUFFER_CONFIG));
    props.put(ProducerConfig.RECEIVE_BUFFER_CONFIG, context.getParameter(ProducerConfig.RECEIVE_BUFFER_CONFIG));
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, context.getParameter(ProducerConfig.BATCH_SIZE_CONFIG));
    props.put(ProducerConfig.LINGER_MS_CONFIG, context.getParameter(ProducerConfig.LINGER_MS_CONFIG));
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, context.getParameter(ProducerConfig.BUFFER_MEMORY_CONFIG));
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, context.getParameter(ProducerConfig.COMPRESSION_TYPE_CONFIG));
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, context.getParameter(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    props.put(SASL_MECHANISM, context.getParameter(SASL_MECHANISM));


    if (Objects.nonNull(context.getParameter(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG))) {
      props.put(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, context.getParameter(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG));
    }
    Iterator<String> parameters = context.getParameterNamesIterator();
    parameters.forEachRemaining(parameter -> {
      if (parameter.startsWith("_")) {
        props.put(parameter.substring(1), context.getParameter(parameter));
      }
    });

    verifySecurity(context, props);

    return props;
  }

  public static BaseLoadGenerator configureGenerator(JavaSamplerContext context, Properties props) {
   return configSchemRegistryUrl(context, props);
  }

  private static void verifySecurity(JavaSamplerContext context, Properties props) {
    if (FLAG_YES.equalsIgnoreCase(context.getParameter(SSL_ENABLED))) {

      props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
      props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
      props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
      props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
      props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
    }

    if (FLAG_YES.equalsIgnoreCase(context.getParameter(KERBEROS_ENABLED))) {
      System.setProperty(JAVA_SEC_AUTH_LOGIN_CONFIG, context.getParameter(JAVA_SEC_AUTH_LOGIN_CONFIG));
      System.setProperty(JAVA_SEC_KRB5_CONFIG, context.getParameter(JAVA_SEC_KRB5_CONFIG));
      props.put(SASL_KERBEROS_SERVICE_NAME, context.getParameter(SASL_KERBEROS_SERVICE_NAME));
    }

    if (FLAG_YES.equalsIgnoreCase(context.getParameter(JAAS_ENABLED))) {
      if (StringUtils.contains(context.getParameter(JAVA_SEC_AUTH_LOGIN_CONFIG), File.separatorChar)) {
        System.setProperty(JAVA_SEC_AUTH_LOGIN_CONFIG, context.getParameter(JAVA_SEC_AUTH_LOGIN_CONFIG));
      } else {
        props.put(SASL_JAAS_CONFIG, context.getParameter(JAVA_SEC_AUTH_LOGIN_CONFIG));
      }
    }

    props.put(ProducerConfig.CLIENT_ID_CONFIG, context.getParameter(ProducerConfig.CLIENT_ID_CONFIG));

    props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, context.getParameter(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG));
    props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
        context.getParameter(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
    props.put(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG, context.getParameter(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG));
    props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG));
    props.put(SslConfigs.SSL_PROTOCOL_CONFIG, context.getParameter(SslConfigs.SSL_PROTOCOL_CONFIG));

    if (!StringUtils.isBlank(context.getParameter(ProducerConfig.SECURITY_PROVIDERS_CONFIG).trim())) {
      props.put(ProducerConfig.SECURITY_PROVIDERS_CONFIG, context.getParameter(ProducerConfig.SECURITY_PROVIDERS_CONFIG));
    }

    if (!StringUtils.isBlank(context.getParameter(SslConfigs.SSL_PROVIDER_CONFIG).trim())) {
      props.put(SslConfigs.SSL_PROVIDER_CONFIG, context.getParameter(SslConfigs.SSL_PROVIDER_CONFIG));
    }
  }

  private static BaseLoadGenerator configSchemRegistryUrl(JavaSamplerContext context, Properties props) {
    JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
    BaseLoadGenerator generator;

    if (Objects.nonNull(context.getParameter(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) &&
        context.getParameter(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).toLowerCase().contains("json")) {
      generator = new JsonLoadGenerator();
    } else if (Objects.isNull(context.getParameter(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) || context.getParameter(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).toLowerCase().contains("avro")) {
      generator = new AvroLoadGenerator();
    } else {
      throw new KLoadGenException("Unsupported Serializer");
    }

    if (Objects.nonNull(jMeterVariables.get(SCHEMA_REGISTRY_URL))) {
      Map<String, String> originals = new HashMap<>();
      originals.put(SCHEMA_REGISTRY_URL_CONFIG, JMeterContextService.getContext().getVariables().get(SCHEMA_REGISTRY_URL));

      if (FLAG_YES.equals(jMeterVariables.get(SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE
            .equals(jMeterVariables.get(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BASIC_AUTH_CREDENTIALS_SOURCE,
              jMeterVariables.get(BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(USER_INFO_CONFIG, jMeterVariables.get(USER_INFO_CONFIG));
        } else {
          originals.put(BEARER_AUTH_CREDENTIALS_SOURCE,
              jMeterVariables.get(BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(BEARER_AUTH_TOKEN_CONFIG, jMeterVariables.get(BEARER_AUTH_TOKEN_CONFIG));
        }
      }
      props.putAll(originals);

      generator.setUpGenerator(
          originals,
          JMeterContextService.getContext().getVariables().get(AVRO_SUBJECT_NAME),
          (List<FieldValueMapping>) jMeterVariables.getObject(SCHEMA_PROPERTIES));
    } else {
      generator.setUpGenerator(
          JMeterContextService.getContext().getVariables().get(AVRO_SCHEMA),
          (List<FieldValueMapping>) jMeterVariables.getObject(SCHEMA_PROPERTIES));
    }

    return generator;
  }

  public static List<String> populateHeaders(List<HeaderMapping> kafkaHeaders, ProducerRecord<String, Object> producerRecord) {
    List<String> headersSB = new ArrayList<>();
    for (HeaderMapping kafkaHeader : kafkaHeaders) {
      String headerValue = statelessRandomTool.generateRandom(kafkaHeader.getHeaderName(), kafkaHeader.getHeaderValue(),
          10,
          emptyList()).toString();
      headersSB.add(kafkaHeader.getHeaderName().concat(":").concat(headerValue));
      producerRecord.headers().add(kafkaHeader.getHeaderName(), headerValue.getBytes(StandardCharsets.UTF_8));
    }
    return headersSB;
  }
}
