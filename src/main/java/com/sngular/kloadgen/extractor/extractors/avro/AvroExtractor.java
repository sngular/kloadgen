package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;

public class AvroExtractor implements ExtractorRegistry<Object> {

  private static Map<SchemaRegistryEnum, Extractor> schemaRegistryMap = Map.of(SchemaRegistryEnum.CONFLUENT, new AvroConfluentExtractor(), SchemaRegistryEnum.APICURIO,
                                                                       new AvroApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final Object schema, final SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).processSchema(schema);
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).getSchemaNameList(schema);
  }

}