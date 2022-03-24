package net.coru.kloadgen.loadgen.impl;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractLoadGenerator {

  Pair<SchemaMetadata, ParsedSchema> retrieveSchema(Map<String, String> originals, String avroSchemaName) throws IOException, RestClientException {
    var schemaRegistryClient =
        new CachedSchemaRegistryClient(originals.get(SCHEMA_REGISTRY_URL_CONFIG), 1, List.of(new AvroSchemaProvider(), new JsonSchemaProvider(),
                                                                                             new ProtobufSchemaProvider()), originals);
    var metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSchemaName);
    return Pair.of(metadata, schemaRegistryClient.getSchemaBySubjectAndId(avroSchemaName, metadata.getId()));
  }
}
