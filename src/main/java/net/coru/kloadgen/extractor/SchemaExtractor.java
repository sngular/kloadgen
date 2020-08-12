package net.coru.kloadgen.extractor;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.avro.Schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SchemaExtractor {

	List<FieldValueMapping> flatPropertiesList(String subjectName) throws IOException, RestClientException;

	List<FieldValueMapping> flatPropertiesList(Schema parserSchema);

	Schema schemaTypesList(File schemaFile) throws IOException;

	List<FieldValueMapping> processSchema(Schema schema);
}
