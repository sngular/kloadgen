package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JsonSerializerTest {

  private static final SchemaProcessor JSON_SCHEMA_PROCESSOR = new SchemaProcessor();

  private static final GenericJsonRecordSerializer SERIALIZER = new GenericJsonRecordSerializer();

  private static Stream<Arguments> getSchemaToTest() {
    final Builder<Arguments> builder = Stream.builder();

    final File testBasicFile = JsonSerializerTestFixture.TEST_BASIC.getFirst();
    builder.add(Arguments.arguments(Named.of(testBasicFile.getName(), testBasicFile), JsonSerializerTestFixture.TEST_BASIC.getSecond()));
    final File testBasicArraysFile = JsonSerializerTestFixture.TEST_BASIC_ARRAY.getFirst();
    builder.add(Arguments.arguments(Named.of(testBasicArraysFile.getName(), testBasicArraysFile), JsonSerializerTestFixture.TEST_BASIC_ARRAY.getSecond()));
    final File testBasicNumberFile = JsonSerializerTestFixture.TEST_BASIC_NUMBER.getFirst();
    builder.add(Arguments.arguments(Named.of(testBasicNumberFile.getName(), testBasicNumberFile), JsonSerializerTestFixture.TEST_BASIC_NUMBER.getSecond()));
    final File testCollectionsFile = JsonSerializerTestFixture.TEST_COLLECTIONS.getFirst();
    builder.add(Arguments.arguments(Named.of(testCollectionsFile.getName(), testCollectionsFile), JsonSerializerTestFixture.TEST_COLLECTIONS.getSecond()));
    final File testComplexDefinitionsFile = JsonSerializerTestFixture.TEST_COMPLEX_DEFINITIONS.getFirst();
    builder.add(Arguments.arguments(Named.of(testComplexDefinitionsFile.getName(), testComplexDefinitionsFile), JsonSerializerTestFixture.TEST_COMPLEX_DEFINITIONS.getSecond()));
    final File testComplexDocumentFile = JsonSerializerTestFixture.TEST_COMPLEX_DOCUMENT.getFirst();
    builder.add(Arguments.arguments(Named.of(testComplexDocumentFile.getName(), testComplexDocumentFile), JsonSerializerTestFixture.TEST_COMPLEX_DOCUMENT.getSecond()));
    final File testMediumDocumentFile = JsonSerializerTestFixture.TEST_MEDIUM_DOCUMENT.getFirst();
    builder.add(Arguments.arguments(Named.of(testMediumDocumentFile.getName(), testMediumDocumentFile), JsonSerializerTestFixture.TEST_MEDIUM_DOCUMENT.getSecond()));
    final File testMultipleTypeFile = JsonSerializerTestFixture.TEST_MULTIPLE_TYPE.getFirst();
    builder.add(Arguments.arguments(Named.of(testMultipleTypeFile.getName(), testMultipleTypeFile), JsonSerializerTestFixture.TEST_MULTIPLE_TYPE.getSecond()));
    final File testMultipleTypeSingleFile = JsonSerializerTestFixture.TEST_MULTIPLE_TYPE_SINGLE.getFirst();
    builder.add(Arguments.arguments(Named.of(testMultipleTypeSingleFile.getName(), testMultipleTypeSingleFile), JsonSerializerTestFixture.TEST_MULTIPLE_TYPE_SINGLE.getSecond()));
    final File testNestedCollectionsFile = JsonSerializerTestFixture.TEST_NESTED_COLLECTIONS.getFirst();
    builder.add(Arguments.arguments(Named.of(testNestedCollectionsFile.getName(), testNestedCollectionsFile), JsonSerializerTestFixture.TEST_NESTED_COLLECTIONS.getSecond()));
    final File testMapFile = JsonSerializerTestFixture.TEST_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testMapFile.getName(), testMapFile), JsonSerializerTestFixture.TEST_MAP.getSecond()));

    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("getSchemaToTest")
  final void recordSerializersTestLogicalTypes(final File schemaFile, final List<FieldValueMapping> fieldValueMappings) throws Exception {
    final var schemaStr = SerializerTestFixture.readSchema(schemaFile);
    final BaseSchemaMetadata confluentBaseSchemaMetadata =
        new BaseSchemaMetadata<>(
            ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1,
                                                                                                      schemaStr)));

    final ParsedSchema parsedSchema = new ParsedSchema(schemaFile, "JSON");
    JSON_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.JSON, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final var objectNode = JSON_SCHEMA_PROCESSOR.next();

    final var message = SERIALIZER.serialize("the-topic", (ObjectNode) objectNode);

    Assertions.assertThat(message).isNotNull();
  }

}
