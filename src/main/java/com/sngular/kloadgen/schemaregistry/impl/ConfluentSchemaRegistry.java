package com.sngular.kloadgen.schemaregistry.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.AbstractParsedSchemaAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.threads.JMeterContextService;

@Slf4j
public final class ConfluentSchemaRegistry implements SchemaRegistryAdapter {

  private SchemaRegistryClient schemaRegistryClient;

  @Override
  public String getSchemaRegistryUrlKey() {
    return AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
  }

  @Override
  public void setSchemaRegistryClient(final String url, final Map<String, ?> properties) {
    log.debug("CREATEION");
    this.schemaRegistryClient = new CachedSchemaRegistryClient(List.of(checkPropertyOrVariable(url)), 1000,
                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);
  }

  @Override
  public void setSchemaRegistryClient(final Map<String, ?> properties) {
    log.debug("CREATITIN");
    final String url = properties.get(this.getSchemaRegistryUrlKey()).toString();
    this.schemaRegistryClient = new CachedSchemaRegistryClient(List.of(checkPropertyOrVariable(url)), 1000,
                                                               List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);

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
  public BaseSchemaMetadata<ConfluentSchemaMetadata> getLatestSchemaMetadata(final String subjectName) {
    try {
      final SchemaMetadataAdapter schemaMetadataAdapter = ConfluentSchemaMetadata.parse(this.schemaRegistryClient.getLatestSchemaMetadata(subjectName));
      return new BaseSchemaMetadata(schemaMetadataAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public BaseParsedSchema<ConfluentAbstractParsedSchemaMetadata> getSchemaBySubject(final String subjectName) {
    try {
      final ConfluentSchemaMetadata schemaMetadata = ConfluentSchemaMetadata.parse(this.schemaRegistryClient.getLatestSchemaMetadata(subjectName));
      final ParsedSchema parsedSchema = this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId());
      final AbstractParsedSchemaAdapter abstractParsedSchemaAdapter = ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema);
      return new BaseParsedSchema(abstractParsedSchemaAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public BaseParsedSchema<ConfluentAbstractParsedSchemaMetadata> getSchemaBySubjectAndId(final String subjectName,
      final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    try {
      final ParsedSchema parsedSchema = this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, metadata.getSchemaMetadataAdapter().getId());
      final AbstractParsedSchemaAdapter abstractParsedSchemaAdapter = ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema);
      return new BaseParsedSchema(abstractParsedSchemaAdapter);
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
