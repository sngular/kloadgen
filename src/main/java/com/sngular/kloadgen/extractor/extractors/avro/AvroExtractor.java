package com.sngular.kloadgen.extractor.extractors.avro;

import static com.sngular.kloadgen.common.SchemaRegistryEnum.APICURIO;
import static com.sngular.kloadgen.common.SchemaRegistryEnum.CONFLUENT;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
//import com.sngular.kloadgen.model.json.Schema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.apache.avro.Schema;
public class AvroExtractor implements ExtractorRegistry<Object> {

  static Map<SchemaRegistryEnum, Extractor> schemaRegistryMap = Map.of(CONFLUENT, new AvroConfluentExtractor(), APICURIO,
          new AvroApicurioExtractor());

  public final List<FieldValueMapping> processSchema(final Object schema, SchemaRegistryEnum registryEnum) {
    Schema rawschema;
    if(schema instanceof AvroSchema) {
      rawschema = ((AvroSchema)schema).rawSchema();
    }else {
      rawschema = (Schema) schema;
    }
    return schemaRegistryMap.get(registryEnum).processSchema(rawschema);
  }

  public final List<String> getSchemaNameList(final String schema, SchemaRegistryEnum registryEnum) {
    return schemaRegistryMap.get(registryEnum).getSchemaNameList(schema);
  }

}