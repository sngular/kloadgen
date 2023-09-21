package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.apicurio.registry.utils.serde.avro.AvroSchemaUtils;
import org.apache.avro.Schema;

public class AvroApicurioExtractor extends AbstractAvroFileExtractor implements Extractor<Schema> {

  public final List<FieldValueMapping> processSchema(final Schema schemaReceived) {
    return processSchemaDefault(schemaReceived);
  }

  public final List<String> getSchemaNameList(final String schema) {
    return getSchemaNameList(AvroSchemaUtils.parse(schema));
  }

}