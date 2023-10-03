package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;

public class ProtobuffExtractor implements ExtractorRegistry<Object> {

  static Map<SchemaRegistryEnum, Extractor> schemaRegistryMap = Map.of(SchemaRegistryEnum.CONFLUENT, new ProtoBufConfluentExtractor(),
                                                                       SchemaRegistryEnum.APICURIO, new ProtoBufApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final Object schemaReceived, final SchemaRegistryEnum registryEnum) {
    final var resultSchema = new ArrayList<FieldValueMapping>();
    if (schemaReceived instanceof ProtoFileElement) {
      resultSchema.addAll(schemaRegistryMap.get(SchemaRegistryEnum.APICURIO).processSchema(schemaReceived));
    }
    resultSchema.addAll(schemaRegistryMap.get(registryEnum).processSchema(schemaReceived));
    return resultSchema;
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).getSchemaNameList(schema);
  }

}