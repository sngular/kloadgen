package com.sngular.kloadgen.serializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.extractor.impl.SchemaExtractorImpl;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  @Test
  void deserialize() throws Exception {
    final var schemaFile = new FileHelper().getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
    final var schemaStr = readSchema(schemaFile);
    final var fieldValueMappings = Arrays.asList(
        FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityIntArray[2]").fieldType("int-array").valueLength(0).fieldValueList("[1,2]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityRecordArray[2].name").fieldType("string").valueLength(0).fieldValueList("[1,3]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("topLevelIntArray[3]").fieldType("int-array").valueLength(0).fieldValueList("[2,4,5]").required(true).isAncestorRequired(true)
                         .build(),
        FieldValueMapping.builder().fieldName("topLevelRecordArray[3].name").fieldType("string").valueLength(0).fieldValueList("[7,8,9]").required(true).isAncestorRequired(true)
                         .build());
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

  private static String readSchema(final File file) throws IOException {
    final StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }
}