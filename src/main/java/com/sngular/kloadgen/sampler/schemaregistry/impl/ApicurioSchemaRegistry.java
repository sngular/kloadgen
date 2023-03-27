package com.sngular.kloadgen.sampler.schemaregistry.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryConstants;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryManager;
import com.sngular.kloadgen.sampler.schemaregistry.schema.ApicurioParsedSchema;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.apicurio.registry.resolver.SchemaParser;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.RegistryClientFactory;
import io.apicurio.registry.rest.client.exception.RestClientException;
import io.apicurio.registry.rest.v2.beans.ArtifactMetaData;
import io.apicurio.registry.rest.v2.beans.SearchedArtifact;
import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.avro.AvroSchemaParser;
import io.apicurio.registry.serde.jsonschema.JsonSchemaParser;
import io.apicurio.registry.serde.protobuf.ProtobufSchemaParser;
import io.apicurio.registry.types.ArtifactState;
import org.apache.tika.io.IOUtils;

public class ApicurioSchemaRegistry implements SchemaRegistryManager {

  private RegistryClient schemaRegistryClient;

  private Map<String, String> propertiesMap;

  public ApicurioSchemaRegistry() {
    this.propertiesMap = Map.of(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryConstants.SCHEMA_REGISTRY_APICURIO,
                                SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL_KEY, SerdeConfig.REGISTRY_URL);
  }

  @Override
  public String getSchemaRegistryUrlKey() {
    return SerdeConfig.REGISTRY_URL;
  }

  @Override
  public void setSchemaRegistryClient(String url, Map<String, ?> properties) {
    this.schemaRegistryClient = RegistryClientFactory.create(url);
  }

  @Override
  public void setSchemaRegistryClient(Map<String, ?> properties) {
    String url = properties.get(this.getSchemaRegistryUrlKey()).toString();
    this.schemaRegistryClient = RegistryClientFactory.create(url);
  }

  @Override
  public Map<String, String> getPropertiesMap() {
    return propertiesMap;
  }

  @Override
  public Collection<String> getAllSubjects() throws KLoadGenException {
    Collection<String> subjects = new ArrayList<>();
    try {
      List<SearchedArtifact> artifacts = this.schemaRegistryClient.searchArtifacts(null, null, null,
                                                                                   null, null, null, null, null, null).getArtifacts();

      for (SearchedArtifact searchedArtifact : artifacts) {
        subjects.add(searchedArtifact.getName());
      }
      return subjects;
    } catch (RestClientException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  @Override
  public Object getLatestSchemaMetadata(String subjectName) throws KLoadGenException {
    try {
      final SearchedArtifact searchedArtifact = getLastestSearchedArtifact(subjectName);
      return this.schemaRegistryClient.getArtifactMetaData(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_DEFAULT_GROUP, searchedArtifact.getId());
    } catch (RestClientException e) {
      throw new KLoadGenException(e.getMessage());
    }
  }

  @Override
  public ApicurioParsedSchema getSchemaBySubject(String subjectName) {
    final ApicurioParsedSchema schema = new ApicurioParsedSchema();
    try {
      List<SearchedArtifact> artifacts = this.schemaRegistryClient.searchArtifacts(null, subjectName, null,
                                                                                   null, null, null, null, null, null).getArtifacts();
      if (artifacts.isEmpty()) {
        throw new KLoadGenException(String.format("Schema %s not found", subjectName));
      } else {
        SearchedArtifact searchedArtifact = artifacts.get(0);
        InputStream inputStream = this.schemaRegistryClient.getLatestArtifact(searchedArtifact.getGroupId(), searchedArtifact.getId());
        String result = IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));

        String searchedArtifactType = searchedArtifact.getType().toString();
        if (SchemaTypeEnum.AVRO.name().equalsIgnoreCase(searchedArtifactType)) {
          SchemaParser parserAvro = new AvroSchemaParser(null);
          schema.setSchema(parserAvro.parseSchema(result.getBytes(StandardCharsets.UTF_8), new HashMap<>()));
        } else if (SchemaTypeEnum.JSON.name().equalsIgnoreCase(searchedArtifactType)) {
          SchemaParser parserJson = new JsonSchemaParser();
          schema.setSchema(parserJson.parseSchema(result.getBytes(StandardCharsets.UTF_8), new HashMap<>()));
        } else if (SchemaTypeEnum.PROTOBUF.name().equalsIgnoreCase(searchedArtifactType)) {
          SchemaParser parserProtobuf = new ProtobufSchemaParser();
          schema.setSchema(parserProtobuf.parseSchema(result.getBytes(StandardCharsets.UTF_8), new HashMap<>()));
        } else {
          throw new KLoadGenException(String.format("Schema type not supported %s", searchedArtifactType));
        }
        schema.setType(searchedArtifactType);
        return schema;
      }
    } catch (IOException e) {
      throw new KLoadGenException(e);
    }
  }

  public Object getSchemaBySubjectAndId(String subjectName, Object metadata) {
    Object schema = null;
    final ArtifactMetaData schemaMetadata = (ArtifactMetaData) metadata;

    try {
      InputStream inputStream = this.schemaRegistryClient.getContentByGlobalId(schemaMetadata.getGlobalId());
      String result = IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));

      String searchedArtifactType = schemaMetadata.getType().toString();
      if (SchemaTypeEnum.AVRO.name().equalsIgnoreCase(searchedArtifactType)) {
        SchemaParser parserAvro = new AvroSchemaParser(null);
        schema = parserAvro.parseSchema(result.getBytes(StandardCharsets.UTF_8), new HashMap<>());
      } else if (SchemaTypeEnum.JSON.name().equalsIgnoreCase(searchedArtifactType)) {
        SchemaParser parserJson = new JsonSchemaParser();
        schema = parserJson.parseSchema(result.getBytes(StandardCharsets.UTF_8), new HashMap<>());
      } else if (SchemaTypeEnum.PROTOBUF.name().equalsIgnoreCase(searchedArtifactType)) {
        SchemaParser parserProtobuf = new ProtobufSchemaParser();
        schema = parserProtobuf.parseSchema(result.getBytes(StandardCharsets.UTF_8), new HashMap<>());
      } else {
        throw new KLoadGenException(String.format("Schema type not supported %s", searchedArtifactType));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return schema;
  }

  private SearchedArtifact getLastestSearchedArtifact(final String subjectName) {
    boolean found = false;
    SearchedArtifact best = null;
    final Comparator<SearchedArtifact> comparator = Comparator.comparing(SearchedArtifact::getModifiedOn);
    for (SearchedArtifact artifact : this.schemaRegistryClient.searchArtifacts(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_DEFAULT_GROUP, null, null, null, null, null, null, null,
                                                                               null,
                                                                               null, null).getArtifacts()) {
      if (artifact.getName().equals(subjectName) && ArtifactState.ENABLED.equals(artifact.getState()) && !found || comparator.compare(artifact, best) > 0) {
        found = true;
        best = artifact;
      }
    }
    SearchedArtifact searchedArtifact = (found ? Optional.of(best) : Optional.<SearchedArtifact>empty())
        .orElseThrow(() -> new KLoadGenException(String.format("Does not exist any enabled" +
                                                               "artifact " +
                                                               "registered with name %s",
                                                               subjectName)));
    return searchedArtifact;
  }
}
