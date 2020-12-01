/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor.impl;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.extractors.AvroExtractor;
import net.coru.kloadgen.extractor.extractors.JsonExtractor;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;

public class SchemaExtractorImpl implements SchemaExtractor {

  private final AvroExtractor avroExtractor = new AvroExtractor();

  private final JsonExtractor jsonExtractor = new JsonExtractor();

  @Override
  public Pair<String, List<FieldValueMapping>> flatPropertiesList(String subjectName) throws IOException, RestClientException {
    Map<String, String> originals = new HashMap<>();

    Properties properties = JMeterContextService.getContext().getProperties();
    if (Objects.nonNull(properties.getProperty(SCHEMA_REGISTRY_URL))) {
      originals.put(SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(SCHEMA_REGISTRY_URL));

      if (FLAG_YES.equals(properties.getProperty(SCHEMA_REGISTRY_AUTH_FLAG))) {
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE
            .equals(properties.getProperty(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BASIC_AUTH_CREDENTIALS_SOURCE,
              properties.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE));
          originals.put(USER_INFO_CONFIG, properties.getProperty(USER_INFO_CONFIG));
        } else if (SCHEMA_REGISTRY_AUTH_BEARER_KEY
            .equals(properties.getProperty(SCHEMA_REGISTRY_AUTH_KEY))) {
          originals.put(BEARER_AUTH_CREDENTIALS_SOURCE,
              properties.getProperty(BEARER_AUTH_CREDENTIALS_SOURCE));
          originals.put(BEARER_AUTH_TOKEN_CONFIG, properties.getProperty(BEARER_AUTH_TOKEN_CONFIG));
        }
      }
    }

    List<FieldValueMapping> attributeList = new ArrayList<>();
    SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(List.of(originals.get(SCHEMA_REGISTRY_URL_CONFIG)), 1000, List.of(new AvroSchemaProvider(), new JsonSchemaProvider()), originals);

    SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(subjectName);
    ParsedSchema schema = schemaRegistryClient.getSchemaBySubjectAndId(subjectName, schemaMetadata.getId());
    if ("AVRO".equalsIgnoreCase(schema.schemaType())) {
      (((AvroSchema) schema).rawSchema()).getFields().forEach(field -> avroExtractor.processField(field, attributeList));
    } else if ("JSON".equalsIgnoreCase(schema.schemaType())){
      attributeList.addAll(jsonExtractor.processSchema(((JsonSchema) schema).toJsonNode()));
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schema.schemaType()));
    }
    return Pair.of(schema.schemaType(), attributeList);
  }

  @Override
  public List<FieldValueMapping> flatPropertiesList(ParsedSchema parserSchema) {
    return processSchema(parserSchema);
  }

  @Override
  public ParsedSchema schemaTypesList(File schemaFile, String schemaType) throws IOException {
    ParsedSchema parsedSchema;
    if ("AVRO".equalsIgnoreCase(schemaType)) {
      parsedSchema = new AvroSchema(readLineByLine(schemaFile.getPath()));
    } else {
      parsedSchema = new JsonSchema(readLineByLine(schemaFile.getPath()));
    }
    return parsedSchema;
  }

  private List<FieldValueMapping> processSchema(ParsedSchema schema) {
    if ("AVRO".equalsIgnoreCase(schema.schemaType())) {
      return avroExtractor.processSchema(((AvroSchema)schema).rawSchema());
    } else if ("JSON".equalsIgnoreCase(schema.schemaType())) {
      return jsonExtractor.processSchema(((JsonSchema)schema).toJsonNode());
    } else {
      throw new KLoadGenException("Unsupported Schema Type");
    }
  }

  private static String readLineByLine(String filePath) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
    {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }


}
