package com.sngular.kloadgen.serializer;

import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_FILE_ISSUE;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_ISSUE;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_LOGICAL_TYPES;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_MAP;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_NULL_ON_OPTIONAL_FIELDS;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_OPTIONAL_ENUM;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_OPTIONAL_MAP;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_SUBENTITY_ARRAY;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_UNION_RECORD;
import static com.sngular.kloadgen.serializer.AvroSerializersTestFixture.TEST_USER;
import static com.sngular.kloadgen.serializer.SerializerTestFixture.readSchema;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.extractor.impl.SchemaExtractorImpl;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.util.PropsKeysHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import javax.xml.bind.DatatypeConverter;
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

    Builder<Arguments> builder = Stream.builder();

    File testSubentityArrayFile = TEST_SUBENTITY_ARRAY.getFirst();
    builder.add(Arguments.arguments(Named.of(testSubentityArrayFile.getName(), testSubentityArrayFile), TEST_SUBENTITY_ARRAY.getSecond()));
    File testIssueFile = TEST_ISSUE.getFirst();
    builder.add(Arguments.arguments(Named.of(testIssueFile.getName(), testIssueFile), TEST_ISSUE.getSecond()));
    File testOptionalEnumFile = TEST_OPTIONAL_ENUM.getFirst();
    builder.add(Arguments.arguments(Named.of(testOptionalEnumFile.getName(), testOptionalEnumFile), TEST_OPTIONAL_ENUM.getSecond()));
    File testFileIssueFile = TEST_FILE_ISSUE.getFirst();
    builder.add(Arguments.arguments(Named.of(testFileIssueFile.getName(), testFileIssueFile), TEST_FILE_ISSUE.getSecond()));
    File testLogicalTypesFile = TEST_LOGICAL_TYPES.getFirst();
    builder.add(Arguments.arguments(Named.of(testLogicalTypesFile.getName(), testLogicalTypesFile), TEST_LOGICAL_TYPES.getSecond()));
    File testMapFile = TEST_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testMapFile.getName(), testMapFile), TEST_MAP.getSecond()));
    File testNullOnOptionalFieldsFile = TEST_NULL_ON_OPTIONAL_FIELDS.getFirst();
    builder.add(
        Arguments.arguments(Named.of(testNullOnOptionalFieldsFile.getName(), testNullOnOptionalFieldsFile), TEST_NULL_ON_OPTIONAL_FIELDS.getSecond()));
    File testOptionalMapFile = TEST_OPTIONAL_MAP.getFirst();
    builder.add(Arguments.arguments(Named.of(testOptionalMapFile.getName(), testOptionalMapFile), TEST_OPTIONAL_MAP.getSecond()));
    File testNullOnOptionalFields = TEST_NULL_ON_OPTIONAL_FIELDS.getFirst();
    builder.add(Arguments.arguments(Named.of(testNullOnOptionalFields.getName(), testNullOnOptionalFields), TEST_NULL_ON_OPTIONAL_FIELDS.getSecond()));
    File testUnionRecordFile = TEST_UNION_RECORD.getFirst();
    builder.add(Arguments.arguments(Named.of(testUnionRecordFile.getName(), testUnionRecordFile), TEST_UNION_RECORD.getSecond()));
    File testUserFile = TEST_USER.getFirst();
    builder.add(Arguments.arguments(Named.of(testUserFile.getName(), testUserFile), TEST_USER.getSecond()));

    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("getSchemaToTest")
  void deserialize(File schemaFile, List<FieldValueMapping> fieldValueMappings) throws Exception {
    final var schemaStr = readSchema(schemaFile);
    final var metadata = new SchemaMetadata(1, 1, schemaStr);

    avroDeserializer.configure(Map.of(PropsKeysHelper.VALUE_SCHEMA, schemaStr), false);

    final ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(schemaFile, "AVRO");
    AVRO_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.AVRO, parsedSchema, metadata, fieldValueMappings);
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