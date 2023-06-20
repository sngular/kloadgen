package com.sngular.kloadgen.loadgen.impl;

import java.io.IOException;
import java.util.Map;

import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface SRLoadGenerator {

  default Pair<BaseSchemaMetadata, Object> retrieveSchema(
      final Map<String, String> originals, final String avroSchemaName)
      throws IOException, RestClientException {

    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(originals.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME));
    schemaRegistryManager.setSchemaRegistryClient(originals);

    final var metadata = schemaRegistryManager.getLatestSchemaMetadata(avroSchemaName);
    return Pair.of(metadata, schemaRegistryManager.getSchemaBySubjectAndId(avroSchemaName, metadata));
  }
}
