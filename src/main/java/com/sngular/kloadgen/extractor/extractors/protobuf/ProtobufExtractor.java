package com.sngular.kloadgen.extractor.extractors.protobuf;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtobufExtractor<T extends ParsedSchema> implements ExtractorRegistry<T> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new ProtoBufConfluentExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new ProtoBufApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final T schemaReceived, final SchemaRegistryEnum registryEnum) {
    return new ArrayList<FieldValueMapping>(SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived));
  }

  public final T processSchema(final String fileContent) {
    return (T) new ParsedSchema(fileContent, "PROTOBUF");
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}