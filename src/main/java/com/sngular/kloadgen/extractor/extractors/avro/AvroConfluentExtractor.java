package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;

public class AvroConfluentExtractor extends AbstractAvroFileExtractor implements Extractor<AvroSchema> {

  public final List<FieldValueMapping> processSchema(final AvroSchema schema) {
    return this.processSchemaDefault(schema.rawSchema());
  }

  public final List<String> getSchemaNameList(final String schema) {
    return getSchemaNameList(new AvroSchema(schema).rawSchema());
  }

}