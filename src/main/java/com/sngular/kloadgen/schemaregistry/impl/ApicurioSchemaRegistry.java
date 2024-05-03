package com.sngular.kloadgen.schemaregistry.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.protobuf.Message;
import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import io.apicurio.registry.resolver.SchemaParser;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.exception.RestClientException;
import io.apicurio.registry.rest.v2.beans.ArtifactMetaData;
import io.apicurio.registry.rest.v2.beans.SearchedArtifact;
import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.avro.AvroSchemaParser;
import io.apicurio.registry.serde.jsonschema.JsonSchema;
import io.apicurio.registry.serde.jsonschema.JsonSchemaParser;
import io.apicurio.registry.serde.protobuf.ProtobufSchemaParser;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import org.apache.avro.Schema;

public final class ApicurioSchemaRegistry implements SchemaRegistryAdapter {

  private RegistryClient schemaRegistryClient;

  public String getSchemaRegistryUrlKey() {
    return SerdeConfig.REGISTRY_URL;
  }

  @Override
  public void setSchemaRegistryClient(final String url, final Map<String, ?> properties) {
    this.schemaRegistryClient = (RegistryClient) SchemaRegistryFactory.getSchemaRegistryClient(SchemaRegistryEnum.APICURIO, url, properties);
  }

  @Override
  public void setSchemaRegistryClient(final Map<String, ?> properties) {
    final String url = Objects.toString(properties.get(this.getSchemaRegistryUrlKey()), "");
    this.schemaRegistryClient = (RegistryClient) SchemaRegistryFactory.getSchemaRegistryClient(SchemaRegistryEnum.APICURIO, url, properties);
  }

  @Override
  public Collection<String> getAllSubjects() throws KLoadGenException {
    final Collection<String> subjects = new ArrayList<>();
    try {
      final List<SearchedArtifact> artifacts = this.schemaRegistryClient.searchArtifacts(null, null, null,
                                                                                         null, null, null, null, null, null).getArtifacts();

      for (final SearchedArtifact searchedArtifact : artifacts) {
        subjects.add(searchedArtifact.getId());
      }
      return subjects;
    } catch (final RestClientException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  @Override
  public BaseSchemaMetadata<ApicurioSchemaMetadata> getLatestSchemaMetadata(final String artifactId) throws KLoadGenException {
    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(artifactId);
      final ArtifactMetaData artifactMetaData = this.schemaRegistryClient.getArtifactMetaData(searchedArtifact.getGroupId(), searchedArtifact.getId());
      return new BaseSchemaMetadata<>(new ApicurioSchemaMetadata(artifactMetaData));
    } catch (final RestClientException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  @Override
  public BaseParsedSchema<ApicurioAbstractParsedSchemaMetadata> getSchemaBySubject(final String artifactId) {
    final ApicurioAbstractParsedSchemaMetadata schema = new ApicurioAbstractParsedSchemaMetadata();
    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(artifactId);
      final InputStream inputStream = this.schemaRegistryClient.getLatestArtifact(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final String searchedArtifactType = searchedArtifact.getType();
      setSchemaBySchemaType(schema, inputStream.readAllBytes(), searchedArtifactType);
      schema.setType(searchedArtifactType);
      return new BaseParsedSchema<>(schema);
    } catch (final IOException e) {
      throw new KLoadGenException(e);
    }
  }

  private static void setSchemaBySchemaType(final ApicurioAbstractParsedSchemaMetadata schema, final byte[] result, final String searchedArtifactType) {

    switch (SchemaTypeEnum.valueOf(searchedArtifactType)) {
      case AVRO:
        final SchemaParser<Schema, Object> parserAvro = new AvroSchemaParser<>(null);
        schema.setSchema(parserAvro.parseSchema(result, new HashMap<>()));
        break;
      case PROTOBUF:
        final SchemaParser<ProtobufSchema, Message> parserProtobuf = new ProtobufSchemaParser<>();
        schema.setSchema(parserProtobuf.parseSchema(result, new HashMap<>()));
        break;
      case JSON:
        final SchemaParser<JsonSchema, Object> parserJson = new JsonSchemaParser<>();
        schema.setSchema(parserJson.parseSchema(result, new HashMap<>()));
        break;
      default:
        throw new KLoadGenException(String.format("Schema type not supported %s", searchedArtifactType));

    }
  }

  @Override
  public final BaseParsedSchema<ApicurioAbstractParsedSchemaMetadata> getSchemaBySubjectAndId(
      final String subjectName, final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    final ApicurioAbstractParsedSchemaMetadata schema = new ApicurioAbstractParsedSchemaMetadata();

    final SchemaMetadataAdapter schemaMetadataAdapter = metadata.getSchemaMetadataAdapter();
    try {
      final InputStream inputStream = this.schemaRegistryClient.getContentByGlobalId(schemaMetadataAdapter.getGlobalId());

      final String searchedArtifactType = schemaMetadataAdapter.getType();
      setSchemaBySchemaType(schema, inputStream.readAllBytes(), searchedArtifactType);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return new BaseParsedSchema<>(schema);
  }

  private SearchedArtifact getLastestSearchedArtifact(final String artifactId) {
    SearchedArtifact searchedArtifact = null;

    for (final SearchedArtifact artifact : this.schemaRegistryClient.searchArtifacts(null, null, null, null, null, null,
                                                                                     null, null, null, null, null).getArtifacts()) {
      if (artifact.getId().equals(artifactId) && ArtifactState.ENABLED.equals(artifact.getState())) {
        searchedArtifact = artifact;
      }
    }

    if (searchedArtifact == null) {
      throw new KLoadGenException(String.format("Does not exist any enabled artifact registered with id %s", artifactId));
    }

    return searchedArtifact;
  }
}
