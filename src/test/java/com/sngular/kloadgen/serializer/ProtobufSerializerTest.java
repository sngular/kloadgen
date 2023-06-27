package com.sngular.kloadgen.serializer;

import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_COMPLETE_PROTO;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_COMPLEX;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_DATE_TIME;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_DEVE;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_EASY;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_EMBEDDED_TYPE;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_ENUM;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_GOOGLE_TYPES;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_ISSUE_311;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_MAP;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_ONE_OF;
import static com.sngular.kloadgen.serializer.ProtobuffSerializerTestFixture.TEST_PROVIDED;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.testutil.SchemaParseUtil;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class ProtobufSerializerTest {

  private ProtobufSerializer protobufSerializer;

  @BeforeEach
  void setUp() {
    protobufSerializer = new ProtobufSerializer();
  }

  private static Stream<Arguments> getSchemaToTest() {
    Builder<Arguments> builder = Stream.builder();

    File testCompleteProtoFile = TEST_COMPLETE_PROTO.getFirst();
    builder.add(Arguments.arguments(Named.of(testCompleteProtoFile.getName(), testCompleteProtoFile), TEST_COMPLETE_PROTO.getSecond()));
    File testComplexFile = TEST_COMPLEX.getFirst();
    builder.add(Arguments.arguments(Named.of(testComplexFile.getName(), testComplexFile), TEST_COMPLEX.getSecond()));
    File testDateTimeFile = TEST_DATE_TIME.getFirst();
    builder.add(Arguments.arguments(Named.of(testDateTimeFile.getName(), testDateTimeFile), TEST_DATE_TIME.getSecond()));
    File testDeveFile = TEST_DEVE.getFirst();
    builder.add(Arguments.arguments(Named.of(testDeveFile.getName(), testDeveFile), TEST_DEVE.getSecond()));
    File testEasyFile = TEST_EASY.getFirst();
    builder.add(Arguments.arguments(Named.of(testEasyFile.getName(), testEasyFile), TEST_EASY.getSecond()));
    File testEmbeddedTypeFile = TEST_EMBEDDED_TYPE.getFirst();
    builder.add(Arguments.arguments(Named.of(testEmbeddedTypeFile.getName(), testEmbeddedTypeFile), TEST_EMBEDDED_TYPE.getSecond()));
    File testEnumFile = TEST_ENUM.getFirst();
    builder.add(Arguments.arguments(Named.of(testEnumFile.getName(), testEnumFile), TEST_ENUM.getSecond()));
    File testGoogleTypesFile = TEST_GOOGLE_TYPES.getFirst();
    builder.add(Arguments.arguments(Named.of(testGoogleTypesFile.getName(), testGoogleTypesFile), TEST_GOOGLE_TYPES.getSecond()));
    File testIssue311File = TEST_ISSUE_311.getFirst();
    builder.add(Arguments.arguments(Named.of(testIssue311File.getName(), testIssue311File), TEST_ISSUE_311.getSecond()));
    File testMapFile = TEST_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testMapFile.getName(), testMapFile), TEST_MAP.getSecond()));
    File testOneOfFile = TEST_ONE_OF.getFirst();
    builder.add(Arguments.arguments(Named.of(testOneOfFile.getName(), testOneOfFile), TEST_ONE_OF.getSecond()));
    File testProvidedFile = TEST_PROVIDED.getFirst();
    builder.add(Arguments.arguments(Named.of(testProvidedFile.getName(), testProvidedFile), TEST_PROVIDED.getSecond()));

    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("getSchemaToTest")
  void serialize(File schemaFile, List<FieldValueMapping> fieldValueMappings) throws IOException {
    final ParsedSchema parsedSchema = SchemaParseUtil.getParsedSchema(schemaFile, "Protobuf");
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    final BaseSchemaMetadata confluentBaseSchemaMetadata =
        new BaseSchemaMetadata<>(
            ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1, "")));
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);

    final var generatedRecord = protobufSchemaProcessor.next();

    final var message = protobufSerializer.serialize("the-topic", EnrichedRecord.builder()
                                                                                .genericRecord(((EnrichedRecord) generatedRecord).getGenericRecord())
                                                                                .schemaMetadata(((EnrichedRecord) generatedRecord).getSchemaMetadata())
                                                                                .build());
    Assertions.assertThat(message).isNotNull();
  }
}
