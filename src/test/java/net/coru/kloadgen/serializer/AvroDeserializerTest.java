package net.coru.kloadgen.serializer;

import static java.util.Arrays.asList;

import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Stream;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class AvroDeserializerTest {

  SchemaProcessor avroSchemaProcessor = new SchemaProcessor();

  private AvroDeserializer avroDeserializer;

  private AvroSerializer avroSerializer;

  @BeforeEach
  void setUp() {
    avroDeserializer = new AvroDeserializer();
    avroSerializer = new AvroSerializer();
  }

  @Test
  void deserialize() throws Exception {
    var SCHEMA = new FileHelper().getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
    var SCHEMA_STR = readSchema(SCHEMA);
    var fieldValueMappings = asList(
        FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityIntArray[2]").fieldType("int-array").valueLength(0).fieldValueList("[1,2]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityRecordArray[2].name").fieldType("string").valueLength(0).fieldValueList("[1,3]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("topLevelIntArray[3]").fieldType("int-array").valueLength(0).fieldValueList("[2,4,5]").required(true).isAncestorRequired(true)
                         .build(),
        FieldValueMapping.builder().fieldName("topLevelRecordArray[3].name").fieldType("string").valueLength(0).fieldValueList("[7,8,9]").required(true).isAncestorRequired(true)
                         .build());
    var metadata = new SchemaMetadata(1, 1, SCHEMA_STR);

    avroDeserializer.configure(Map.of(VALUE_SCHEMA, SCHEMA_STR), false);

    ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(SCHEMA, "AVRO");
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parsedSchema, metadata, fieldValueMappings);
    var record = avroSchemaProcessor.next();

    var message = avroSerializer.serialize("the-topic",
                                           EnrichedRecord
                                               .builder()
                                               .genericRecord(((EnrichedRecord) record).getGenericRecord())
                                               .schemaMetadata(((EnrichedRecord) record).getSchemaMetadata())
                                               .build());

    log.info("[AvroDeserializer] to deserialize = {}", DatatypeConverter.printHexBinary(message));

    var result = avroDeserializer.deserialize("the-topic", message);

    assertThat(result).isNotNull();
  }

  private static String readSchema(File file) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }
}