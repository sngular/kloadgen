package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;

public class AvroExtractor<T extends ParsedSchema> implements ExtractorRegistry<T> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new AvroConfluentExtractor(), SchemaRegistryEnum.APICURIO,
                                                                       new AvroApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final T schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schema.rawSchema());
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new ParsedSchema(fileContent, "AVRO");
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}