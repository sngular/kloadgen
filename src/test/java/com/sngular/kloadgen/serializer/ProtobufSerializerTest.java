package com.sngular.kloadgen.serializer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
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
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
    JMeterContextService.getContext().getProperties().put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "CONFLUENT");
  }

  private static Stream<Arguments> getSchemaToTest() {
    final Builder<Arguments> builder = Stream.builder();

    final File testCompleteProtoFile = ProtobufSerializerTestFixture.TEST_COMPLETE_PROTO.getFirst();
    builder.add(Arguments.arguments(Named.of(testCompleteProtoFile.getName(), testCompleteProtoFile), ProtobufSerializerTestFixture.TEST_COMPLETE_PROTO.getSecond()));
    final File testComplexFile = ProtobufSerializerTestFixture.TEST_COMPLEX.getFirst();
    builder.add(Arguments.arguments(Named.of(testComplexFile.getName(), testComplexFile), ProtobufSerializerTestFixture.TEST_COMPLEX.getSecond()));
    final File testDateTimeFile = ProtobufSerializerTestFixture.TEST_DATE_TIME.getFirst();
    builder.add(Arguments.arguments(Named.of(testDateTimeFile.getName(), testDateTimeFile), ProtobufSerializerTestFixture.TEST_DATE_TIME.getSecond()));
    final File testDeveFile = ProtobufSerializerTestFixture.TEST_DEVE.getFirst();
    builder.add(Arguments.arguments(Named.of(testDeveFile.getName(), testDeveFile), ProtobufSerializerTestFixture.TEST_DEVE.getSecond()));
    final File testEasyFile = ProtobufSerializerTestFixture.TEST_EASY.getFirst();
    builder.add(Arguments.arguments(Named.of(testEasyFile.getName(), testEasyFile), ProtobufSerializerTestFixture.TEST_EASY.getSecond()));
    final File testEmbeddedTypeFile = ProtobufSerializerTestFixture.TEST_EMBEDDED_TYPE.getFirst();
    builder.add(Arguments.arguments(Named.of(testEmbeddedTypeFile.getName(), testEmbeddedTypeFile), ProtobufSerializerTestFixture.TEST_EMBEDDED_TYPE.getSecond()));
    final File testEnumFile = ProtobufSerializerTestFixture.TEST_ENUM.getFirst();
    builder.add(Arguments.arguments(Named.of(testEnumFile.getName(), testEnumFile), ProtobufSerializerTestFixture.TEST_ENUM.getSecond()));
    final File testGoogleTypesFile = ProtobufSerializerTestFixture.TEST_GOOGLE_TYPES.getFirst();
    builder.add(Arguments.arguments(Named.of(testGoogleTypesFile.getName(), testGoogleTypesFile), ProtobufSerializerTestFixture.TEST_GOOGLE_TYPES.getSecond()));
    final File testIssue311File = ProtobufSerializerTestFixture.TEST_ISSUE_311.getFirst();
    builder.add(Arguments.arguments(Named.of(testIssue311File.getName(), testIssue311File), ProtobufSerializerTestFixture.TEST_ISSUE_311.getSecond()));
    final File testMapFile = ProtobufSerializerTestFixture.TEST_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testMapFile.getName(), testMapFile), ProtobufSerializerTestFixture.TEST_MAP.getSecond()));
    final File testOneOfFile = ProtobufSerializerTestFixture.TEST_ONE_OF.getFirst();
    builder.add(Arguments.arguments(Named.of(testOneOfFile.getName(), testOneOfFile), ProtobufSerializerTestFixture.TEST_ONE_OF.getSecond()));
    final File testProvidedFile = ProtobufSerializerTestFixture.TEST_PROVIDED.getFirst();
    builder.add(Arguments.arguments(Named.of(testProvidedFile.getName(), testProvidedFile), ProtobufSerializerTestFixture.TEST_PROVIDED.getSecond()));

    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("getSchemaToTest")
  void serialize(final File schemaFile, final List<FieldValueMapping> fieldValueMappings) throws IOException {
    final ParsedSchema parsedSchema = new ParsedSchema(schemaFile, "PROTOBUF");
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    final BaseSchemaMetadata confluentBaseSchemaMetadata =
        new BaseSchemaMetadata<>(
            ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1, "")));
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, parsedSchema.rawSchema(), confluentBaseSchemaMetadata, fieldValueMappings);

    final var generatedRecord = protobufSchemaProcessor.next();

    final var message = protobufSerializer.serialize("the-topic", EnrichedRecord.builder()
                                                                                .genericRecord(((EnrichedRecord) generatedRecord).getGenericRecord())
                                                                                .schemaMetadata(((EnrichedRecord) generatedRecord).getSchemaMetadata())
                                                                                .build());
    Assertions.assertThat(message).isNotNull();
  }
}
