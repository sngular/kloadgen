package net.coru.kloadgen.extractor;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;

public interface SchemaExtractor {

	List<FieldValueMapping> flatPropertiesList(String subjectName) throws IOException, RestClientException;

	List<FieldValueMapping> flatPropertiesList(ParsedSchema parserSchema);

	ParsedSchema schemaTypesList(File schemaFile) throws IOException;

	List<FieldValueMapping> processSchema(ParsedSchema schema);
}
