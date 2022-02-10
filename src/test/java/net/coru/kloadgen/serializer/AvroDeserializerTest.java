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
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.AvroSchemaProcessor;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class AvroDeserializerTest {

  private AvroDeserializer avroDeserializer;

  private AvroSerializer avroSerializer;

  AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();

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
        new FieldValueMapping("subEntity.anotherLevel.subEntityIntArray[2]", "int-array", 0, "[1]"),
        new FieldValueMapping("subEntity.anotherLevel.subEntityRecordArray[2].name", "string", 0, "[1]"),
        new FieldValueMapping("topLevelIntArray[3]", "int-array", 0, "[2]"),
        new FieldValueMapping("topLevelRecordArray[3].name", "string", 0, "[2]")
    );
    var metadata = new SchemaMetadata(1, 1, SCHEMA_STR);


    avroDeserializer.configure(Map.of(VALUE_SCHEMA, SCHEMA_STR), false);

    ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(SCHEMA, "AVRO");
    avroSchemaProcessor.processSchema(parsedSchema, metadata, fieldValueMappings);
    var record = avroSchemaProcessor.next();

    var message = avroSerializer.serialize("the-topic",
                             EnrichedRecord
                                 .builder()
                                 .genericRecord(record.getGenericRecord())
                                 .schemaMetadata(record.getSchemaMetadata())
                                 .build());

    log.info("[AvroDeserializer] to deserialize = {}", message);

    var result = avroDeserializer.deserialize("the-topic", message);

    assertThat(result).isNotNull();
  }

  private static String readSchema(File file) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8))
    {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }
}