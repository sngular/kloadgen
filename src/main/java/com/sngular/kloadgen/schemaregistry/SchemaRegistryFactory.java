package com.sngular.kloadgen.schemaregistry;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.util.JMeterHelper;
import io.apicurio.registry.rest.client.RegistryClientFactory;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;

public final class SchemaRegistryFactory {

  private SchemaRegistryFactory() {
  }

  public static Object getSchemaRegistryClient(final SchemaRegistryEnum typeEnum, final String url, final Map<String, ?> properties) {
    return switch (typeEnum) {
      case APICURIO -> RegistryClientFactory.create(url);
      case CONFLUENT -> new CachedSchemaRegistryClient(List.of(JMeterHelper.checkPropertyOrVariable(url)), 1000,
                                                       List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()), properties);
    };
  }
}
