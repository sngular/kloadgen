/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.sampler;

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

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.loadgen.impl.AvroSRLoadGenerator;
import com.sngular.kloadgen.loadgen.impl.JsonSRLoadGenerator;
import com.sngular.kloadgen.loadgen.impl.PlainTextLoadGenerator;
import com.sngular.kloadgen.loadgen.impl.ProtobufLoadGenerator;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.model.HeaderMapping;
import com.sngular.kloadgen.randomtool.generator.StatelessGeneratorTool;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.apicurio.registry.resolver.SchemaResolverConfig;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import org.apache.avro.SchemaParseException;
import org.apache.commons.lang3.ObjectUtils;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SamplerUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SamplerUtil.class);

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
    defaultParameters.addArgument(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG_DEFAULT);
    defaultParameters.addArgument(ProducerKeysHelper.APICURIO_LEGACY_ID_HANDLER, ProducerKeysHelper.FLAG_NO);
    defaultParameters.addArgument(ProducerKeysHelper.APICURIO_ENABLE_HEADERS_ID, ProducerKeysHelper.FLAG_YES);
    return defaultParameters;
  }

  /*  public static Properties setupCommonProperties(final JavaSamplerContext context) {
      final Properties props = new Properties();

      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
      if ("true".equals(context.getJMeterVariables().get(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY))) {
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, context.getJMeterVariables().get(PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY));
      } else if ("true".equals(context.getJMeterVariables().get(PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY))) {
        props.put(PropsKeysHelper.MESSAGE_KEY_KEY_TYPE, context.getJMeterVariables().get(PropsKeysHelper.KEY_TYPE));
        props.put(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE, context.getJMeterVariables().get(PropsKeysHelper.KEY_VALUE));
        if (Objects.nonNull(context.getJMeterVariables().get(PropsKeysHelper.KEY_SCHEMA_TYPE))) {
          props.put(PropsKeysHelper.KEY_SCHEMA_TYPE, context.getJMeterVariables().get(PropsKeysHelper.KEY_SCHEMA_TYPE));
        }
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

      final String schemaRegistryNameValue = context.getJMeterVariables().get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
      final String enableSchemaRegistrationValue = context.getParameter(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG);
      if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_APICURIO.equalsIgnoreCase(schemaRegistryNameValue)) {
        props.put(SerdeConfig.AUTO_REGISTER_ARTIFACT, enableSchemaRegistrationValue);
        props.put(SchemaResolverConfig.REGISTRY_URL, context.getJMeterVariables().get(SchemaResolverConfig.REGISTRY_URL));
        props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, context.getJMeterVariables().get(SchemaResolverConfig.REGISTRY_URL));
      } else {
        props.put(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, enableSchemaRegistrationValue);
        final String schemaRegistryURL = context.getJMeterVariables().get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL);
        if (StringUtils.isNotBlank(schemaRegistryURL)) {
          props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, schemaRegistryURL);
        }
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

   */

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
    defaultParameters.addArgument(ProducerKeysHelper.SSL_ENABLED, ProducerKeysHelper.FLAG_NO);
    defaultParameters.addArgument(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "<Key Password>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "<Keystore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "<Keystore Password>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "<Truststore Location>");
    defaultParameters.addArgument(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "<Truststore Password>");

    defaultParameters.addArgument(ConsumerConfig.CLIENT_ID_CONFIG, "");
    defaultParameters.addArgument(ConsumerConfig.SECURITY_PROVIDERS_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, SslConfigs.DEFAULT_SSL_ENABLED_PROTOCOLS);
    defaultParameters.addArgument(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_KEYMANGER_ALGORITHM);
    defaultParameters.addArgument(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE);
    defaultParameters.addArgument(SslConfigs.SSL_PROVIDER_CONFIG, "");
    defaultParameters.addArgument(SslConfigs.SSL_PROTOCOL_CONFIG, SslConfigs.DEFAULT_SSL_PROTOCOL);
    defaultParameters.addArgument(PropsKeysHelper.TIMEOUT_MILLIS, "5000");
    defaultParameters.addArgument(ConsumerConfig.GROUP_ID_CONFIG, "anonymous");
    defaultParameters.addArgument(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
    defaultParameters.addArgument(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "57671680");
    defaultParameters.addArgument(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500");
    defaultParameters.addArgument(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
    defaultParameters.addArgument(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
    defaultParameters.addArgument(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "45000");
    defaultParameters.addArgument(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
    defaultParameters.addArgument(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "50");
    defaultParameters.addArgument(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "1000");

    return defaultParameters;
  }

  public static void setupConsumerDeserializerProperties(final Properties props, final JavaSamplerContext context) {
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

  public static void setupConsumerSchemaRegistryProperties(final Properties props, final JavaSamplerContext context) {
    final Map<String, String> originals = setupSchemaRegistryAuthenticationProperties(context.getJMeterVariables());
    props.putAll(originals);

    if (Objects.nonNull(context.getJMeterVariables().get(ProducerKeysHelper.VALUE_NAME_STRATEGY))) {
      props.put(ProducerKeysHelper.VALUE_NAME_STRATEGY, context.getJMeterVariables().get(ProducerKeysHelper.VALUE_NAME_STRATEGY));
    }
    if (Objects.nonNull(context.getJMeterVariables().get(ProducerKeysHelper.KEY_NAME_STRATEGY))) {
      props.put(ProducerKeysHelper.KEY_NAME_STRATEGY, context.getJMeterVariables().get(ProducerKeysHelper.KEY_NAME_STRATEGY));
    }
  }

  static Map<String, String> setupSchemaRegistryAuthenticationProperties(final JMeterVariables context) {
    final Map<String, String> props = new HashMap<>();
    if (Objects.nonNull(context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME))) {

      final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME));
      props.put(schemaRegistryManager.getSchemaRegistryUrlKey(), context.get(schemaRegistryManager.getSchemaRegistryUrlKey()));
      props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, context.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME));

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
    return props;
  }

  public static Properties setupCommonConsumerProperties(final JavaSamplerContext context) {
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

    setupConsumerDeserializerProperties(props, context);
    setupConsumerSchemaRegistryProperties(props, context);

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
    if (Objects.nonNull(context.getJMeterVariables().get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL))) {
      props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, context.getJMeterVariables().get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL));
    }

    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, context.getParameter(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
    props.put(PropsKeysHelper.TIMEOUT_MILLIS, context.getParameter(PropsKeysHelper.TIMEOUT_MILLIS));

    props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, context.getParameter(ConsumerConfig.FETCH_MIN_BYTES_CONFIG));
    props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, context.getParameter(ConsumerConfig.FETCH_MAX_BYTES_CONFIG));
    props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, context.getParameter(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG));
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, context.getParameter(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, context.getParameter(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, context.getParameter(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
    props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, context.getParameter(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG));
    props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, context.getParameter(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG));
    props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, context.getParameter(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG));

    final Iterator<String> parameters = context.getParameterNamesIterator();
    parameters.forEachRemaining(parameter -> {
      if (parameter.startsWith("_")) {
        props.put(parameter.substring(1), context.getParameter(parameter));
      }
    });

    verifySecurity(context, props);

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
              propertyOrDefault(context.getParameter(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG), ProducerKeysHelper.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
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

    if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_APICURIO.equalsIgnoreCase(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME))) {
      props.put(SchemaResolverConfig.ARTIFACT_RESOLVER_STRATEGY, Objects.nonNull(valueNameStrategy) ? valueNameStrategy : ProducerKeysHelper.TOPIC_NAME_STRATEGY_APICURIO);
    } else if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_CONFLUENT.equalsIgnoreCase(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME))) {
      props.put(ProducerKeysHelper.VALUE_NAME_STRATEGY, Objects.nonNull(valueNameStrategy) ? valueNameStrategy : ProducerKeysHelper.TOPIC_NAME_STRATEGY_CONFLUENT);
    }

    if (ObjectUtils.isNotEmpty(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_TYPE))) {
      generator = getBaseLoadGenerator(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_TYPE));
    } else {
      throw new KLoadGenException("Unsupported Serializer");
    }

    if (generator.getClass().equals(PlainTextLoadGenerator.class)) {
      final List<FieldValueMapping> list = new ArrayList<>();
      list.add(FieldValueMapping.builder().fieldName(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES)).build());
      props.put(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, list);
    }

    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
              Objects.requireNonNullElse(jMeterVariables.get(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY), ProducerKeysHelper.VALUE_SERIALIZER_CLASS_CONFIG_DEFAULT));

    if (Objects.nonNull(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME))) {
      final Map<String, String> originals = setupSchemaRegistryAuthenticationProperties(jMeterVariables);

      props.putAll(originals);

      try {
        generator.setUpGenerator(originals, jMeterVariables.get(PropsKeysHelper.VALUE_SUBJECT_NAME),
                                 (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
      } catch (final KLoadGenException exc) {
        if (Objects.nonNull(props.get(SchemaRegistryKeyHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG))) {
          generator.setUpGenerator(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA), (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
        } else {
          LOG.error(exc.getMessage(), exc);
          throw exc;
        }
      }
    } else {
      try {
        generator.setUpGenerator(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA), (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
      } catch (final SchemaParseException exc) {
        generator.setUpGenerator(jMeterVariables.get(PropsKeysHelper.VALUE_SCHEMA), (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES));
      }
    }

    return generator;
  }

  public static BaseLoadGenerator configureKeyGenerator(final Properties props) {
    final JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
    final BaseLoadGenerator generator;

    final String keyNameStrategy = ProducerKeysHelper.KEY_NAME_STRATEGY;
    final String keyNameStrategyValue = jMeterVariables.get(keyNameStrategy);
    if (Objects.isNull(keyNameStrategyValue)) {
      final String schemaRegistryNameValue = jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
      if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_APICURIO.equalsIgnoreCase(schemaRegistryNameValue)) {
        props.put(keyNameStrategy, ProducerKeysHelper.TOPIC_NAME_STRATEGY_APICURIO);
      } else if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_CONFLUENT.equalsIgnoreCase(schemaRegistryNameValue)) {
        props.put(keyNameStrategy, ProducerKeysHelper.TOPIC_NAME_STRATEGY_CONFLUENT);
      }
    } else {
      props.put(keyNameStrategy, keyNameStrategyValue);
    }

    if (ObjectUtils.isNotEmpty(jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA_TYPE))) {
      generator = getBaseLoadGenerator(jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA_TYPE));
    } else {
      throw new KLoadGenException("Unsupported Serializer");
    }

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
              Objects.requireNonNullElse(jMeterVariables.get(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY), ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT));

    if (Objects.nonNull(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME))) {
      final Map<String, String> originals = new HashMap<>();
      final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(jMeterVariables.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME));
      originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), jMeterVariables.get(schemaRegistryManager.getSchemaRegistryUrlKey()));

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

      generator.setUpGenerator(originals, jMeterVariables.get(PropsKeysHelper.KEY_SUBJECT_NAME),
                               (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES));
    } else {
      generator.setUpGenerator(jMeterVariables.get(PropsKeysHelper.KEY_SCHEMA), (List<FieldValueMapping>) jMeterVariables.getObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES));
    }

    return generator;
  }

  @NotNull
  private static BaseLoadGenerator getBaseLoadGenerator(final String schemaType) {
    final BaseLoadGenerator generator;
    if (JSON_TYPE_SET.contains(schemaType.toLowerCase())) {
      generator = new JsonSRLoadGenerator();
    } else if ("avro".equalsIgnoreCase(schemaType)) {
      generator = new AvroSRLoadGenerator();
    } else if ("protobuf".equalsIgnoreCase(schemaType)) {
      generator = new ProtobufLoadGenerator();
    } else if ("noSchema".equalsIgnoreCase(schemaType)) {
      generator = new PlainTextLoadGenerator();
    } else {
      throw new KLoadGenException("Unsupported Serializer");
    }
    return generator;
  }

  public static List<String> populateHeaders(final List<HeaderMapping> kafkaHeaders, final ProducerRecord<Object, Object> producerRecord) {
    final List<String> headersSB = new ArrayList<>();
    for (final HeaderMapping kafkaHeader : kafkaHeaders) {
      final String headerValue = STATELESS_GENERATOR_TOOL.generateObject(kafkaHeader.getHeaderName(), kafkaHeader.getHeaderValue(), 10, Collections.emptyList()).toString();
      headersSB.add(kafkaHeader.getHeaderName().concat(":").concat(headerValue));
      producerRecord.headers().add(kafkaHeader.getHeaderName(), headerValue.getBytes(StandardCharsets.UTF_8));
    }
    return headersSB;
  }

}
