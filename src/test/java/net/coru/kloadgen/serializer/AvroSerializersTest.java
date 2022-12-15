package net.coru.kloadgen.serializer;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

public class AvroSerializersTest {

    private static final SchemaProcessor AVRO_SCHEMA_PROCESSOR = new SchemaProcessor();

    @BeforeEach
    void setUp() {
        final var genericData = GenericData.get();

        genericData.addLogicalTypeConversion(new TimeConversions.DateConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.LocalTimestampMicrosConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.LocalTimestampMillisConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.TimeMicrosConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
        genericData.addLogicalTypeConversion(new Conversions.DecimalConversion());
        genericData.addLogicalTypeConversion(new Conversions.UUIDConversion());
    }

    @ParameterizedTest
    @MethodSource("getSerializers")
    void recordSerializersTestLogicalTypes(Serializer serializer) throws Exception {
        final var schemaFile = new FileHelper().getFile("/avro-files/testLogicalTypes.avsc");
        final var schemaStr = readSchema(schemaFile);
        final var fieldValueMappings = Arrays.asList(
                FieldValueMapping.builder().fieldName("Date").fieldType("int_date").valueLength(0).fieldValueList("[]").required(true)
                        .isAncestorRequired(true).build(),
                FieldValueMapping.builder().fieldName("TimeMillis").fieldType("int_time-millis").valueLength(0).fieldValueList("[]").required(true)
                        .isAncestorRequired(true).build(),
                FieldValueMapping.builder().fieldName("TimeMicros").fieldType("long_time-micros").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("TimestampMillis").fieldType("long_timestamp-millis").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("TimestampMicros").fieldType("long_timestamp-micros").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("LocalTimestampMillis").fieldType("long_local-timestamp-millis").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("LocalTimestampMicros").fieldType("long_local-timestamp-micros").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("UUID").fieldType("string_uuid").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("Decimal").fieldType("bytes_decimal").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build(),
                FieldValueMapping.builder().fieldName("DecimalFixed").fieldType("fixed_decimal").valueLength(0).fieldValueList("[]").required(true).isAncestorRequired(true)
                        .build());
        final var metadata = new SchemaMetadata(1, 1, schemaStr);

        final ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(schemaFile, "AVRO");
        AVRO_SCHEMA_PROCESSOR.processSchema(SchemaTypeEnum.AVRO, parsedSchema, metadata, fieldValueMappings);
        final var generatedRecord = AVRO_SCHEMA_PROCESSOR.next();

        final var message = serializer.serialize("the-topic", (GenericRecord) ((EnrichedRecord) generatedRecord).getGenericRecord());

        Assertions.assertThat(message).isNotNull();
    }

    private static Stream<Arguments> getSerializers() {
        return Stream.of(
                Arguments.arguments(new GenericAvroRecordSerializer()),
                Arguments.arguments(new GenericAvroRecordBinarySerializer())
        );
    }

    private static String readSchema(final File file) throws IOException {
        final StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }

        return contentBuilder.toString();
    }
}
