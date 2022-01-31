/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.groovy.util.Maps;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class AvroSchemaProcessorTest {

    private final FileHelper fileHelper = new FileHelper();
    private final SchemaExtractor schemaExtractor = new SchemaExtractorImpl();

    @BeforeEach
    public void setUp() {
        File file = new File("src/test/resources");
        String absolutePath = file.getAbsolutePath();
        JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
        JMeterContext jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());
        JMeterUtils.setLocale(Locale.ENGLISH);
    }

    private GenericRecord setUpEntityForAvroTestWithSubEntitySimpleArray(ParsedSchema parsedSchema) {
        GenericRecord entity = new GenericData.Record((Schema) parsedSchema.rawSchema());
        Schema subEntitySchema = entity.getSchema().getField("subEntity").schema();
        GenericRecord subEntityRecord = new GenericData.Record(subEntitySchema);
        Schema anotherLevelSchema = subEntitySchema.getField("anotherLevel").schema();
        GenericRecord anotherLevelRecord = new GenericData.Record(anotherLevelSchema);
        anotherLevelRecord.put("subEntityIntArray", asList(1, 1));
        subEntityRecord.put("anotherLevel", anotherLevelRecord);

        entity.put("subEntity", subEntityRecord);
        entity.put("topLevelIntArray", asList(2, 2, 2));

        return entity;
    }

    @Test
    void testAvroSchemaProcessorWithSubEntitySimpleArray() throws IOException {
        List<FieldValueMapping> fieldValueMappings = asList(
                new FieldValueMapping("subEntity.anotherLevel.subEntityIntArray[2]", "int-array", 0, "[1]"),
                new FieldValueMapping("topLevelIntArray[3]", "int-array", 0, "[2]")
        );

        File testFile = fileHelper.getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
        ParsedSchema parsedSchema = schemaExtractor.schemaTypesList(testFile, "AVRO");
        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
        avroSchemaProcessor.processSchema(parsedSchema, new SchemaMetadata(1, 1, ""), fieldValueMappings);
        GenericRecord entity = setUpEntityForAvroTestWithSubEntitySimpleArray(parsedSchema);
        EnrichedRecord message = avroSchemaProcessor.next();

        assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
        assertThat(message.getGenericRecord()).isNotNull();
        assertThat(message.getGenericRecord()).isEqualTo(entity);
    }

    private GenericRecord setUpEntityForAvroTestWithSubEntityArray(ParsedSchema parsedSchema) {
        GenericRecord entity = new GenericData.Record((Schema) parsedSchema.rawSchema());
        Schema subEntitySchema = entity.getSchema().getField("subEntity").schema();
        GenericRecord subEntityRecord = new GenericData.Record(subEntitySchema);
        Schema anotherLevelSchema = subEntitySchema.getField("anotherLevel").schema();
        Schema subEntityRecordArraySchema = anotherLevelSchema.getField("subEntityRecordArray").schema().getElementType();
        GenericRecord anotherLevelRecord = new GenericData.Record(anotherLevelSchema);
        GenericRecord subEntityItemsRecord = new GenericData.Record(subEntityRecordArraySchema);
        subEntityItemsRecord.put("name", "second");
        anotherLevelRecord.put("subEntityRecordArray", asList(subEntityItemsRecord, subEntityItemsRecord));

        subEntityRecord.put("anotherLevel", anotherLevelRecord);
        entity.put("subEntity", subEntityRecord);

        GenericRecord topLevelRecordArray =
                new GenericData.Record(entity.getSchema().getField("topLevelRecordArray").schema().getElementType());
        topLevelRecordArray.put("name", "third");
        entity.put("topLevelRecordArray", singletonList(topLevelRecordArray));

        return entity;
    }

    @Test
    void testAvroSchemaProcessorWithSubEntityArray() throws IOException {
        List<FieldValueMapping> fieldValueMappings = asList(
                new FieldValueMapping("subEntity.anotherLevel.subEntityRecordArray[2].name", "string", 0, "second"),
                new FieldValueMapping("topLevelRecordArray[1].name", "string", 0, "third")
        );

        File testFile = fileHelper.getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
        ParsedSchema parsedSchema = schemaExtractor.schemaTypesList(testFile, "AVRO");
        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
        avroSchemaProcessor.processSchema(parsedSchema, new SchemaMetadata(1, 1, ""), fieldValueMappings);
        GenericRecord entity = setUpEntityForAvroTestWithSubEntityArray(parsedSchema);
        EnrichedRecord message = avroSchemaProcessor.next();

        assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
        assertThat(message.getGenericRecord()).isNotNull();
        assertThat(message.getGenericRecord()).isEqualTo(entity);
    }

    private GenericRecord setUpEntityForEmbeddedAvroTest(ParsedSchema parsedSchema) {
        GenericRecord entity = new GenericData.Record((Schema) parsedSchema.rawSchema());
        GenericRecord subEntityFieldMySchema = new GenericData.Record(entity.getSchema().getField("fieldMySchema").schema());

        subEntityFieldMySchema.put("testInt_id", 4);
        subEntityFieldMySchema.put("testLong", 3L);
        subEntityFieldMySchema.put("fieldString", "testing");

        entity.put("fieldMySchema", subEntityFieldMySchema);
        entity.put("timestamp", 5L);

        return entity;
    }

    @Test
    void testEmbeddedAvroSchemaProcessor() throws IOException {
        List<FieldValueMapping> fieldValueMappings = asList(
                new FieldValueMapping("fieldMySchema.testInt_id", "int", 0, "4"),
                new FieldValueMapping("fieldMySchema.testLong", "long", 0, "3"),
                new FieldValueMapping("fieldMySchema.fieldString", "string", 0, "testing"),
                new FieldValueMapping("timestamp", "long", 0, "5")
        );
        File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
        ParsedSchema parsedSchema = schemaExtractor.schemaTypesList(testFile, "AVRO");
        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
        avroSchemaProcessor.processSchema(parsedSchema, new SchemaMetadata(1, 1, ""), fieldValueMappings);
        EnrichedRecord message = avroSchemaProcessor.next();
        GenericRecord entity = setUpEntityForEmbeddedAvroTest(parsedSchema);

        assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
        assertThat(message.getGenericRecord()).isNotNull();
        assertThat(message.getGenericRecord()).isEqualTo(entity);
    }

    @Test
    void textAvroSchemaProcessor() throws KLoadGenException {
        List<FieldValueMapping> fieldValueMappingList = asList(
                new FieldValueMapping("name", "string", 0, "Jose"),
                new FieldValueMapping("age", "int", 0, "43"));
        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
        avroSchemaProcessor.processSchema(SchemaBuilder.builder().record("testing").fields().requiredString("name").optionalInt("age").endRecord(),
                new SchemaMetadata(1, 1, ""), fieldValueMappingList);
        EnrichedRecord message = avroSchemaProcessor.next();
        assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
        assertThat(message.getGenericRecord()).isNotNull();
        assertThat(message.getGenericRecord()).hasFieldOrPropertyWithValue("values", asList("Jose", 43).toArray());
  }

  @Test
  void textAvroSchemaProcessorLogicalType() throws KLoadGenException {
    Schema decimalSchemaBytes = SchemaBuilder.builder().bytesType();
    LogicalTypes.decimal(5,2).addToSchema(decimalSchemaBytes);

    List<FieldValueMapping> fieldValueMappingList = asList(
            new FieldValueMapping("name", "string", 0, "Jose"),
            new FieldValueMapping("decimal", "bytes_decimal", 0, "44.444"));

    AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaBuilder.builder().record("testing").fields().requiredString("name").name(
            "decimal").type(decimalSchemaBytes).noDefault().endRecord(),
            new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(message.getGenericRecord()).hasFieldOrPropertyWithValue("values",
            asList("Jose", new BigDecimal("44.444")).toArray());
    }

    @Test
    void textAvroSchemaProcessorArrayMap() throws KLoadGenException {
        List<FieldValueMapping> fieldValueMappingList = asList(
                new FieldValueMapping("values[2][2:]", "string-map-array", 2, "n:1, t:2"),
                new FieldValueMapping("topLevelRecord.subvalues[2][2:]", "string-map-array", 2, "n:1, t:2"));

        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
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
                        .name("topLevelRecord")
                        .type()
                        .record("subvalues")
                        .fields()
                        .name("subvalues")
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
                        .endRecord()
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
        List<Map<String, Object>> valuesElement = (List<Map<String, Object>>) ((GenericRecord) message.getGenericRecord()).get("values");
        assertThat(valuesElement).hasSize(2).containsExactlyInAnyOrder(Maps.of("n", "1", "t", "2"), Maps.of("n", "1", "t", "2"));
        List<Map<String, Object>> subvaluesElement = ((List<Map<String, Object>>) ((GenericRecord) message.getGenericRecord()).get("topLevelRecord"));
        assertThat(subvaluesElement).hasSize(2).containsExactlyInAnyOrder(Maps.of("n", "1", "t", "2"), Maps.of("n", "1", "t", "2"));
    }

    @Test
    void textAvroSchemaProcessorArrayRecord() throws KLoadGenException {
        List<FieldValueMapping> fieldValueMappingList = asList(
                new FieldValueMapping("values[2].name", "string", 2, "Jose, Andres"),
                new FieldValueMapping("values[].amount", "float", 2, "0.5, 0.6"));

        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
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
        List<FieldValueMapping> fieldValueMappingList = singletonList(
                new FieldValueMapping("values[2:]", "string-map", 2, "n:1, t:2"));

        AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
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
        Map<String, String> result = (Map<String, String>) ((GenericRecord) message.getGenericRecord()).get("values");
        assertThat(result).hasSize(2).containsEntry("n", "1").containsEntry("t", "2");
    }

}