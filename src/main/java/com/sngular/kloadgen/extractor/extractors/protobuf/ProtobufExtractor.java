package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;
import com.sngular.kloadgen.parsedschema.ProtobufParsedSchema;
import com.squareup.wire.schema.Location;
import com.squareup.wire.schema.internal.parser.ProtoParser;

public class ProtobufExtractor implements ExtractorRegistry<ProtobufParsedSchema> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new ProtoBufConfluentExtractor(),
                                                                                       SchemaRegistryEnum.APICURIO, new ProtoBufApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final AbstractParsedSchema<?> schemaReceived, final SchemaRegistryEnum registryEnum) {
    return new ArrayList<FieldValueMapping>(SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schemaReceived));
  }

  public final ProtobufParsedSchema processSchema(final String fileContent) {
    final var protoDescriptor = new ProtoParser(Location.get("/"), fileContent.toCharArray());
    return new ProtobufParsedSchema(null, protoDescriptor.readProtoFile());
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}