package com.sngular.kloadgen.extractor.extractors;

import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;

public interface Extractor {

  List<FieldValueMapping> processSchema(final Object schema);

  ParsedSchema getParsedSchema(final String schema);

  List<FieldValueMapping> processApicurioParsedSchema(final Object schema);

  List<FieldValueMapping> processConfluentParsedSchema(final Object schema);
}
