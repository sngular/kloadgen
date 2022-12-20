package com.sngular.kloadgen.serializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.extractor.impl.SchemaExtractorImpl;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.testutil.FileHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AvroSerializersTest {

  private static final SchemaProcessor AVRO_SCHEMA_PROCESSOR = new SchemaProcessor();

  private static Stream<Arguments> getSerializers() {
    return Stream.of(
        Arguments.arguments(new GenericAvroRecordSerializer<>()),
        Arguments.arguments(new GenericAvroRecordBinarySerializer<>())
    );
  }

  @ParameterizedTest
  @MethodSource("getSerializers")
  void recordSerializersTestLogicalTypes(final Serializer<GenericRecord> serializer) throws Exception {
    final var schemaFile = new FileHelper().getFile("/avro-files/testLogicalTypes.avsc");
    final var schemaStr = readSchema(schemaFile);
    final var fieldValueMappings = Arrays.asList(
        createFieldValueMapping("Date", "int_date"),
        createFieldValueMapping("TimeMillis", "int_time-millis"),
        createFieldValueMapping("TimeMicros", "long_time-micros"),
        createFieldValueMapping("TimestampMillis", "long_timestamp-millis"),
        createFieldValueMapping("TimestampMicros", "long_timestamp-micros"),
        createFieldValueMapping("LocalTimestampMillis", "long_local-timestamp-millis"),
        createFieldValueMapping("LocalTimestampMicros", "long_local-timestamp-micros"),
        createFieldValueMapping("UUID", "string_uuid"),
        createFieldValueMapping("Decimal", "bytes_decimal"),
        createFieldValueMapping("DecimalFixed", "fixed_decimal"));
    final var metadata = new SchemaMetadata(1, 1, schemaStr);

    final ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(schemaFile, "AVRO");
    AVRO_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.AVRO, parsedSchema, metadata, fieldValueMappings);
    final var generatedRecord = AVRO_SCHEMA_PROCESSOR.next();

    final var message = serializer.serialize("the-topic", (GenericRecord) ((EnrichedRecord) generatedRecord).getGenericRecord());

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
    return FieldValueMapping.builder().fieldName(name).fieldType(fieldType).valueLength(0).fieldValueList("[]").required(true)
                            .isAncestorRequired(true).build();
  }
}
