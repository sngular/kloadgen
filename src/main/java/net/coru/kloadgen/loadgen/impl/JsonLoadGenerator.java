/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.JsonSchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
public class JsonLoadGenerator implements BaseLoadGenerator {

  private SchemaRegistryClient schemaRegistryClient;

  private SchemaMetadata metadata;

  private final JsonSchemaProcessor jsonSchemaProcessor;

  public JsonLoadGenerator() {
    jsonSchemaProcessor = new JsonSchemaProcessor();
  }

  public void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings) {
    try {
      this.jsonSchemaProcessor.processSchema(fieldExprMappings);
    } catch (Exception exc){
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings) {
    try {
      this.jsonSchemaProcessor.processSchema(fieldExprMappings);
    } catch (Exception exc){
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public EnrichedRecord nextMessage() {
    return new EnrichedRecord(metadata, jsonSchemaProcessor.next());
  }

  private ParsedSchema retrieveSchema(Map<String, String> originals, String avroSchemaName) throws IOException, RestClientException {
    schemaRegistryClient = new CachedSchemaRegistryClient(List.of(originals.get(SCHEMA_REGISTRY_URL_CONFIG)), 1000, List.of(new JsonSchemaProvider()), originals);

    return getSchemaBySubject(avroSchemaName);
  }

  private ParsedSchema getSchemaBySubject(String avroSubjectName) throws IOException, RestClientException {
    metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSubjectName);
    return schemaRegistryClient.getSchemaBySubjectAndId(avroSubjectName, metadata.getId());
  }
}
