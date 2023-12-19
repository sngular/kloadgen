package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;

public class ProtobufExtractor implements ExtractorRegistry<ParsedSchema> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new ProtoBufConfluentExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new ProtoBufApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final ParsedSchema schemaReceived, final SchemaRegistryEnum registryEnum) {
    return new ArrayList<FieldValueMapping>(SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived));
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new ParsedSchema(fileContent, "PROTOBUF");
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}