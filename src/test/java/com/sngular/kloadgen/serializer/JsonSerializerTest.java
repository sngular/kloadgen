package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_BASIC;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_BASIC_ARRAY;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_BASIC_NUMBER;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_COLLECTIONS;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_COMPLEX_DEFINITIONS;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_COMPLEX_DOCUMENT;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_MAP;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_MEDIUM_DOCUMENT;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_MULTIPLE_TYPE;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_MULTIPLE_TYPE_SINGLE;
import static com.sngular.kloadgen.serializer.JsonSerializerTestFixture.TEST_NESTED_COLLECTIONS;
import static com.sngular.kloadgen.serializer.SerializerTestFixture.readSchema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.testutil.SchemaParseUtil;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JsonSerializerTest {

  private static final SchemaProcessor JSON_SCHEMA_PROCESSOR = new SchemaProcessor();

  private static final GenericJsonRecordSerializer serializer = new GenericJsonRecordSerializer();

  private static Stream<Arguments> getSchemaToTest() {
    Builder<Arguments> builder = Stream.builder();

    File testBasicFile = TEST_BASIC.getFirst();
    builder.add(Arguments.arguments(Named.of(testBasicFile.getName(), testBasicFile), TEST_BASIC.getSecond()));
    File testBasicArraysFile = TEST_BASIC_ARRAY.getFirst();
    builder.add(Arguments.arguments(Named.of(testBasicArraysFile.getName(), testBasicArraysFile), TEST_BASIC_ARRAY.getSecond()));
    File testBasicNumberFile = TEST_BASIC_NUMBER.getFirst();
    builder.add(Arguments.arguments(Named.of(testBasicNumberFile.getName(), testBasicNumberFile), TEST_BASIC_NUMBER.getSecond()));
    File testCollectionsFile = TEST_COLLECTIONS.getFirst();
    builder.add(Arguments.arguments(Named.of(testCollectionsFile.getName(), testCollectionsFile), TEST_COLLECTIONS.getSecond()));
    File testComplexDefinitionsFile = TEST_COMPLEX_DEFINITIONS.getFirst();
    builder.add(Arguments.arguments(Named.of(testComplexDefinitionsFile.getName(), testComplexDefinitionsFile), TEST_COMPLEX_DEFINITIONS.getSecond()));
    File testComplexDocumentFile = TEST_COMPLEX_DOCUMENT.getFirst();
    builder.add(Arguments.arguments(Named.of(testComplexDocumentFile.getName(), testComplexDocumentFile), TEST_COMPLEX_DOCUMENT.getSecond()));
    File testMediumDocumentFile = TEST_MEDIUM_DOCUMENT.getFirst();
    builder.add(Arguments.arguments(Named.of(testMediumDocumentFile.getName(), testMediumDocumentFile), TEST_MEDIUM_DOCUMENT.getSecond()));
    File testMultipleTypeFile = TEST_MULTIPLE_TYPE.getFirst();
    builder.add(Arguments.arguments(Named.of(testMultipleTypeFile.getName(), testMultipleTypeFile), TEST_MULTIPLE_TYPE.getSecond()));
    File testMultipleTypeSingleFile = TEST_MULTIPLE_TYPE_SINGLE.getFirst();
    builder.add(Arguments.arguments(Named.of(testMultipleTypeSingleFile.getName(), testMultipleTypeSingleFile), TEST_MULTIPLE_TYPE_SINGLE.getSecond()));
    File testNestedCollectionsFile = TEST_NESTED_COLLECTIONS.getFirst();
    builder.add(Arguments.arguments(Named.of(testNestedCollectionsFile.getName(), testNestedCollectionsFile), TEST_NESTED_COLLECTIONS.getSecond()));
    File testMapFile = TEST_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testMapFile.getName(), testMapFile), TEST_MAP.getSecond()));

    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("getSchemaToTest")
  void recordSerializersTestLogicalTypes(File schemaFile, List<FieldValueMapping> fieldValueMappings) throws Exception {
    final var schemaStr = readSchema(schemaFile);
    final BaseSchemaMetadata confluentBaseSchemaMetadata =
        new BaseSchemaMetadata<>(
            ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1,
                                                                                                      schemaStr)));

    final ParsedSchema parsedSchema = SchemaParseUtil.getParsedSchema(schemaFile, "JSON");
    JSON_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.JSON, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final var objectNode = JSON_SCHEMA_PROCESSOR.next();

    final var message = serializer.serialize("the-topic", (ObjectNode) objectNode);

    Assertions.assertThat(message).isNotNull();
  }

}
