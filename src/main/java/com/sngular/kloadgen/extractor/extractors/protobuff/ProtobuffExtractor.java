package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtobuffExtractor implements ExtractorRegistry<Object> {

  static Map<SchemaRegistryEnum, Extractor> schemaRegistryMap = Map.of(SchemaRegistryEnum.CONFLUENT, new ProtoBufConfluentExtractor(),
      SchemaRegistryEnum.APICURIO, new ProtoBufApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final Object schemaReceived, final SchemaRegistryEnum registryEnum) {
    if (schemaReceived instanceof ProtoFileElement) {
      return schemaRegistryMap.get(SchemaRegistryEnum.APICURIO).processSchema(schemaReceived);
    }
    return schemaRegistryMap.get(registryEnum).processSchema(schemaReceived);
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new ProtobufSchema(fileContent);
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).getSchemaNameList(schema);
  }

}