package com.sngular.kloadgen.serializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.extractor.impl.SchemaExtractorImpl;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.testutil.FileHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSerializerTest {

  private static final SchemaProcessor JSON_SCHEMA_PROCESSOR = new SchemaProcessor();

  private static final GenericJsonRecordSerializer<ObjectNode> serializer = new GenericJsonRecordSerializer();

  @Test
  void recordSerializersTestLogicalTypes() throws Exception {
    final var schemaFile = new FileHelper().getFile("/jsonschema/nested-collections.jcs");
    final var schemaStr = readSchema(schemaFile);
    final var fieldValueMappings = Arrays.asList(
        createFieldValueMapping("arrayOfMapsOfObjects[][:].stringObject", "string"),
        createFieldValueMapping("arrayOfMapsOfObjects[][:].numberObject", "number"),
        createFieldValueMapping("arrayOfArraysOfStrings[][]", "string-array-array"),
        createFieldValueMapping("mapOfArraysOfStrings[:][]", "string-array-map"),
        createFieldValueMapping("mapOfMapsOfObjects[:][:].name4Object", "string"),
        createFieldValueMapping("mapOfMapsOfObjects[:][:].number4Object", "number"),
        createFieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].stringControl", "string"),
        createFieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].numberControl", "number"));
    final var metadata = new SchemaMetadata(1, 1, schemaStr);

    final ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(schemaFile, "JSON");
    JSON_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.JSON, parsedSchema, metadata, fieldValueMappings);
    final var objectNode = JSON_SCHEMA_PROCESSOR.next();

    final var message = serializer.serialize("the-topic", (ObjectNode) objectNode);

    Assertions.assertThat(message).isNotNull();
  }

  private static String readSchema(final File file) throws IOException {
    final StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }

  private FieldValueMapping createFieldValueMapping(final String name, final String fieldType) {
    return FieldValueMapping.builder().fieldName(name).fieldType(fieldType).valueLength(0).fieldValueList("").required(false)
                            .isAncestorRequired(true).build();
  }
}
