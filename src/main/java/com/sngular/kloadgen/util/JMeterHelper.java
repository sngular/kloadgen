package com.sngular.kloadgen.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryManager;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryManagerFactory;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class JMeterHelper {

  private JMeterHelper() {
  }

  public static Object getParsedSchema(final String subjectName, final Properties properties) {
    final Map<String, String> originals = new HashMap<>();

    String schemaRegistryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    final SchemaRegistryManager schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(schemaRegistryName);

    if (StringUtils.isNotEmpty(schemaRegistryName)) {
      originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), properties.getProperty(schemaRegistryManager.getSchemaRegistryUrlKey()));

      if (ProducerKeysHelper.FLAG_YES.equals(properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE
            .equals(properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE,
                        properties.getProperty(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(SchemaRegistryClientConfig.USER_INFO_CONFIG, properties.getProperty(SchemaRegistryClientConfig.USER_INFO_CONFIG));
        } else if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY
            .equals(properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE,
                        properties.getProperty(SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG, properties.getProperty(SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG));
        }
      }
    }
    return schemaRegistryManager.getSchemaBySubject(subjectName);
  }

  public static String checkPropertyOrVariable(final String textToCheck) {
    final String result;
    if (textToCheck.matches("\\$\\{__P\\(.*\\)}")) {
      result = JMeterContextService.getContext().getProperties().getProperty(textToCheck.substring(6, textToCheck.length() - 2));
    } else if (textToCheck.matches("\\$\\{\\w*}")) {
      result = JMeterContextService.getContext().getVariables().get(textToCheck.substring(2, textToCheck.length() - 1));
    } else {
      result = textToCheck;
    }
    return result;
  }
}
