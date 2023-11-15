package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GenericRecordAvroSerializersTest {

  private static final SchemaProcessor AVRO_SCHEMA_PROCESSOR = new SchemaProcessor();

  private static Stream<Arguments> getSerializerAndSchemaToTest() {

    final Builder<Arguments> builder = Stream.builder();
    AvroSerializersTestFixture.SERIALIZER_LIST.forEach(serializer -> extracted(serializer, builder));
    return builder.build();
  }

  private static void extracted(final Serializer<GenericRecord> serializer, final Builder<Arguments> builder) {
    final Named<Serializer<GenericRecord>> serializerArgument = Named.of(serializer.getClass().getName(), serializer);
    final File testSubentityArrayFile = AvroSerializersTestFixture.TEST_SUBENTITY_ARRAY.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testSubentityArrayFile.getName(), testSubentityArrayFile),
                                    AvroSerializersTestFixture.TEST_SUBENTITY_ARRAY.getSecond()));
    final File testEmbededAvrosExampleFile = AvroSerializersTestFixture.TEST_EMBEDED_AVROS_EXAMPLE.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testEmbededAvrosExampleFile.getName(), testEmbededAvrosExampleFile),
                                    AvroSerializersTestFixture.TEST_EMBEDED_AVROS_EXAMPLE.getSecond()));
    final File testIssueFile = AvroSerializersTestFixture.TEST_ISSUE.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testIssueFile.getName(), testIssueFile), AvroSerializersTestFixture.TEST_ISSUE.getSecond()));
    final File testOptionalEnumFile = AvroSerializersTestFixture.TEST_OPTIONAL_ENUM.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testOptionalEnumFile.getName(), testOptionalEnumFile), AvroSerializersTestFixture.TEST_OPTIONAL_ENUM.getSecond()));
    final File testFileIssueFile = AvroSerializersTestFixture.TEST_FILE_ISSUE.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testFileIssueFile.getName(), testFileIssueFile), AvroSerializersTestFixture.TEST_FILE_ISSUE.getSecond()));
    final File testLogicalTypesFile = AvroSerializersTestFixture.TEST_LOGICAL_TYPES.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testLogicalTypesFile.getName(), testLogicalTypesFile), AvroSerializersTestFixture.TEST_LOGICAL_TYPES.getSecond()));
    final File testMapFile = AvroSerializersTestFixture.TEST_MAP.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testMapFile.getName(), testMapFile), AvroSerializersTestFixture.TEST_MAP.getSecond()));
    final File testNullOnOptionalFieldsFile = AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getFirst();
    builder.add(
        Arguments.arguments(serializerArgument, Named.of(testNullOnOptionalFieldsFile.getName(), testNullOnOptionalFieldsFile),
                            AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getSecond()));
    final File testOptionalMapFile = AvroSerializersTestFixture.TEST_OPTIONAL_MAP.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testOptionalMapFile.getName(), testOptionalMapFile), AvroSerializersTestFixture.TEST_OPTIONAL_MAP.getSecond()));
    final File testNullOnOptionalFields = AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testNullOnOptionalFields.getName(), testNullOnOptionalFields),
                                    AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getSecond()));
    final File testUnionRecordFile = AvroSerializersTestFixture.TEST_UNION_RECORD.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testUnionRecordFile.getName(), testUnionRecordFile), AvroSerializersTestFixture.TEST_UNION_RECORD.getSecond()));
    final File testUserFile = AvroSerializersTestFixture.TEST_USER.getFirst();
    builder.add(Arguments.arguments(serializerArgument, Named.of(testUserFile.getName(), testUserFile), AvroSerializersTestFixture.TEST_USER.getSecond()));
  }

  @ParameterizedTest
  @MethodSource("getSerializerAndSchemaToTest")
  void genericAvroRecordSerializerTest(final Serializer<GenericRecord> serializer, final File schemaFile, final List<FieldValueMapping> fieldValueMappings) throws Exception {
    final var schemaStr = SerializerTestFixture.readSchema(schemaFile);
    final BaseSchemaMetadata confluentBaseSchemaMetadata =
        new BaseSchemaMetadata<>(
            ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1,
                                                                                                      schemaStr)));

    final ParsedSchema parsedSchema = new ParsedSchema(schemaFile, "AVRO");
    AVRO_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.AVRO, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final var generatedRecord = AVRO_SCHEMA_PROCESSOR.next();

    final var message = serializer.serialize("the-topic", (GenericRecord) ((EnrichedRecord) generatedRecord).getGenericRecord());

    Assertions.assertThat(message).isNotNull();
  }


}
