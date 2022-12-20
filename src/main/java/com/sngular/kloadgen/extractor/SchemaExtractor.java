package com.sngular.kloadgen.extractor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.commons.lang3.tuple.Pair;

public interface SchemaExtractor {

  Pair<String, List<FieldValueMapping>> flatPropertiesList(String subjectName) throws IOException, RestClientException;

  List<FieldValueMapping> flatPropertiesList(ParsedSchema parserSchema);

  ParsedSchema schemaTypesList(File schemaFile, String schemaType) throws IOException;

}
