package com.sngular.kloadgen.extractor.extractors;

import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;

import java.util.List;

public interface Extractor {

    List<FieldValueMapping> processSchema(final Object schema);
    ParsedSchema getParsedSchema(final String schema);

    List<FieldValueMapping> processApicurioParsedSchema(Object schema);

    List<FieldValueMapping> processConfluentParsedSchema(Object schema);
}
