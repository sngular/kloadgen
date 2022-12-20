package com.sngular.kloadgen.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.jmeter.threads.JMeterContextService;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class JMeterHelper {

  private JMeterHelper() {
  }

  public static ParsedSchema getParsedSchema(final String subjectName, final Properties properties) throws RestClientException, IOException {
    final Map<String, String> originals = new HashMap<>();

    if (Objects.nonNull(properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL))) {
      originals.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL));

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

    final SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(List.of(originals.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG)), 1000,
                                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()),
                                                                               originals);

    final SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(subjectName);
    return schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId());
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
