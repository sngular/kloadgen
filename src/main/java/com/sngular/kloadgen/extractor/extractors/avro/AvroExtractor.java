package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;
import com.sngular.kloadgen.parsedschema.AvroParsedSchema;
import org.apache.avro.Schema;

public class AvroExtractor implements ExtractorRegistry<AvroParsedSchema> {

  private static final Map<SchemaRegistryEnum, Extractor> SCHEMA_REGISTRY_MAP = Map.of(SchemaRegistryEnum.CONFLUENT, new AvroConfluentExtractor(), SchemaRegistryEnum.APICURIO,
                                                                       new AvroApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final AbstractParsedSchema<?> schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).processSchema(schema.getSchema());
  }

  public final AvroParsedSchema processSchema(final String fileContent) {
    return new AvroParsedSchema(null, new Schema.Parser().parse(fileContent));
  }

  public final List<String> getSchemaNameList(final String schema, final SchemaRegistryEnum registryEnum) {
    return SCHEMA_REGISTRY_MAP.get(registryEnum).getSchemaNameList(schema);
  }

}