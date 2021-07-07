package net.coru.kloadgen.processor;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.groovy.util.Maps;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvroSchemaProcessorTest {

  @BeforeEach
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  void textAvroSchemaProcessor() throws KLoadGenException {
    List<FieldValueMapping> fieldValueMappingList = asList(
        new FieldValueMapping("name", "string", 0, "Jose"),
        new FieldValueMapping("age", "int", 0, "43"));
    SchemaProcessorLib avroSchemaProcessor = new SchemaProcessorLib();
    avroSchemaProcessor.processSchema(SchemaBuilder.builder().record("testing").fields().requiredString("name").optionalInt("age").endRecord(),
        new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(message.getGenericRecord()).hasFieldOrPropertyWithValue("values", asList("Jose", 43).toArray());

  }

  @Test
  void textAvroSchemaProcessorArrayMap() throws KLoadGenException {
    List<FieldValueMapping> fieldValueMappingList = Collections.singletonList(
        new FieldValueMapping("values[2][]", "string-map-array", 2, "n:1, t:2"));

    SchemaProcessorLib avroSchemaProcessor = new SchemaProcessorLib();
    avroSchemaProcessor.processSchema(SchemaBuilder
        .builder()
        .record("arrayMap")
        .fields()
        .name("values")
        .type()
        .array()
        .items()
        .type(SchemaBuilder
            .builder()
            .map()
            .values()
            .stringType()
            .getValueType())
        .noDefault()
        .endRecord(),
        new SchemaMetadata(1, 1, ""),
        fieldValueMappingList);

    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message)
        .isNotNull()
        .isInstanceOf(EnrichedRecord.class)
        .extracting(EnrichedRecord::getGenericRecord)
        .isNotNull()
        .hasFieldOrProperty("values")
        .extracting("values")
        .extracting(Arrays::asList)
        .asList()
        .hasSize(1);
    List<Map<String, Object>> result = (List<Map<String, Object>>) ((GenericRecord) message.getGenericRecord()).get("values");
    assertThat(result).hasSize(2).contains(Maps.of("n", "1", "t", "2"), Maps.of("n", "1", "t", "2"));
  }

  @Test
  void textAvroSchemaProcessorArrayRecord() throws KLoadGenException {
    List<FieldValueMapping> fieldValueMappingList = asList(
        new FieldValueMapping("values[2].name", "string", 2, "Jose, Andres"),
        new FieldValueMapping("values[].amount", "float", 2, "0.5, 0.6"));

    SchemaProcessorLib avroSchemaProcessor = new SchemaProcessorLib();
    avroSchemaProcessor.processSchema(SchemaBuilder
            .builder()
            .record("array")
            .fields()
            .name("values")
            .type()
            .array()
            .items()
            .type(SchemaBuilder
                .builder()
                .record("test")
                .fields()
                .requiredString("name")
                .requiredFloat("amount")
                .endRecord())
            .noDefault()
            .endRecord(),
        new SchemaMetadata(1, 1, ""),
        fieldValueMappingList);

    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message)
        .isNotNull()
        .isInstanceOf(EnrichedRecord.class)
        .extracting(EnrichedRecord::getGenericRecord)
        .isNotNull()
        .hasFieldOrProperty("values")
        .extracting("values")
        .extracting(Arrays::asList)
        .asList()
        .hasSize(1);
  }

  @Test
  void textAvroSchemaProcessorMap() throws KLoadGenException {
    List<FieldValueMapping> fieldValueMappingList = Collections.singletonList(
        new FieldValueMapping("values[4]", "string-map", 2, "n:1, t:2"));

    SchemaProcessorLib avroSchemaProcessor = new SchemaProcessorLib();
    avroSchemaProcessor.processSchema(SchemaBuilder
            .builder()
            .record("arrayMap")
            .fields()
            .name("values")
            .type()
            .array()
            .items()
            .type(SchemaBuilder
                .builder()
                .map()
                .values()
                .stringType()
                .getValueType())
            .noDefault()
            .endRecord(),
        new SchemaMetadata(1, 1, ""),
        fieldValueMappingList);

    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message)
            .isNotNull()
            .isInstanceOf(EnrichedRecord.class)
            .extracting(EnrichedRecord::getGenericRecord)
            .isNotNull()
            .hasFieldOrProperty("values")
            .extracting("values")
            .extracting(Arrays::asList)
            .asList()
            .hasSize(1);
    Map<String, String> result = (Map<String, String>)((GenericRecord) message.getGenericRecord()).get("values");
    assertThat(result).hasSize(4).containsEntry("n","1").containsEntry("t","2");
  }
}