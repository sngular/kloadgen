package com.sngular.kloadgen.schemaregistry.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryConstants;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ParsedSchemaAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

public class ConfluentSchemaRegistry implements SchemaRegistryAdapter {

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

  public final String getSchemaRegistryUrlKey() {
    return AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
  }

  public final void setSchemaRegistryClient(final String url, final Map<String, ?> properties) {
    this.schemaRegistryClient = new CachedSchemaRegistryClient(List.of(JMeterHelper.checkPropertyOrVariable(url)), 1000,
                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);
  }

  public final void setSchemaRegistryClient(final Map<String, ?> properties) {
    final String url = properties.get(this.getSchemaRegistryUrlKey()).toString();
    this.schemaRegistryClient = new CachedSchemaRegistryClient(List.of(JMeterHelper.checkPropertyOrVariable(url)), 1000,
                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);

  }

  public final Collection<String> getAllSubjects() {
    try {
      return new ArrayList<>(this.schemaRegistryClient.getAllSubjects());
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public final BaseSchemaMetadata<ConfluentSchemaMetadata> getLatestSchemaMetadata(final String subjectName) {
    try {
      final SchemaMetadataAdapter schemaMetadataAdapter = ConfluentSchemaMetadata.parse(this.schemaRegistryClient.getLatestSchemaMetadata(subjectName));
      return new BaseSchemaMetadata(schemaMetadataAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public final BaseParsedSchema<ConfluentParsedSchemaMetadata> getSchemaBySubject(final String subjectName) {
    try {
      final ConfluentSchemaMetadata schemaMetadata = ConfluentSchemaMetadata.parse(this.schemaRegistryClient.getLatestSchemaMetadata(subjectName));
      final ParsedSchema parsedSchema = this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId());
      final ParsedSchemaAdapter parsedSchemaAdapter = ConfluentParsedSchemaMetadata.parse(parsedSchema);
      return new BaseParsedSchema(parsedSchemaAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public final BaseParsedSchema<ConfluentParsedSchemaMetadata> getSchemaBySubjectAndId(final String subjectName, final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    try {
      final ParsedSchema parsedSchema = this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, metadata.getSchemaMetadataAdapter().getId());
      final ParsedSchemaAdapter parsedSchemaAdapter = ConfluentParsedSchemaMetadata.parse(parsedSchema);
      return new BaseParsedSchema(parsedSchemaAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

}
