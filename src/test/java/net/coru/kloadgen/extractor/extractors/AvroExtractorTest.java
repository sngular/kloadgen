package net.coru.kloadgen.extractor.extractors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.avro.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvroExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final AvroExtractor avroExtractor = new AvroExtractor();

  @Test
  @DisplayName("Should extract Embedded Record")
  void testFlatPropertiesEmbeddedAvros() throws Exception {

    String testFile = fileHelper.getContent("/avro-files/embedded-avros-example-test.avsc");
    ParsedSchema parsedSchema = avroExtractor.getParsedSchema(testFile);
    Schema schema = (Schema) parsedSchema.rawSchema();

    List<FieldValueMapping> fieldValueMappingList = avroExtractor.processSchema(schema);

    assertThat(fieldValueMappingList)
        .hasSize(4)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("fieldMySchema.testInt_id").fieldType("int").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("fieldMySchema.testLong").fieldType("long").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("fieldMySchema.fieldString").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("timestamp").fieldType("long").fieldValueList("").required(true).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract Optional Map with Array/Record")
  void testFlatPropertiesOptionalMapArray() throws Exception {

    String testFile = fileHelper.getContent("/avro-files/testOptionalMap.avsc");
    ParsedSchema parsedSchema = avroExtractor.getParsedSchema(testFile);
    Schema schema = (Schema) parsedSchema.rawSchema();

    List<FieldValueMapping> fieldValueMappingList = avroExtractor.processSchema(schema);

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("mapOfString[:]").fieldType("string-map").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfString[]").fieldType("string-array").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfMap[][:]").fieldType("string-map-array").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("mapOfArray[:][]").fieldType("int-array-map").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("mapOfArrayOfRecord[:][].name").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("mapOfArrayOfRecord[:][].age").fieldType("int").fieldValueList("").valueLength(0).required(true).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfMapOfRecord[][:].name").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfMapOfRecord[][:].age").fieldType("int").fieldValueList("").valueLength(0).required(true).isAncestorRequired(false).build()
        );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesMap() throws Exception {
    String testFile = fileHelper.getContent("/avro-files/testMap.avsc");
    ParsedSchema parsedSchema = avroExtractor.getParsedSchema(testFile);
    Schema schema = (Schema) parsedSchema.rawSchema();

    List<FieldValueMapping> fieldValueMappingList = avroExtractor.processSchema(schema);

    assertThat(fieldValueMappingList)
        .hasSize(9)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("theMap[:][].otherType.addTypeId").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].otherType.name").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].otherType.otherField").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].addAmount").fieldType("bytes_decimal").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].addCode").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataMap[:]").fieldType("string-map").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataArray[]").fieldType("string-array").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataMapMap[:][:]").fieldType("string-map-map").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataArrayArray[][]").fieldType("string-array-array").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract Logical times")
  void testFlatPropertiesLogicalTypes() throws Exception {

    String testFile = fileHelper.getContent("/avro-files/testLogicalTypes.avsc");
    ParsedSchema parsedSchema = avroExtractor.getParsedSchema(testFile);
    Schema schema = (Schema) parsedSchema.rawSchema();

    List<FieldValueMapping> fieldValueMappingList = avroExtractor.processSchema(schema);

    assertThat(fieldValueMappingList)
        .hasSize(10)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("Date").fieldType("int_date").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimeMillis").fieldType("int_time-millis").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimeMicros").fieldType("long_time-micros").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimestampMillis").fieldType("long_timestamp-millis").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimestampMicros").fieldType("long_timestamp-micros").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("LocalTimestampMillis").fieldType("long_local-timestamp-millis").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("LocalTimestampMicros").fieldType("long_local-timestamp-micros").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("UUID").fieldType("string_uuid").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("Decimal").fieldType("bytes_decimal").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("DecimalFixed").fieldType("fixed_decimal").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract Optional Array")
  void testFlatPropertiesOptionalArray() throws Exception {

    String testFile = fileHelper.getContent("/avro-files/issue.avsc");
    ParsedSchema parsedSchema = avroExtractor.getParsedSchema(testFile);
    Schema schema = (Schema) parsedSchema.rawSchema();

    List<FieldValueMapping> fieldValueMappingList = avroExtractor.processSchema(schema);

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional1").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional2").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional3").fieldType("string").fieldValueList("").valueLength(0).required(false).isAncestorRequired(true).build()
        );
  }

}
