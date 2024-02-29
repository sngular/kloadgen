package com.sngular.kloadgen.extractor.extractors.json;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;
import com.sngular.kloadgen.parsedschema.JsonParsedSchema;

public class JsonExtractor implements ExtractorRegistry<JsonParsedSchema> {

  private static final Map<SchemaRegistryEnum, Extractor<String>> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new JsonDefaultExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new JsonDefaultExtractor());

  public final List<FieldValueMapping> processSchema(final AbstractParsedSchema<?> schemaReceived, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived.getRawSchema().toString());
  }


  public final JsonParsedSchema processSchema(final String fileContent) {
    return new JsonParsedSchema(null, fileContent);
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}
