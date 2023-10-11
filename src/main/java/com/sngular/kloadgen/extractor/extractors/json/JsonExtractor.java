package com.sngular.kloadgen.extractor.extractors.json;

import static com.sngular.kloadgen.common.SchemaRegistryEnum.APICURIO;
import static com.sngular.kloadgen.common.SchemaRegistryEnum.CONFLUENT;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;


public class JsonExtractor implements ExtractorRegistry<Object> {

  static Map<SchemaRegistryEnum, Extractor> schemaRegistryMap = Map.of(CONFLUENT, new JsonDefaultExtractor(),
          APICURIO, new JsonDefaultExtractor());

  public final List<FieldValueMapping> processSchema(final Object schemaReceived, SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).processSchema(schemaReceived);
  }

  @Override
  public ParsedSchema processSchema(String fileContent) {
    return new JsonSchema(fileContent);
  }

  public final List<String> getSchemaNameList(final String schema, SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).getSchemaNameList(schema);
  }

}
