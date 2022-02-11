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
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.emptyList;
import static net.coru.kloadgen.util.ProducerKeysHelper.ACKS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BATCH_SIZE_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BOOTSTRAP_SERVERS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.BUFFER_MEMORY_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.COMPRESSION_TYPE_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM;
import static net.coru.kloadgen.util.ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG;
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
import static net.coru.kloadgen.util.ProducerKeysHelper.KEY_NAME_STRATEGY;
import static net.coru.kloadgen.util.ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT;
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
import static net.coru.kloadgen.util.ProducerKeysHelper.VALUE_SERIALIZER_CLASS_CONFIG_DEFAULT;
import static net.coru.kloadgen.util.ProducerKeysHelper.ZOOKEEPER_SERVERS;
import static net.coru.kloadgen.util.ProducerKeysHelper.ZOOKEEPER_SERVERS_DEFAULT;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_DESERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SUBJECT_NAME;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.MESSAGE_KEY_KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.MSG_KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY;
import static net.coru.kloadgen.util.PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY;
import static net.coru.kloadgen.util.PropsKeysHelper.TIMEOUT_MILLIS;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_DESERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SUBJECT_NAME;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
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
import java.util.Set;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.loadgen.impl.AvroLoadGenerator;
import net.coru.kloadgen.loadgen.impl.JsonLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;
import net.coru.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

public final class SamplerUtil {

  private static final StatelessGeneratorTool statelessGeneratorTool = new StatelessGeneratorTool();

  private static final Set<String> JSON_TYPE_SET = Set.of("json-schema", "json");

  private SamplerUtil() {
  }

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
    defaultParameters.addArgument(KERBEROS_ENABLED, FLAG_NO);
    defaultParameters.addArgument(JAAS_ENABLED, FLAG_NO);
    defaultParameters.addArgument(JAVA_SEC_AUTH_LOGIN_CONFIG, JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
    defaultParameters.addArgument(JAVA_SEC_KRB5_CONFIG, JAVA_SEC_KRB5_CONFIG_DEFAULT);
    defaultParameters.addArgument(SASL_KERBEROS_SERVICE_NAME, SASL_KERBEROS_SERVICE_NAME_DEFAULT);
    defaultParameters.addArgument(SASL_MECHANISM, SASL_MECHANISM_DEFAULT);
    defaultParameters.addArgument(SSL_ENABLED, FLAG_NO);
    defaultParameters.addArgument(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "<Key Password>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "<Keystore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "<Keystore Password>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "<Truststore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "<Truststore Password>");

    defaultParameters.addArgument(ProducerConfig.CLIENT_ID_CONFIG, "");
    defaultParameters.addArgument(ProducerConfig.SECURITY_PROVIDERS_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, SslConfigs.DEFAULT_SSL_ENABLED_PROTOCOLS);
    defaultParameters.addArgument(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "<Ssl identification algorithm>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_KEYMANGER_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE);
    defaultParameters.addArgument(SslConfigs.SSL_PROVIDER_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, SslConfigs.DEFAULT_SSL_PROTOCOL);
    defaultParameters.addArgument(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, "false");

    return defaultParameters;
  }

  public static Properties setupCommonProperties(JavaSamplerContext context) {
    Properties props = new Properties();

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    if ("true".equals(context.getJMeterVariables().get(SCHEMA_KEYED_MESSAGE_KEY))) {
      props.put(KEY_SERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(KEY_SERIALIZER_CLASS_PROPERTY));
    } else if ("true".equals(context.getJMeterVariables().get(SIMPLE_KEYED_MESSAGE_KEY))) {
      props.put(MESSAGE_KEY_KEY_TYPE, context.getJMeterVariables().get(KEY_TYPE));
      props.put(MESSAGE_KEY_KEY_VALUE, context.getJMeterVariables().get(KEY_VALUE));
      props.put(KEY_SERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(KEY_SERIALIZER_CLASS_PROPERTY));
    } else {
      props.put(SCHEMA_KEYED_MESSAGE_KEY, Boolean.FALSE);
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

    if (Objects.nonNull(context.getParameter(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG))) {
      props.put(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG,
          context.getParameter(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG));
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

  public static Arguments getCommonConsumerDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG_DEFAULT);
    defaultParameters.addArgument(ZOOKEEPER_SERVERS, ZOOKEEPER_SERVERS_DEFAULT);
    defaultParameters.addArgument(KAFKA_TOPIC_CONFIG, KAFKA_TOPIC_CONFIG_DEFAULT);
    defaultParameters.addArgument(ConsumerConfig.SEND_BUFFER_CONFIG, SEND_BUFFER_CONFIG_DEFAULT);
    defaultParameters.addArgument(ConsumerConfig.RECEIVE_BUFFER_CONFIG, RECEIVE_BUFFER_CONFIG_DEFAULT);
    defaultParameters.addArgument(AUTO_OFFSET_RESET_CONFIG, "earliest");
    defaultParameters.addArgument(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.name);
    defaultParameters.addArgument(MESSAGE_KEY_KEY_TYPE, MSG_KEY_TYPE);
    defaultParameters.addArgument(KERBEROS_ENABLED, FLAG_NO);
    defaultParameters.addArgument(JAAS_ENABLED, FLAG_NO);
    defaultParameters.addArgument(JAVA_SEC_AUTH_LOGIN_CONFIG, JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
    defaultParameters.addArgument(JAVA_SEC_KRB5_CONFIG, JAVA_SEC_KRB5_CONFIG_DEFAULT);
    defaultParameters.addArgument(SASL_KERBEROS_SERVICE_NAME, SASL_KERBEROS_SERVICE_NAME_DEFAULT);
    defaultParameters.addArgument(SASL_MECHANISM, SASL_MECHANISM_DEFAULT);
    defaultParameters.addArgument(VALUE_NAME_STRATEGY, TOPIC_NAME_STRATEGY);
    defaultParameters.addArgument(KEY_NAME_STRATEGY, TOPIC_NAME_STRATEGY);
    defaultParameters.addArgument(SSL_ENABLED, FLAG_NO);
    defaultParameters.addArgument(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "<Key Password>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "<Keystore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "<Keystore Password>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "<Truststore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "<Truststore Password>");

    defaultParameters.addArgument(ConsumerConfig.CLIENT_ID_CONFIG, "");
    defaultParameters.addArgument(ConsumerConfig.SECURITY_PROVIDERS_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, SslConfigs.DEFAULT_SSL_ENABLED_PROTOCOLS);
    defaultParameters.addArgument(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
        SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_KEYMANGER_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE);
    defaultParameters.addArgument(SslConfigs.SSL_PROVIDER_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, SslConfigs.DEFAULT_SSL_PROTOCOL);
    defaultParameters.addArgument(TIMEOUT_MILLIS, "5000");
    defaultParameters.addArgument(MAX_POLL_INTERVAL_MS_CONFIG, "3000");
    defaultParameters.addArgument(ConsumerConfig.GROUP_ID_CONFIG, "anonymous");
    return defaultParameters;
  }

  public static void setupConsumerDeserializerProperties(JavaSamplerContext context, Properties props) {
    if (Objects.nonNull(context.getJMeterVariables().get(KEY_DESERIALIZER_CLASS_PROPERTY))) {
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(KEY_DESERIALIZER_CLASS_PROPERTY));
    } else {
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    }
    if (Objects.nonNull(context.getJMeterVariables().get(VALUE_DESERIALIZER_CLASS_PROPERTY))) {
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(VALUE_DESERIALIZER_CLASS_PROPERTY));
    } else {
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    }
  }

  public static void setupConsumerSchemaRegistryProperties(JavaSamplerContext context, Properties props) {
    Map<String, String> originals = new HashMap<>();
    setupSchemaRegistryAuthenticationProperties(context.getJMeterVariables(), originals);
    props.putAll(originals);

    if (Objects.nonNull(context.getJMeterVariables().get(VALUE_NAME_STRATEGY))) {
      props.put(VALUE_NAME_STRATEGY, context.getJMeterVariables().get(VALUE_NAME_STRATEGY));
    }
    if (Objects.nonNull(context.getJMeterVariables().get(KEY_NAME_STRATEGY))) {
      props.put(KEY_NAME_STRATEGY, context.getJMeterVariables().get(KEY_NAME_STRATEGY));
    }
  }

  private static void setupSchemaRegistryAuthenticationProperties(JMeterVariables context, Map<String, String> props) {
    if (Objects.nonNull(context.get(SCHEMA_REGISTRY_URL))) {
      props.put(SCHEMA_REGISTRY_URL, context.get(SCHEMA_REGISTRY_URL));

      if (FLAG_YES.equals(context.get(SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equals(context.get(SCHEMA_REGISTRY_AUTH_KEY))) {
          props.put(BASIC_AUTH_CREDENTIALS_SOURCE, context.get(BASIC_AUTH_CREDENTIALS_SOURCE));
          props.put(USER_INFO_CONFIG, context.get(USER_INFO_CONFIG));
        } else {
          props.put(BEARER_AUTH_CREDENTIALS_SOURCE, context.get(BEARER_AUTH_CREDENTIALS_SOURCE));
          props.put(BEARER_AUTH_TOKEN_CONFIG, context.get(BEARER_AUTH_TOKEN_CONFIG));
        }
      }
    }
  }

  public static Properties setupCommonConsumerProperties(JavaSamplerContext context) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

    setupConsumerDeserializerProperties(context, props);
    setupConsumerSchemaRegistryProperties(context, props);

    props.put(ConsumerConfig.SEND_BUFFER_CONFIG, context.getParameter(ConsumerConfig.SEND_BUFFER_CONFIG));
    props.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, context.getParameter(ConsumerConfig.RECEIVE_BUFFER_CONFIG));
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, context.getParameter(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    props.put(SASL_MECHANISM, context.getParameter(SASL_MECHANISM));

    props.put(KAFKA_TOPIC_CONFIG, context.getParameter(KAFKA_TOPIC_CONFIG));
    props.put(GROUP_ID_CONFIG, context.getParameter(GROUP_ID_CONFIG));

    props.put(CLIENT_ID_CONFIG, context.getParameter(CLIENT_ID_CONFIG));

    if (Objects.nonNull(context.getJMeterVariables().get(VALUE_SCHEMA))) {
      props.put(VALUE_SCHEMA, context.getJMeterVariables().get(VALUE_SCHEMA));
    }
    if (Objects.nonNull(context.getJMeterVariables().get(KEY_SCHEMA))) {
      props.put(KEY_SCHEMA, context.getJMeterVariables().get(KEY_SCHEMA));
    }

    props.put(AUTO_OFFSET_RESET_CONFIG, context.getParameter(AUTO_OFFSET_RESET_CONFIG));
    props.put(TIMEOUT_MILLIS, context.getParameter(TIMEOUT_MILLIS));

    Iterator<String> parameters = context.getParameterNamesIterator();
    parameters.forEachRemaining(parameter -> {
      if (parameter.startsWith("_")) {
        props.put(parameter.substring(1), context.getParameter(parameter));
      }
    });

    verifySecurity(context, props);

    props.put(MAX_POLL_INTERVAL_MS_CONFIG, context.getParameter(MAX_POLL_INTERVAL_MS_CONFIG));
    return props;
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
        propertyOrDefault(context.getParameter(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG),
            DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
            ""));

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

  public static BaseLoadGenerator configureValueGenerator(Properties props) {
    JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
    BaseLoadGenerator generator;

    String valueNameStrategy = jMeterVariables.get(VALUE_NAME_STRATEGY);

    if (Objects.isNull(valueNameStrategy)) {
      props.put(VALUE_NAME_STRATEGY, TOPIC_NAME_STRATEGY);
    } else {
      props.put(VALUE_NAME_STRATEGY, valueNameStrategy);
    }

    if (Objects.nonNull(jMeterVariables.get(VALUE_SCHEMA_TYPE))) {
      if (JSON_TYPE_SET.contains(jMeterVariables.get(VALUE_SCHEMA_TYPE).toLowerCase())) {
        generator = new JsonLoadGenerator();
      } else if (jMeterVariables.get(VALUE_SCHEMA_TYPE).equalsIgnoreCase("avro")) {
        generator = new AvroLoadGenerator();
      } else {
        throw new KLoadGenException("Unsupported Serializer");
      }
    } else {
      generator = new AvroLoadGenerator();
    }

    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        Objects.requireNonNullElse(jMeterVariables.get(VALUE_SERIALIZER_CLASS_PROPERTY), VALUE_SERIALIZER_CLASS_CONFIG_DEFAULT));

    if (Objects.nonNull(jMeterVariables.get(SCHEMA_REGISTRY_URL))) {
      Map<String, String> originals = new HashMap<>();
      setupSchemaRegistryAuthenticationProperties(jMeterVariables, originals);

      props.putAll(originals);

      try {
        generator.setUpGenerator(
            originals,
            jMeterVariables.get(VALUE_SUBJECT_NAME),
            (List<FieldValueMapping>) jMeterVariables.getObject(VALUE_SCHEMA_PROPERTIES));
      } catch (KLoadGenException exc) {
        if (Objects.nonNull(props.get(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG))) {
          generator.setUpGenerator(
              jMeterVariables.get(VALUE_SCHEMA),
              (List<FieldValueMapping>) jMeterVariables.getObject(VALUE_SCHEMA_PROPERTIES));
        } else {
          throw exc;
        }
      }
    } else {
      generator.setUpGenerator(
          jMeterVariables.get(VALUE_SCHEMA),
          (List<FieldValueMapping>) jMeterVariables.getObject(VALUE_SCHEMA_PROPERTIES));
    }

    return generator;
  }

  public static BaseLoadGenerator configureKeyGenerator(Properties props) {
    JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
    BaseLoadGenerator generator;

    String keyNameStrategy = jMeterVariables.get(KEY_NAME_STRATEGY);

    if (Objects.isNull(keyNameStrategy)) {
      props.put(KEY_NAME_STRATEGY, TOPIC_NAME_STRATEGY);
    } else {
      props.put(KEY_NAME_STRATEGY, keyNameStrategy);
    }

    if (Objects.nonNull(jMeterVariables.get(KEY_SCHEMA_TYPE))) {
      if (JSON_TYPE_SET.contains(jMeterVariables.get(KEY_SCHEMA_TYPE).toLowerCase())) {
        generator = new JsonLoadGenerator();
      } else if (jMeterVariables.get(KEY_SCHEMA_TYPE).equalsIgnoreCase("avro")) {
        generator = new AvroLoadGenerator();
      } else {
        throw new KLoadGenException("Unsupported Serializer");
      }
    } else {
      generator = new AvroLoadGenerator();
    }

    props.put(KEY_SERIALIZER_CLASS_CONFIG,
        Objects.requireNonNullElse(jMeterVariables.get(VALUE_SERIALIZER_CLASS_PROPERTY), KEY_SERIALIZER_CLASS_CONFIG_DEFAULT));

    if (Objects.nonNull(jMeterVariables.get(SCHEMA_REGISTRY_URL))) {
      Map<String, String> originals = new HashMap<>();
      originals.put(SCHEMA_REGISTRY_URL_CONFIG, jMeterVariables.get(SCHEMA_REGISTRY_URL));

      if (FLAG_YES.equals(jMeterVariables.get(SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equals(jMeterVariables.get(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BASIC_AUTH_CREDENTIALS_SOURCE, jMeterVariables.get(BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(USER_INFO_CONFIG, jMeterVariables.get(USER_INFO_CONFIG));
        } else {
          originals.put(BEARER_AUTH_CREDENTIALS_SOURCE, jMeterVariables.get(BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(BEARER_AUTH_TOKEN_CONFIG, jMeterVariables.get(BEARER_AUTH_TOKEN_CONFIG));
        }
      }

      props.putAll(originals);

      generator.setUpGenerator(
          originals,
          jMeterVariables.get(KEY_SUBJECT_NAME),
          (List<FieldValueMapping>) jMeterVariables.getObject(KEY_SCHEMA_PROPERTIES));
    } else {
      generator.setUpGenerator(
          jMeterVariables.get(KEY_SCHEMA),
          (List<FieldValueMapping>) jMeterVariables.getObject(KEY_SCHEMA_PROPERTIES));
    }

    return generator;
  }

  public static List<String> populateHeaders(List<HeaderMapping> kafkaHeaders, ProducerRecord<Object, Object> producerRecord) {
    List<String> headersSB = new ArrayList<>();
    for (HeaderMapping kafkaHeader : kafkaHeaders) {
      String headerValue = statelessGeneratorTool.generateObject(kafkaHeader.getHeaderName(), kafkaHeader.getHeaderValue(),
          10,
          emptyList()).toString();
      headersSB.add(kafkaHeader.getHeaderName().concat(":").concat(headerValue));
      producerRecord.headers().add(kafkaHeader.getHeaderName(), headerValue.getBytes(StandardCharsets.UTF_8));
    }
    return headersSB;
  }

  private static String propertyOrDefault(String property, String defaultToken, String valueToSent) {
    return defaultToken.equals(property) ? valueToSent : property;
  }
}
