package net.coru.kloadgen.loadgen.impl;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

import java.io.IOException;
import java.util.Map;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractLoadGenerator {

  Pair<SchemaMetadata, ParsedSchema> retrieveSchema(Map<String, String> originals, String avroSchemaName) throws IOException, RestClientException {
    var schemaRegistryClient = new CachedSchemaRegistryClient(originals.get(SCHEMA_REGISTRY_URL_CONFIG), 1000, originals);
    var metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSchemaName);
    return Pair.of(metadata, schemaRegistryClient.getSchemaBySubjectAndId(avroSchemaName, metadata.getId()));
  }
}
