package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import org.apache.avro.Schema;

public class AvroConfluentExtractor extends AbstractAvroFileExtractor implements Extractor<Schema> {

  public final List<FieldValueMapping> processSchema(final Schema schema) {
    return this.processSchemaDefault(schema);
  }

  public final List<String> getSchemaNameList(final String schema) {
    return getSchemaNameList(new Schema.Parser().parse(schema));
  }

}