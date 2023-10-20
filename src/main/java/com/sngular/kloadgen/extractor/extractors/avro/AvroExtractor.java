package com.sngular.kloadgen.extractor.extractors.avro;

import static com.sngular.kloadgen.common.SchemaRegistryEnum.APICURIO;
import static com.sngular.kloadgen.common.SchemaRegistryEnum.CONFLUENT;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;

public class AvroExtractor implements ExtractorRegistry<Object> {

  static Map<SchemaRegistryEnum, Extractor> schemaRegistryMap = Map.of(CONFLUENT, new AvroConfluentExtractor(), APICURIO,
          new AvroApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final Object schema, SchemaRegistryEnum registryEnum) {
    if(schema.getClass().toString().equals("class io.confluent.kafka.schemaregistry.avro.AvroSchema")) {
      return schemaRegistryMap.get(registryEnum).processSchema(((AvroSchema)schema).rawSchema());
    }
    return schemaRegistryMap.get(registryEnum).processSchema(schema);
  }

  public final List<String> getSchemaNameList(final String schema, SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).getSchemaNameList(schema);
  }

}