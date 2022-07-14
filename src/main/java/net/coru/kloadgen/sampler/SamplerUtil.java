/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.loadgen.impl.AvroSRLoadGenerator;
import net.coru.kloadgen.loadgen.impl.JsonSRLoadGenerator;
import net.coru.kloadgen.loadgen.impl.ProtobufLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;
import net.coru.kloadgen.util.ProducerKeysHelper;
import net.coru.kloadgen.util.PropsKeysHelper;
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
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

public final class SamplerUtil {

  private static final StatelessGeneratorTool STATELESS_GENERATOR_TOOL = new StatelessGeneratorTool();

  private static final Set<String> JSON_TYPE_SET = Set.of("json-schema", "json");

  private SamplerUtil() {
  }

  @SuppressWarnings("checkstyle:ExecutableStatementCount")
  public static Arguments getCommonDefaultParameters() {
    final Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ProducerKeysHelper.BOOTSTRAP_SERVERS_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.ZOOKEEPER_SERVERS, ProducerKeysHelper.ZOOKEEPER_SERVERS_DEFAULT);
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
    defaultParameters.addArgument(ProducerKeysHelper.JAAS_ENABLED, ProducerKeysHelper.FLAG_NO);
    defaultParameters.addArgument(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG, ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG, ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME, ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.SASL_MECHANISM, ProducerKeysHelper.SASL_MECHANISM_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.SSL_ENABLED, ProducerKeysHelper.FLAG_NO);
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
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE);
    defaultParameters.addArgument(SslConfigs.SSL_PROVIDER_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, SslConfigs.DEFAULT_SSL_PROTOCOL);
    defaultParameters.addArgument(ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, "false");

    return defaultParameters;
  }

  public static Properties setupCommonProperties(final JavaSamplerContext context) {
    final Properties props = new Properties();

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    if ("true".equals(context.getJMeterVariables().get(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY))) {
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY));
    } else if ("true".equals(context.getJMeterVariables().get(PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY))) {
      props.put(PropsKeysHelper.MESSAGE_KEY_KEY_TYPE, context.getJMeterVariables().get(PropsKeysHelper.KEY_TYPE));
      props.put(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE, context.getJMeterVariables().get(PropsKeysHelper.KEY_VALUE));
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY));
    } else {
      props.put(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY, Boolean.FALSE);
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
    props.put(ProducerKeysHelper.SASL_MECHANISM, context.getParameter(ProducerKeysHelper.SASL_MECHANISM));

    if (Objects.nonNull(context.getParameter(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG))) {
      props.put(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG,
                context.getParameter(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG));
    }
    final Iterator<String> parameters = context.getParameterNamesIterator();
    parameters.forEachRemaining(parameter -> {
      if (parameter.startsWith("_")) {
        props.put(parameter.substring(1), context.getParameter(parameter));
      }
    });

    verifySecurity(context, props);

    return props;
  }

  private static String propertyOrDefault(final String property, final String defaultToken, final String valueToSent) {
    return defaultToken.equals(property) ? valueToSent : property;
  }

  @SuppressWarnings("checkstyle:ExecutableStatementCount")
  public static Arguments getCommonConsumerDefaultParameters() {
    final Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ProducerKeysHelper.BOOTSTRAP_SERVERS_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.ZOOKEEPER_SERVERS, ProducerKeysHelper.ZOOKEEPER_SERVERS_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.KAFKA_TOPIC_CONFIG, ProducerKeysHelper.KAFKA_TOPIC_CONFIG_DEFAULT);
    defaultParameters.addArgument(ConsumerConfig.SEND_BUFFER_CONFIG, ProducerKeysHelper.SEND_BUFFER_CONFIG_DEFAULT);
    defaultParameters.addArgument(ConsumerConfig.RECEIVE_BUFFER_CONFIG, ProducerKeysHelper.RECEIVE_BUFFER_CONFIG_DEFAULT);
    defaultParameters.addArgument(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    defaultParameters.addArgument(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.name);
    defaultParameters.addArgument(PropsKeysHelper.MESSAGE_KEY_KEY_TYPE, PropsKeysHelper.MSG_KEY_TYPE);
    defaultParameters.addArgument(ProducerKeysHelper.KERBEROS_ENABLED, ProducerKeysHelper.FLAG_NO);
    defaultParameters.addArgument(ProducerKeysHelper.JAAS_ENABLED, ProducerKeysHelper.FLAG_NO);
    defaultParameters.addArgument(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG, ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG, ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME, ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.SASL_MECHANISM, ProducerKeysHelper.SASL_MECHANISM_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.VALUE_NAME_STRATEGY, ProducerKeysHelper.TOPIC_NAME_STRATEGY);
    defaultParameters.addArgument(ProducerKeysHelper.KEY_NAME_STRATEGY, ProducerKeysHelper.TOPIC_NAME_STRATEGY);
    defaultParameters.addArgument(ProducerKeysHelper.SSL_ENABLED, ProducerKeysHelper.FLAG_NO);
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
    defaultParameters.addArgument(PropsKeysHelper.TIMEOUT_MILLIS, "5000");
    defaultParameters.addArgument(CommonClientConfigs.MAX_POLL_INTERVAL_MS_CONFIG, "3000");
    defaultParameters.addArgument(ConsumerConfig.GROUP_ID_CONFIG, "anonymous");
    return defaultParameters;
  }

  public static void setupConsumerDeserializerProperties(final JavaSamplerContext context, final Properties props) {
    if (Objects.nonNull(context.getJMeterVariables().get(PropsKeysHelper.KEY_DESERIALIZER_CLASS_PROPERTY))) {
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(PropsKeysHelper.KEY_DESERIALIZER_CLASS_PROPERTY));
    } else {
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    }
    if (Objects.nonNull(context.getJMeterVariables().get(PropsKeysHelper.VALUE_DESERIALIZER_CLASS_PROPERTY))) {
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(PropsKeysHelper.VALUE_DESERIALIZER_CLASS_PROPERTY));
    } else {
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    }
  }

  public static void setupConsumerSchemaRegistryProperties(final JavaSamplerContext context, final Properties props) {
    final Map<String, String> originals = new HashMap<>();
    setupSchemaRegistryAuthenticationProperties(context.getJMeterVariables(), originals);
    props.putAll(originals);

    if (Objects.nonNull(context.getJMeterVariables().get(ProducerKeysHelper.VALUE_NAME_STRATEGY))) {
      props.put(ProducerKeysHelper.VALUE_NAME_STRATEGY, context.getJMeterVariables().get(ProducerKeysHelper.VALUE_NAME_STRATEGY));
    }
    if (Objects.nonNull(context.getJMeterVariables().get(ProducerKeysHelper.KEY_NAME_STRATEGY))) {
      props.put(ProducerKeysHelper.KEY_NAME_STRATEGY, context.getJMeterVariables().get(ProducerKeysHelper.KEY_NAME_STRATEGY));
    }
  }

  private static void setupSchemaRegistryAuthenticationProperties(final JMeterVariables context, final Map<String, String> props) {
    if (Objects.nonNull(context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL))) {
      props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL));

      if (ProducerKeysHelper.FLAG_YES.equals(context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equals(context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          props.put(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, context.get(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE));
          props.put(SchemaRegistryClientConfig.USER_INFO_CONFIG, context.get(SchemaRegistryClientConfig.USER_INFO_CONFIG));
        } else {
          props.put(SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE, context.get(SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE));
          props.put(SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG, context.get(SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG));
        }
      }
    }
  }

  public static Properties setupCommonConsumerProperties(final JavaSamplerContext context) {
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

    setupConsumerDeserializerProperties(context, props);
    setupConsumerSchemaRegistryProperties(context, props);

    props.put(ConsumerConfig.SEND_BUFFER_CONFIG, context.getParameter(ConsumerConfig.SEND_BUFFER_CONFIG));
    props.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, context.getParameter(ConsumerConfig.RECEIVE_BUFFER_CONFIG));
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, context.getParameter(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    props.put(ProducerKeysHelper.SASL_MECHANISM, context.getParameter(ProducerKeysHelper.SASL_MECHANISM));

    props.put(ProducerKeysHelper.KAFKA_TOPIC_CONFIG, context.getParameter(ProducerKeysHelper.KAFKA_TOPIC_CONFIG));
    props.put(CommonClientConfigs.GROUP_ID_CONFIG, context.getParameter(CommonClientConfigs.GROUP_ID_CONFIG));

    props.put(CommonClientConfigs.CLIENT_ID_CONFIG, context.getParameter(CommonClientConfigs.CLIENT_ID_CONFIG));

    if (Objects.nonNull(context.getJMeterVariables().get(PropsKeysHelper.VALUE_SCHEMA))) {
      props.put(PropsKeysHelper.VALUE_SCHEMA, context.getJMeterVariables().get(PropsKeysHelper.VALUE_SCHEMA));
    }
    if (Objects.nonNull(context.getJMeterVariables().get(PropsKeysHelper.KEY_SCHEMA))) {
      props.put(PropsKeysHelper.KEY_SCHEMA, context.getJMeterVariables().get(PropsKeysHelper.KEY_SCHEMA));
    }

    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, context.getParameter(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
    props.put(PropsKeysHelper.TIMEOUT_MILLIS, context.getParameter(PropsKeysHelper.TIMEOUT_MILLIS));

    final Iterator<String> parameters = context.getParameterNamesIterator();
    parameters.forEachRemaining(parameter -> {
      if (parameter.startsWith("_")) {
        props.put(parameter.substring(1), context.getParameter(parameter));
      }
    });

    verifySecurity(context, props);

    props.put(CommonClientConfigs.MAX_POLL_INTERVAL_MS_CONFIG, context.getParameter(CommonClientConfigs.MAX_POLL_INTERVAL_MS_CONFIG));
    return props;
  }

  private static void verifySecurity(final JavaSamplerContext context, final Properties props) {
    if (ProducerKeysHelper.FLAG_YES.equalsIgnoreCase(context.getParameter(ProducerKeysHelper.SSL_ENABLED))) {

      props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
      props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
      props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
      props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
      props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, context.getParameter(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
    }

    if (ProducerKeysHelper.FLAG_YES.equalsIgnoreCase(context.getParameter(ProducerKeysHelper.KERBEROS_ENABLED))) {
      System.setProperty(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG, context.getParameter(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG));
      System.setProperty(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG, context.getParameter(ProducerKeysHelper.JAVA_SEC_KRB5_CONFIG));
      props.put(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME, context.getParameter(ProducerKeysHelper.SASL_KERBEROS_SERVICE_NAME));
    }

    if (ProducerKeysHelper.FLAG_YES.equalsIgnoreCase(context.getParameter(ProducerKeysHelper.JAAS_ENABLED))) {
      if (StringUtils.contains(context.getParameter(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG), File.separatorChar)) {
        System.setProperty(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG, context.getParameter(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG));
      } else {
        props.put(SaslConfigs.SASL_JAAS_CONFIG, context.getParameter(ProducerKeysHelper.JAVA_SEC_AUTH_LOGIN_CONFIG));
      }
    }

    props.put(ProducerConfig.CLIENT_ID_CONFIG, context.getParameter(ProducerConfig.CLIENT_ID_CONFIG));

    props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, context.getParameter(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG));

    props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
              propertyOrDefault(context.getParameter(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG),
                                ProducerKeysHelper.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
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

  public static BaseLoadGenerator configureValueGenerator(final Properties props) {
    final JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
    final BaseLoadGenerator generator;

    final String valueNameStrategy = jMeterVariables.get(ProducerKeysHelper.VALUE_NAME_STRATEGY);

    if (Objects.isNull(valueNameStrategy)) {
      props.put(ProducerKeysHelper.VALUE_NAME_STRATEGY, ProducerKeysHelper.TOPIC_NAME_STRATEGY);
    } else {
      props.put(ProducerKeysHelper.VALUE_NAME_STRATEGY, valueNameStrategy);
    }

    if (Objects.nonNull(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_TYPE))) {
      if (JSON_TYPE_SET.contains(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_TYPE).toLowerCase())) {
        generator = new JsonSRLoadGenerator();
      } else if (jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_TYPE).equalsIgnoreCase("avro")) {
        generator = new AvroSRLoadGenerator();
      } else if (jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_TYPE).equalsIgnoreCase("Protobuf")) {
        generator = new ProtobufLoadGenerator();
      } else {
        throw new KLoadGenException("Unsupported Serializer");
      }
    } else {
      generator = new AvroSRLoadGenerator();
    }

    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
              Objects.requireNonNullElse(jMeterVariables.get(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY), ProducerKeysHelper.VALUE_SERIALIZER_CLASS_CONFIG_DEFAULT));

    if (Objects.nonNull(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL))) {
      final Map<String, String> originals = new HashMap<>();
      setupSchemaRegistryAuthenticationProperties(jMeterVariables, originals);

      props.putAll(originals);

      try {
        generator.setUpGenerator(
            originals,
            jMeterVariables.get(PropsKeysHelper.VALUE_SUBJECT_NAME),
            (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
      } catch (final KLoadGenException exc) {
        if (Objects.nonNull(props.get(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG))) {
          generator.setUpGenerator(
              jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA),
              (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
        } else {
          throw exc;
        }
      }
    } else {
      generator.setUpGenerator(
          jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA),
          (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
    }

    return generator;
  }

  public static BaseLoadGenerator configureKeyGenerator(final Properties props) {
    final JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
    final BaseLoadGenerator generator;

    final String keyNameStrategy = jMeterVariables.get(ProducerKeysHelper.KEY_NAME_STRATEGY);

    if (Objects.isNull(keyNameStrategy)) {
      props.put(ProducerKeysHelper.KEY_NAME_STRATEGY, ProducerKeysHelper.TOPIC_NAME_STRATEGY);
    } else {
      props.put(ProducerKeysHelper.KEY_NAME_STRATEGY, keyNameStrategy);
    }

    if (Objects.nonNull(jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA_TYPE))) {
      if (JSON_TYPE_SET.contains(jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA_TYPE).toLowerCase())) {
        generator = new JsonSRLoadGenerator();
      } else if (jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA_TYPE).equalsIgnoreCase("avro")) {
        generator = new AvroSRLoadGenerator();
      } else if (jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA_TYPE).equalsIgnoreCase("Protobuf")) {
        generator = new ProtobufLoadGenerator();
      } else {
        throw new KLoadGenException("Unsupported Serializer");
      }
    } else {
      generator = new AvroSRLoadGenerator();
    }

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
              Objects.requireNonNullElse(jMeterVariables.get(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY), ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT));

    if (Objects.nonNull(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL))) {
      final Map<String, String> originals = new HashMap<>();
      originals.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL));

      if (ProducerKeysHelper.FLAG_YES.equals(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equals(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, jMeterVariables.get(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(SchemaRegistryClientConfig.USER_INFO_CONFIG, jMeterVariables.get(SchemaRegistryClientConfig.USER_INFO_CONFIG));
        } else {
          originals.put(SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE, jMeterVariables.get(SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG, jMeterVariables.get(SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG));
        }
      }

      props.putAll(originals);

      generator.setUpGenerator(
          originals,
          jMeterVariables.get(PropsKeysHelper.KEY_SUBJECT_NAME),
          (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES));
    } else {
      generator.setUpGenerator(
          jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA),
          (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES));
    }

    return generator;
  }

  public static List<String> populateHeaders(final List<HeaderMapping> kafkaHeaders, final ProducerRecord<Object, Object> producerRecord) {
    final List<String> headersSB = new ArrayList<>();
    for (HeaderMapping kafkaHeader : kafkaHeaders) {
      final String headerValue = STATELESS_GENERATOR_TOOL.generateObject(kafkaHeader.getHeaderName(), kafkaHeader.getHeaderValue(),
                                                                   10,
                                                                   Collections.emptyList()).toString();
      headersSB.add(kafkaHeader.getHeaderName().concat(":").concat(headerValue));
      producerRecord.headers().add(kafkaHeader.getHeaderName(), headerValue.getBytes(StandardCharsets.UTF_8));
    }
    return headersSB;
  }
}
