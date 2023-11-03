package com.sngular.kloadgen.extractor.extractors.json;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;


public class JsonExtractor implements ExtractorRegistry<Object> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new JsonDefaultExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new JsonDefaultExtractor());

  public final List<FieldValueMapping> processSchema(final Object schemaReceived, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived);
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new JsonSchema(fileContent);
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}
