package com.sngular.kloadgen.sampler.schemaregistry.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryConstants;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryManager;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.jmeter.threads.JMeterContextService;

public class ConfluentSchemaRegistry implements SchemaRegistryManager {

  private SchemaRegistryClient schemaRegistryClient;

  private Map<String, String> propertiesMap;

  public ConfluentSchemaRegistry() {
    this.propertiesMap = Map.of(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryConstants.SCHEMA_REGISTRY_CONFLUENT,
                                SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL_KEY, AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                                SchemaRegistryConstants.BASIC_AUTH_CREDENTIALS, AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE,
                                SchemaRegistryConstants.USER_INFO_CONFIG, AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG,
                                SchemaRegistryConstants.BEARER_AUTH_CREDENTIALS, AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CREDENTIALS_SOURCE,
                                SchemaRegistryConstants.BEARER_AUTH_TOKEN_CONFIG, AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_TOKEN_CONFIG);
  }

  @Override
  public String getSchemaRegistryUrlKey() {
    return AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
  }

  @Override
  public void setSchemaRegistryClient(String url, Map<String, ?> properties) {
    this.schemaRegistryClient = new CachedSchemaRegistryClient(List.of(checkPropertyOrVariable(url)), 1000,
                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);
  }

  @Override
  public void setSchemaRegistryClient(Map<String, ?> properties) {
    String url = properties.get(this.getSchemaRegistryUrlKey()).toString();
    this.schemaRegistryClient = new CachedSchemaRegistryClient(List.of(checkPropertyOrVariable(url)), 1000,
                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);

  }

  @Override
  public Map<String, String> getPropertiesMap() {
    return propertiesMap;
  }

  @Override
  public Collection<String> getAllSubjects() {
    try {
      return new ArrayList<>(this.schemaRegistryClient.getAllSubjects());
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  @Override
  public Object getLatestSchemaMetadata(String subjectName) {
    try {
      return this.schemaRegistryClient.getLatestSchemaMetadata(subjectName);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public Object getSchemaBySubject(String subjectName) {
    final SchemaMetadata schemaMetadata;
    try {
      schemaMetadata = this.schemaRegistryClient.getLatestSchemaMetadata(subjectName);
      return this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId());
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public Object getSchemaBySubjectAndId(String subjectName, Object metadata) {
    final SchemaMetadata schemaMetadata = (SchemaMetadata) metadata;
    try {
      return this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId());
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  private String checkPropertyOrVariable(final String textToCheck) {
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
