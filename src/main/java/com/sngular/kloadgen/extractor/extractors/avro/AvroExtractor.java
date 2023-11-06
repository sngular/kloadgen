package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;

public class AvroExtractor implements ExtractorRegistry<Object> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new AvroConfluentExtractor(), SchemaRegistryEnum.APICURIO,
          new AvroApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final Object schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schema);
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new AvroSchema(fileContent);
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}