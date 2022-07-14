package net.coru.kloadgen.loadgen.impl;

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
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface SRLoadGenerator {

  default Pair<SchemaMetadata, ParsedSchema> retrieveSchema(final Map<String, String> originals, final String avroSchemaName)
      throws IOException, RestClientException {
    final var schemaRegistryClient =
        new CachedSchemaRegistryClient(originals.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG), 1, List.of(new AvroSchemaProvider(), new JsonSchemaProvider(),
                                                                                                                            new ProtobufSchemaProvider()), originals);
    final var metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSchemaName);
    return Pair.of(metadata, schemaRegistryClient.getSchemaBySubjectAndId(avroSchemaName, metadata.getId()));
  }
}
