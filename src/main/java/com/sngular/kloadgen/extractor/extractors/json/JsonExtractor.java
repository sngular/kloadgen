package com.sngular.kloadgen.extractor.extractors.json;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;


public class JsonExtractor implements ExtractorRegistry<ParsedSchema> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new JsonDefaultExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new JsonDefaultExtractor());

  public final List<FieldValueMapping> processSchema(final ParsedSchema schemaReceived, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived.rawSchema().toString());
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new ParsedSchema(fileContent, "JSON");
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}
