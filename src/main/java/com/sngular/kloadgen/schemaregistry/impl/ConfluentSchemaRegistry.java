package com.sngular.kloadgen.schemaregistry.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.AbstractParsedSchemaAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConfluentSchemaRegistry implements SchemaRegistryAdapter {

  private SchemaRegistryClient schemaRegistryClient;

  @Override
  public String getSchemaRegistryUrlKey() {
    return AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
  }

  @Override
  public void setSchemaRegistryClient(final String url, final Map<String, ?> properties) {
    this.schemaRegistryClient = (SchemaRegistryClient) SchemaRegistryFactory.getSchemaRegistryClient(SchemaRegistryEnum.CONFLUENT, url, properties);
  }

  @Override
  public void setSchemaRegistryClient(final Map<String, ?> properties) {
    final String url = properties.get(this.getSchemaRegistryUrlKey()).toString();
    this.schemaRegistryClient = (SchemaRegistryClient) SchemaRegistryFactory.getSchemaRegistryClient(SchemaRegistryEnum.CONFLUENT, url, properties);

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
      final ParsedSchema parsedSchema = new ParsedSchema(this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId()));
      final AbstractParsedSchemaAdapter abstractParsedSchemaAdapter = ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema);
      return new BaseParsedSchema(abstractParsedSchemaAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  public BaseParsedSchema<ConfluentAbstractParsedSchemaMetadata> getSchemaBySubjectAndId(final String subjectName,
      final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    try {
      final ParsedSchema parsedSchema = new ParsedSchema(this.schemaRegistryClient.getSchemaBySubjectAndId(subjectName, metadata.getSchemaMetadataAdapter().getId()));
      final AbstractParsedSchemaAdapter abstractParsedSchemaAdapter = ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema);
      return new BaseParsedSchema(abstractParsedSchemaAdapter);
    } catch (RestClientException | IOException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }
}
