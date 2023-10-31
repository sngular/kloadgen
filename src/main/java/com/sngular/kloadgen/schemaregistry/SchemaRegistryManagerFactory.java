package com.sngular.kloadgen.schemaregistry;

import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.schemaregistry.impl.ApicurioSchemaRegistry;
import com.sngular.kloadgen.schemaregistry.impl.ConfluentSchemaRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaRegistryManagerFactory {

  static Map<SchemaRegistryEnum, SchemaRegistryAdapter> schemaRegistryMap = Map.of(SchemaRegistryEnum.CONFLUENT, new ConfluentSchemaRegistry(), SchemaRegistryEnum.APICURIO, new ApicurioSchemaRegistry());

  public static SchemaRegistryAdapter getSchemaRegistry(final String registry) {
    try {
      final SchemaRegistryEnum schemaRegistryEnum = SchemaRegistryEnum.valueOf(registry.toUpperCase());
      return schemaRegistryMap.get(schemaRegistryEnum);
    } catch (final IllegalArgumentException e) {
      final String logMsg = "Can not parse the registry " + registry;
      log.error(logMsg, e);
      throw new KLoadGenException("Error obtaining the schema registry manager. " + logMsg);
    }
  }
}
