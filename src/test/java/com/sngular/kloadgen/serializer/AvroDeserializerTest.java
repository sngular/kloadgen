package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.bind.DatatypeConverter;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.util.PropsKeysHelper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class AvroDeserializerTest {

  private static final SchemaProcessor AVRO_SCHEMA_PROCESSOR = new SchemaProcessor();

  private AvroDeserializer avroDeserializer;

  private AvroSerializer avroSerializer;

  @BeforeEach
  void setUp() {
    avroDeserializer = new AvroDeserializer();
    avroSerializer = new AvroSerializer();
  }

  private static Stream<Arguments> getSchemaToTest() {

    final Builder<Arguments> builder = Stream.builder();

    final File testSubentityArrayFile = AvroSerializersTestFixture.TEST_SUBENTITY_ARRAY.getFirst();
    builder.add(Arguments.arguments(Named.of(testSubentityArrayFile.getName(), testSubentityArrayFile), AvroSerializersTestFixture.TEST_SUBENTITY_ARRAY.getSecond()));
    final File testIssueFile = AvroSerializersTestFixture.TEST_ISSUE.getFirst();
    builder.add(Arguments.arguments(Named.of(testIssueFile.getName(), testIssueFile), AvroSerializersTestFixture.TEST_ISSUE.getSecond()));
    final File testOptionalEnumFile = AvroSerializersTestFixture.TEST_OPTIONAL_ENUM.getFirst();
    builder.add(Arguments.arguments(Named.of(testOptionalEnumFile.getName(), testOptionalEnumFile), AvroSerializersTestFixture.TEST_OPTIONAL_ENUM.getSecond()));
    final File testFileIssueFile = AvroSerializersTestFixture.TEST_FILE_ISSUE.getFirst();
    builder.add(Arguments.arguments(Named.of(testFileIssueFile.getName(), testFileIssueFile), AvroSerializersTestFixture.TEST_FILE_ISSUE.getSecond()));
    final File testLogicalTypesFile = AvroSerializersTestFixture.TEST_LOGICAL_TYPES.getFirst();
    builder.add(Arguments.arguments(Named.of(testLogicalTypesFile.getName(), testLogicalTypesFile), AvroSerializersTestFixture.TEST_LOGICAL_TYPES.getSecond()));
    final File testMapFile = AvroSerializersTestFixture.TEST_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testMapFile.getName(), testMapFile), AvroSerializersTestFixture.TEST_MAP.getSecond()));
    final File testNullOnOptionalFieldsFile = AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getFirst();
    builder.add(
        Arguments.arguments(Named.of(testNullOnOptionalFieldsFile.getName(), testNullOnOptionalFieldsFile), AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getSecond()));
    final File testOptionalMapFile = AvroSerializersTestFixture.TEST_OPTIONAL_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testOptionalMapFile.getName(), testOptionalMapFile), AvroSerializersTestFixture.TEST_OPTIONAL_MAP.getSecond()));
    final File testNullOnOptionalFields = AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getFirst();
    builder.add(Arguments.arguments(Named.of(testNullOnOptionalFields.getName(), testNullOnOptionalFields), AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS.getSecond()));
    final File testUnionRecordFile = AvroSerializersTestFixture.TEST_UNION_RECORD.getFirst();
    builder.add(Arguments.arguments(Named.of(testUnionRecordFile.getName(), testUnionRecordFile), AvroSerializersTestFixture.TEST_UNION_RECORD.getSecond()));
    final File testUserFile = AvroSerializersTestFixture.TEST_USER.getFirst();
    builder.add(Arguments.arguments(Named.of(testUserFile.getName(), testUserFile), AvroSerializersTestFixture.TEST_USER.getSecond()));

    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("getSchemaToTest")
  void deserialize(final File schemaFile, final List<FieldValueMapping> fieldValueMappings) throws Exception {
    final var schemaStr = SerializerTestFixture.readSchema(schemaFile);
    final BaseSchemaMetadata confluentBaseSchemaMetadata =
        new BaseSchemaMetadata<>(
            ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1,
                                                                                                      schemaStr)));

    avroDeserializer.configure(Map.of(PropsKeysHelper.VALUE_SCHEMA, schemaStr), false);

    final ParsedSchema parsedSchema = new ParsedSchema(schemaFile, "AVRO");
    AVRO_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.AVRO, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final var generatedRecord = AVRO_SCHEMA_PROCESSOR.next();

    final var message = avroSerializer.serialize("the-topic",
                                                 EnrichedRecord
                                                     .builder()
                                                     .genericRecord(((EnrichedRecord) generatedRecord).getGenericRecord())
                                                     .schemaMetadata(((EnrichedRecord) generatedRecord).getSchemaMetadata())
                                                     .build());

    log.info("[AvroDeserializer] to deserialize = {}", DatatypeConverter.printHexBinary(message));

    final var result = avroDeserializer.deserialize("the-topic", message);

    Assertions.assertThat(result).isNotNull();
  }
}
