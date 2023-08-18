package com.sngular.kloadgen.schemaregistry.impl;

import com.google.protobuf.Message;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.*;
import io.apicurio.registry.resolver.ParsedSchema;
import io.apicurio.registry.resolver.ParsedSchemaImpl;
import io.apicurio.registry.resolver.SchemaParser;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.RegistryClientFactory;
import io.apicurio.registry.rest.client.exception.RestClientException;
import io.apicurio.registry.rest.v2.beans.ArtifactMetaData;
import io.apicurio.registry.rest.v2.beans.ArtifactReference;
import io.apicurio.registry.rest.v2.beans.SearchedArtifact;
import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.avro.AvroSchemaParser;
import io.apicurio.registry.serde.jsonschema.JsonSchema;
import io.apicurio.registry.serde.jsonschema.JsonSchemaParser;
import io.apicurio.registry.serde.protobuf.ProtobufSchemaParser;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.utils.IoUtil;
import io.apicurio.registry.utils.protobuf.schema.FileDescriptorUtils;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import org.apache.avro.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class ApicurioSchemaRegistry implements SchemaRegistryAdapter {

  private RegistryClient schemaRegistryClient;

  public String getSchemaRegistryUrlKey() {
    return SerdeConfig.REGISTRY_URL;
  }

  @Override
  public void setSchemaRegistryClient(final String url, final Map<String, ?> properties) {
    this.schemaRegistryClient = RegistryClientFactory.create(url);
  }

  @Override
  public void setSchemaRegistryClient(final Map<String, ?> properties) {
    final String url = Objects.toString(properties.get(this.getSchemaRegistryUrlKey()), "");
    this.schemaRegistryClient = RegistryClientFactory.create(url);
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
  public BaseParsedSchema<ApicurioParsedSchemaMetadata> getSchemaBySubject(final String artifactId) {
    final ApicurioParsedSchemaMetadata schema = new ApicurioParsedSchemaMetadata();
    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(artifactId);
      final InputStream inputStream = this.schemaRegistryClient.getLatestArtifact(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final ArtifactMetaData artifactMetaData = this.schemaRegistryClient.getArtifactMetaData(searchedArtifact.getGroupId(), searchedArtifact.getId());

      final String searchedArtifactType = searchedArtifact.getType();
      setSchemaBySchemaType(schema, inputStream.readAllBytes(), searchedArtifactType, artifactMetaData.getReferences());
      schema.setType(searchedArtifactType);
      return new BaseParsedSchema<>(schema);
    } catch (final IOException e) {
      throw new KLoadGenException(e);
    }
  }

  private void setSchemaBySchemaType(final ApicurioParsedSchemaMetadata schema, final byte[] result, final String searchedArtifactType, final List<ArtifactReference> artifactReferenceList) {

    switch (SchemaTypeEnum.valueOf(searchedArtifactType)) {
      case AVRO:
        final SchemaParser<Schema, Object> parserAvro = new AvroSchemaParser<>(null);
        schema.setSchema(parserAvro.parseSchema(result, solveAvroReferences(artifactReferenceList)));
        break;
      case PROTOBUF:
        final SchemaParser<ProtobufSchema, Message> parserProtobuf = new ProtobufSchemaParser<>();
        schema.setSchema(parserProtobuf.parseSchema(result, solveProtoBufReferences(artifactReferenceList)));
        break;
      case JSON:
        final SchemaParser<JsonSchema, Object> parserJson = new JsonSchemaParser<>();
        schema.setSchema(parserJson.parseSchema(result, solveJsonReferences(artifactReferenceList)));
        break;
      default:
        throw new KLoadGenException(String.format("Schema type not supported %s", searchedArtifactType));

    }
  }

  private Map<String, ParsedSchema<JsonSchema>> solveJsonReferences(List<ArtifactReference> references) {
    final var referencesMap = new HashMap<String, ParsedSchema<JsonSchema>>();
    references.forEach(
            reference -> {
              var parsedSchema = getJsonParsedSchemaBySubject(reference.getArtifactId());
              referencesMap.put(reference.getArtifactId(), parsedSchema);
            }
    );

    return referencesMap;
  }

  private Map<String, ParsedSchema<ProtobufSchema>> solveProtoBufReferences(final List<ArtifactReference> references) {

    final var referencesMap = new HashMap<String, ParsedSchema<ProtobufSchema>>();
    references.forEach(
            reference -> {
              var parsedSchema = getProtoBufParsedSchemaBySubject(reference.getArtifactId());
              referencesMap.put(reference.getArtifactId(), parsedSchema);
            }
    );

    return referencesMap;
  }

  private Map<String, ParsedSchema<Schema>> solveAvroReferences(final List<ArtifactReference> references) {

    final var referencesMap = new HashMap<String, ParsedSchema<Schema>>();
    references.forEach(
            reference -> {
              var parsedSchema = getAvroParsedSchemaBySubject(reference.getArtifactId());
              referencesMap.put(reference.getArtifactId(), parsedSchema);
            }
    );

    return referencesMap;
  }

  private ParsedSchema<JsonSchema> getJsonParsedSchemaBySubject(final String artifactId) {
    final JsonSchemaParser parserJSON = new JsonSchemaParser();

    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(artifactId);
      final InputStream inputStream = this.schemaRegistryClient.getLatestArtifact(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final ArtifactMetaData artifactMetaData = this.schemaRegistryClient.getArtifactMetaData(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final var solvedParsedSchemas = solveJsonReferences(artifactMetaData.getReferences());
      var schema = parserJSON.parseSchema(inputStream.readAllBytes(), solvedParsedSchemas);
      return new ParsedSchemaImpl<JsonSchema>()
              .setParsedSchema(schema)
              .setSchemaReferences(new ArrayList<>(solvedParsedSchemas.values()))
              .setRawSchema(schema.toString().getBytes());

    } catch (final IOException e) {
      throw new KLoadGenException(e);
    }
  }

  private ParsedSchema<Schema> getAvroParsedSchemaBySubject(final String artifactId) {
    final SchemaParser<Schema, Object> parserAvro = new AvroSchemaParser<>(null);

    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(artifactId);
      final InputStream inputStream = this.schemaRegistryClient.getLatestArtifact(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final ArtifactMetaData artifactMetaData = this.schemaRegistryClient.getArtifactMetaData(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final var solvedParsedSchemas = solveAvroReferences(artifactMetaData.getReferences());
      final var solvedSchemas = new HashSet<Schema>();
      solvedParsedSchemas.forEach((key, parsedSchema) -> solvedSchemas.add(parsedSchema.getParsedSchema()));
      var schema = parserAvro.parseSchema(inputStream.readAllBytes(), solvedParsedSchemas);
      return new ParsedSchemaImpl<Schema>()
              .setParsedSchema(schema)
              .setReferenceName(schema.getFullName())
              .setSchemaReferences(new ArrayList<>(solvedParsedSchemas.values()))
              .setRawSchema(IoUtil.toBytes(schema.toString(solvedSchemas, false)));

    } catch (final IOException e) {
      throw new KLoadGenException(e);
    }
  }

  private ParsedSchema<ProtobufSchema> getProtoBufParsedSchemaBySubject(final String artifactId) {
    final ProtobufSchemaParser<Message> protoBuf = new ProtobufSchemaParser<>();

    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(artifactId);
      final InputStream inputStream = this.schemaRegistryClient.getLatestArtifact(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final ArtifactMetaData artifactMetaData = this.schemaRegistryClient.getArtifactMetaData(searchedArtifact.getGroupId(), searchedArtifact.getId());
      final var solvedParsedSchemas = solveProtoBufReferences(artifactMetaData.getReferences());
      var schema = protoBuf.parseSchema(inputStream.readAllBytes(), solvedParsedSchemas);
      return new ParsedSchemaImpl<ProtobufSchema>()
              .setParsedSchema(schema)
              .setReferenceName(schema.getFileDescriptor().getName())
              .setSchemaReferences(new ArrayList<>(solvedParsedSchemas.values()))
              .setRawSchema(IoUtil.toBytes(FileDescriptorUtils.fileDescriptorToProtoFile(schema.getFileDescriptor().toProto()).toSchema()));

    } catch (final IOException e) {
      throw new KLoadGenException(e);
    }
  }

  @Override
  public BaseParsedSchema<ApicurioParsedSchemaMetadata> getSchemaBySubjectAndId(
          final String subjectName, final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    final ApicurioParsedSchemaMetadata schema = new ApicurioParsedSchemaMetadata();

    final SchemaMetadataAdapter schemaMetadataAdapter = metadata.getSchemaMetadataAdapter();
    try {
      final InputStream inputStream = this.schemaRegistryClient.getContentByGlobalId(schemaMetadataAdapter.getGlobalId());
      final var artifactReferenceList = this.schemaRegistryClient.getArtifactReferencesByGlobalId(schemaMetadataAdapter.getGlobalId());

      final String searchedArtifactType = schemaMetadataAdapter.getType();
      setSchemaBySchemaType(schema, inputStream.readAllBytes(), searchedArtifactType, artifactReferenceList);
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
