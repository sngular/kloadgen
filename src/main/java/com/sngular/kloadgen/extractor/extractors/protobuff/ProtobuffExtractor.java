package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtobuffExtractor<T> implements ExtractorRegistry<T> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new ProtoBufConfluentExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new ProtoBufApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final T schemaReceived, final SchemaRegistryEnum registryEnum) {
    final var resultSchema = new ArrayList<FieldValueMapping>();
    if (schemaReceived instanceof ProtoFileElement) {
      resultSchema.addAll(SCHEMA_REGISTRY_MAP.get(SchemaRegistryEnum.APICURIO).processSchema(schemaReceived));
    } else {
      resultSchema.addAll(SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived));
    }
    return resultSchema;
  }

  public final ParsedSchema processSchema(final String fileContent) {
    return new ProtobufSchema(fileContent);
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}