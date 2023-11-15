/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.processor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.fixture.AvroSchemaFixturesConstants;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.testutil.FileHelper;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AvroSchemaProcessorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final BaseSchemaMetadata confluentBaseSchemaMetadata
      = new BaseSchemaMetadata<>(ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1,
                                                                                                                           "")));

  private static Stream<Object> parametersForTestNullOnOptionalField() {
    return Stream.of(
        Arguments.of(AvroSchemaFixturesConstants.BASIC_STRING_ARRAY_MAP_NULL, AvroSchemaFixturesConstants.BASIC_STRING_ARRAY_MAP_NULL_FIELDS),
        Arguments.of(AvroSchemaFixturesConstants.ARRAY_OPTIONAL_NULL, AvroSchemaFixturesConstants.ARRAY_OPTIONAL_NULL_FIELDS),
        Arguments.of(AvroSchemaFixturesConstants.MAP_OPTIONAL_NULL, AvroSchemaFixturesConstants.MAP_OPTIONAL_NULL_FIELDS)
    );
  }

  @BeforeEach
  public void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should process an Avro Schema Processor With Sub Entity Simple Array")
  void testAvroSchemaProcessorWithSubEntitySimpleArray() throws IOException {
    final var fieldValueMappings = Arrays.asList(
        FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityIntArray[2]").fieldType("int-array").valueLength(0).fieldValueList("[1]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("topLevelIntArray[3]").fieldType("int-array").valueLength(0).fieldValueList("[2]").required(true).isAncestorRequired(true).build());

    final File testFile = fileHelper.getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "AVRO");
    final SchemaProcessor avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final GenericRecord entity = setUpEntityForAvroTestWithSubEntitySimpleArray(parsedSchema);
    final EnrichedRecord message = (EnrichedRecord) avroSchemaProcessor.next();

    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(message.getGenericRecord()).isEqualTo(entity);
  }

  private GenericRecord setUpEntityForAvroTestWithSubEntitySimpleArray(final ParsedSchema parsedSchema) {
    final var entity = new GenericData.Record((Schema) parsedSchema.rawSchema());
    final var subEntitySchema = entity.getSchema().getField("subEntity").schema();
    final var subEntityRecord = new GenericData.Record(subEntitySchema);
    final var anotherLevelSchema = subEntitySchema.getField("anotherLevel").schema();
    final var anotherLevelRecord = new GenericData.Record(anotherLevelSchema);
    anotherLevelRecord.put("subEntityIntArray", Arrays.asList(1, 1));
    subEntityRecord.put("anotherLevel", anotherLevelRecord);

    entity.put("subEntity", subEntityRecord);
    entity.put("topLevelIntArray", Arrays.asList(2, 2, 2));

    return entity;
  }

  @Test
  @DisplayName("Should process Avro Schema Processor With SubEntity Array")
  void testAvroSchemaProcessorWithSubEntityArray() throws IOException {
    final var fieldValueMappings = Arrays.asList(
        FieldValueMapping
            .builder()
            .fieldName("subEntity.anotherLevel.subEntityRecordArray[2].name")
            .fieldType("string")
            .valueLength(0)
            .fieldValueList("second")
            .required(true)
            .isAncestorRequired(true)
            .build(),
        FieldValueMapping
            .builder()
            .fieldName("topLevelRecordArray[1].name")
            .fieldType("string")
            .valueLength(0)
            .fieldValueList("third")
            .required(true)
            .isAncestorRequired(true)
            .build());

    final var testFile = fileHelper.getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
    final var parsedSchema = new ParsedSchema(testFile, "AVRO");
    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final var entity = setUpEntityForAvroTestWithSubEntityArray(parsedSchema);
    final var message = (EnrichedRecord) avroSchemaProcessor.next();

    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(message.getGenericRecord()).isEqualTo(entity);
  }

  private GenericRecord setUpEntityForAvroTestWithSubEntityArray(final ParsedSchema parsedSchema) {
    final var entity = new GenericData.Record((Schema) parsedSchema.rawSchema());
    final var subEntitySchema = entity.getSchema().getField("subEntity").schema();
    final var subEntityRecord = new GenericData.Record(subEntitySchema);
    final var anotherLevelSchema = subEntitySchema.getField("anotherLevel").schema();
    final var subEntityRecordArraySchema = anotherLevelSchema.getField("subEntityRecordArray").schema().getElementType();
    final var anotherLevelRecord = new GenericData.Record(anotherLevelSchema);
    final var subEntityItemsRecord = new GenericData.Record(subEntityRecordArraySchema);
    subEntityItemsRecord.put("name", "second");
    anotherLevelRecord.put("subEntityRecordArray", Arrays.asList(subEntityItemsRecord, subEntityItemsRecord));

    subEntityRecord.put("anotherLevel", anotherLevelRecord);
    entity.put("subEntity", subEntityRecord);

    final var topLevelRecordArray =
        new GenericData.Record(entity.getSchema().getField("topLevelRecordArray").schema().getElementType());
    topLevelRecordArray.put("name", "third");
    entity.put("topLevelRecordArray", Collections.singletonList(topLevelRecordArray));

    return entity;
  }

  @Test
  @DisplayName("Should process Embedded Avro Schema Processor")
  void testEmbeddedAvroSchemaProcessor() throws IOException {
    final var fieldValueMappings = Arrays.asList(
        FieldValueMapping.builder().fieldName("fieldMySchema.testInt_id").fieldType("int").valueLength(0).fieldValueList("4").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("fieldMySchema.testLong").fieldType("long").valueLength(0).fieldValueList("3").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("fieldMySchema.fieldString").fieldType("string").valueLength(0).fieldValueList("testing").required(true).isAncestorRequired(true)
                         .build(),
        FieldValueMapping.builder().fieldName("timestamp").fieldType("long").valueLength(0).fieldValueList("5").required(true).isAncestorRequired(true).build()
    );
    final var testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    final var parsedSchema = new ParsedSchema(testFile, "AVRO");
    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final var message = (EnrichedRecord) avroSchemaProcessor.next();
    final var entity = setUpEntityForEmbeddedAvroTest(parsedSchema);

    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(message.getGenericRecord()).isEqualTo(entity);
  }

  private GenericRecord setUpEntityForEmbeddedAvroTest(final ParsedSchema parsedSchema) {
    GenericData.Record entity = null; //TODO HERE

    if (parsedSchema.rawSchema() instanceof Schema) {
      final Schema schema = (Schema) parsedSchema.rawSchema();
      if (Schema.Type.UNION.equals(schema.getType())) {
        entity = new GenericData.Record(schema.getTypes().get(schema.getTypes().size() - 1));
      } else {
        entity = new GenericData.Record(schema);
      }
    }
    final var subEntityFieldMySchema = new GenericData.Record(entity.getSchema().getField("fieldMySchema").schema());

    subEntityFieldMySchema.put("testInt_id", 4);
    subEntityFieldMySchema.put("testLong", 3L);
    subEntityFieldMySchema.put("fieldString", "testing");

    entity.put("fieldMySchema", subEntityFieldMySchema);
    entity.put("timestamp", 5L);

    return entity;
  }

  @Test
  void textAvroSchemaProcessor() throws KLoadGenException {
    final var fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("name").fieldType("string").valueLength(0).fieldValueList("Jose").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("age").fieldType("int").valueLength(0).fieldValueList("43").required(true).isAncestorRequired(true).build());
    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO,
                                      SchemaBuilder.builder().record("testing").fields().requiredString("name").optionalInt("age").endRecord(),
                                      confluentBaseSchemaMetadata, fieldValueMappingList);
    final var message = (EnrichedRecord) avroSchemaProcessor.next();
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(message.getGenericRecord()).hasFieldOrPropertyWithValue("values", Arrays.asList("Jose", 43).toArray());
  }

  @Test
  @DisplayName("Should process Avro Schema Processor Logical Type")
  void textAvroSchemaProcessorLogicalType() throws KLoadGenException {
    final var decimalSchemaBytes = SchemaBuilder.builder().bytesType();
    LogicalTypes.decimal(5, 2).addToSchema(decimalSchemaBytes);

    final var fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("name").fieldType("string").valueLength(0).fieldValueList("Jose").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("decimal").fieldType("bytes_decimal").valueLength(0).fieldValueList("44.444").required(true).isAncestorRequired(true).build());

    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, SchemaBuilder.builder().record("testing").fields().requiredString("name").name(
                                          "decimal").type(decimalSchemaBytes).noDefault().endRecord(),
                                      confluentBaseSchemaMetadata, fieldValueMappingList);
    final var message = (EnrichedRecord) avroSchemaProcessor.next();
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(message.getGenericRecord()).hasFieldOrPropertyWithValue("values",
                                                                                  Arrays.asList("Jose", new BigDecimal("44.444")).toArray());
  }

  @Test
  @DisplayName("Should process Avro Schema Processor Array Map")
  void textAvroSchemaProcessorArrayMap() throws KLoadGenException {
    final var fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("values[2][2:]").fieldType("string-map-array").valueLength(2).fieldValueList("n:1, t:2").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("topLevelRecord.subvalues[2][2:]").fieldType("string-map-array").valueLength(2).fieldValueList("n:1, t:2").required(true)
                         .isAncestorRequired(true).build());

    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, SchemaBuilder
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
                                      confluentBaseSchemaMetadata,
                                      fieldValueMappingList);

    final var message = (EnrichedRecord) avroSchemaProcessor.next();
    Assertions.assertThat(message)
              .isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull()
              .hasFieldOrProperty("values")
              .extracting("values")
              .extracting(Arrays::asList)
              .asList()
              .hasSize(1);
    final List<Map<String, Object>> valuesElement = (List<Map<String, Object>>) ((GenericRecord) message.getGenericRecord()).get("values");
    Assertions.assertThat(valuesElement).hasSize(2).containsExactlyInAnyOrder(Maps.of("n", "1", "t", "2"), Maps.of("n", "1", "t", "2"));
    final List<Map<String, Object>> subvaluesElement = (List<Map<String, Object>>) ((GenericRecord) ((GenericRecord) message.getGenericRecord()).get("topLevelRecord")).get(
        "subvalues");
    Assertions.assertThat(subvaluesElement).hasSize(2).containsExactlyInAnyOrder(Maps.of("n", "1", "t", "2"), Maps.of("n", "1", "t", "2"));
  }

  @Test
  @DisplayName("Should process Avro Schema Processor Array Record")
  void textAvroSchemaProcessorArrayRecord() throws KLoadGenException {
    final var fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("values[2].name").fieldType("string").valueLength(2).fieldValueList("Jose, Andres").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("values[].amount").fieldType("float").valueLength(2).fieldValueList("0.5, 0.6").required(true).isAncestorRequired(true).build());

    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, SchemaBuilder
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
                                      confluentBaseSchemaMetadata,
                                      fieldValueMappingList);

    final var message = (EnrichedRecord) avroSchemaProcessor.next();
    Assertions.assertThat(message)
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
  @DisplayName("Should Process Avro Schema Processor Map")
  void textAvroSchemaProcessorMap() throws KLoadGenException {
    final var fieldValueMappingList = Collections.singletonList(
        FieldValueMapping.builder().fieldName("values[2:]").fieldType("string-map").valueLength(2).fieldValueList("n:1, t:2").required(true).isAncestorRequired(true).build());

    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, SchemaBuilder
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
                                      confluentBaseSchemaMetadata,
                                      fieldValueMappingList);

    final var message = (EnrichedRecord) avroSchemaProcessor.next();
    Assertions.assertThat(message)
              .isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull()
              .hasFieldOrProperty("values")
              .extracting("values")
              .extracting(Arrays::asList)
              .asList()
              .hasSize(1);
    final var result = (Map<String, String>) ((GenericRecord) message.getGenericRecord()).get("values");
    Assertions.assertThat(result).hasSize(2).containsEntry("n", "1").containsEntry("t", "2");
  }

  @ParameterizedTest
  @MethodSource("parametersForTestNullOnOptionalField")
  void testNullOnOptionalField(final Schema schema, final List<FieldValueMapping> fieldValueMapping) {
    final var metadata = confluentBaseSchemaMetadata;

    final var avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, schema, metadata, fieldValueMapping);
    final var message = (EnrichedRecord) avroSchemaProcessor.next();

    Assertions.assertThat(message)
              .isNotNull()
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();

    final var genericRecord = (GenericRecord) message.getGenericRecord();
    Assertions.assertThat(genericRecord.get(0)).isNotNull();
    Assertions.assertThat(genericRecord.get(1)).isNull();

  }

  @Test
  @DisplayName("Should process Custom Iterator Of Values With Same Starting Value")
  void testCustomSequenceOfValuesWithSameStartingValue() {
    final var fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("values[3].id").fieldType("it").valueLength(0).fieldValueList("[1,2]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("values[3].otherId").fieldType("it").valueLength(0).fieldValueList("[1,3]").required(true).isAncestorRequired(true).build());

    final var avroSchemaProcessor = new SchemaProcessor();
    final var schemaWithTwoSequencesWithSameStartingValue = SchemaBuilder
        .builder()
        .record("Root")
        .fields()
        .name("values")
        .type()
        .array()
        .items()
        .type(SchemaBuilder.builder()
                           .record("valuesData")
                           .fields()
                           .name("id")
                           .type(Schema.Type.STRING.getName())
                           .noDefault()
                           .name("otherId")
                           .type(Schema.Type.LONG.getName())
                           .noDefault()
                           .endRecord())
        .noDefault()
        .endRecord();
    final var entity = entityForCustomSequenceOfValuesWithSameStartingStartingValue(schemaWithTwoSequencesWithSameStartingValue, Arrays.asList("1", "2", "1"),
                                                                                    Arrays.asList("1", "3", "1"));
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, schemaWithTwoSequencesWithSameStartingValue,
                                      confluentBaseSchemaMetadata,
                                      fieldValueMappingList);

    final var message = (EnrichedRecord) avroSchemaProcessor.next();

    Assertions.assertThat(message.getGenericRecord()).isEqualTo(entity);
  }

  private GenericRecord entityForCustomSequenceOfValuesWithSameStartingStartingValue(final Schema schema, final List<String> idValues, final List<String> otherIdValues) {
    final var entity = new GenericData.Record(schema);
    final var valuesSchema = entity.getSchema().getField("values").schema();
    final var valuesDataSchema = valuesSchema.getElementType();
    final var valuesData = IntStream
        .range(0, Math.min(idValues.size(), otherIdValues.size()))
        .mapToObj(idx -> {
          final var valuesDataRecord = new GenericData.Record(valuesDataSchema);
          valuesDataRecord.put("id", idValues.get(idx));
          valuesDataRecord.put("otherId", Long.valueOf(otherIdValues.get(idx)));
          return valuesDataRecord;
        }).collect(Collectors.toList());

    entity.put("values", valuesData);
    return entity;
  }

  @Test
  @DisplayName("Should process Custom Iterator Of Values With Same FieldName In Different Mappings")
  void testCustomIteratorOfValuesWithSameFieldNameInDifferentMappings() {
    final var fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("values[4].id").fieldType("it").valueLength(0).fieldValueList("[1,2.44,3.6]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("otherValues[4].id").fieldType("it").valueLength(0).fieldValueList("[1,3.02,4.98]").required(true).isAncestorRequired(true).build());

    final var idSchema = LogicalTypes.decimal(5, 2).addToSchema(SchemaBuilder.builder().bytesBuilder().endBytes());

    final var avroSchemaProcessor = new SchemaProcessor();
    final var schemaWithTwoIteratorsWithSameStartingValue = SchemaBuilder
        .builder()
        .record("Root")
        .fields()
        .name("values")
        .type()
        .array()
        .items()
        .type(SchemaBuilder.builder()
                           .record("valuesData")
                           .fields()
                           .name("id")
                           .type(idSchema)
                           .noDefault()
                           .endRecord())
        .noDefault()
        .name("otherValues")
        .type()
        .array()
        .items()
        .type(SchemaBuilder.builder()
                           .record("otherValuesData")
                           .fields()
                           .name("id")
                           .type(idSchema)
                           .noDefault()
                           .endRecord())
        .noDefault()
        .endRecord();
    final var entity = entityForCustomIteratorOfValuesWithSameFieldNameInDifferentMappings(schemaWithTwoIteratorsWithSameStartingValue, Arrays.asList("1", "2.44", "3.6", "1"),
                                                                                           Arrays.asList("1", "3.02", "4.98", "1"));
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, schemaWithTwoIteratorsWithSameStartingValue,
                                      confluentBaseSchemaMetadata,
                                      fieldValueMappingList);

    final var message = (EnrichedRecord) avroSchemaProcessor.next();

    Assertions.assertThat(message.getGenericRecord()).isEqualTo(entity);
  }

  private GenericRecord entityForCustomIteratorOfValuesWithSameFieldNameInDifferentMappings(final Schema schema, final List<String> idValues, final List<String> idOtherValues) {
    final var entity = new GenericData.Record(schema);
    final var valuesData = getIdRecordsList(entity, "values", idValues);
    final var otherValuesData = getIdRecordsList(entity, "otherValues", idOtherValues);

    entity.put("values", valuesData);
    entity.put("otherValues", otherValuesData);
    return entity;
  }

  private List<GenericRecord> getIdRecordsList(final GenericRecord entity, final String fieldNameContainingId, final List<String> idValues) {
    final var schemaArrayContainingId = entity.getSchema().getField(fieldNameContainingId).schema();
    final var schemaContainingId = schemaArrayContainingId.getElementType();
    return idValues.stream().map(id -> {
      final GenericRecord recordContainingId = new GenericData.Record(schemaContainingId);
      recordContainingId.put("id", new BigDecimal(id));
      return recordContainingId;
    }).collect(Collectors.toList());
  }


  @Test
  @DisplayName("Should process Embedded Avro Schema Processor")
  void testEnumProcessor() throws IOException {
    final var fieldValueMappings = Collections.singletonList(
        FieldValueMapping
            .builder()
            .fieldName("aggregateAttribute.fruitList.fruits[1].fruitType")
            .fieldType("enum")
            .fieldValueList("[MY_ENUM_1]")
            .valueLength(0)
            .required(true)
            .isAncestorRequired(true)
            .build()
    );
    final File testFile = fileHelper.getFile("/avro-files/optionalEnum.avsc");
    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "AVRO");
    final SchemaProcessor avroSchemaProcessor = new SchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parsedSchema, confluentBaseSchemaMetadata, fieldValueMappings);
    final EnrichedRecord message = (EnrichedRecord) avroSchemaProcessor.next();

    final var entity = entityForEnumMappings((Schema) parsedSchema.rawSchema());
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull().isEqualTo(entity);
  }

  private GenericRecord entityForEnumMappings(final Schema schema) {
    final var aggregate = new GenericData.Record(schema);
    final var aggregateAttribute = aggregate.getSchema().getField("aggregateAttribute").schema();
    final var aggregateAttributeRecord = new GenericData.Record(aggregateAttribute);
    final var fruitList = aggregateAttribute.getField("fruitList").schema().getTypes().get(1);
    final var fruitListRecord = new GenericData.Record(fruitList);
    final var fruitArraySc = fruitList.getField("fruits").schema().getTypes().get(1);
    final var fruitArray = new GenericData.Array<>(fruitArraySc, Collections.emptyList());
    final var fruit = fruitArraySc.getElementType();
    final var fruitRecord = new GenericData.Record(fruit);
    final var fruitEnumType = fruit.getField("fruitType").schema();
    final var fruitEnumRecord = new GenericData.EnumSymbol(fruitEnumType.getTypes().get(1), "MY_ENUM_1");
    fruitRecord.put("fruitType", fruitEnumRecord);
    fruitArray.add(fruitRecord);
    fruitListRecord.put("fruits", fruitArray);
    aggregateAttributeRecord.put("fruitList", fruitListRecord);
    aggregate.put("aggregateAttribute", aggregateAttributeRecord);
    return aggregate;
  }
}
