package net.coru.kloadgen.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import net.coru.kloadgen.extractor.extractors.AvroExtractor;
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
            new FieldValueMapping("fieldMySchema.testInt_id", "int", 0, ""),
            new FieldValueMapping("fieldMySchema.testLong", "long", 0, ""),
            new FieldValueMapping("fieldMySchema.fieldString", "string", 0, ""),
            new FieldValueMapping("timestamp", "long", 0, "", true, true)
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
            new FieldValueMapping("mapOfString[:]", "string-map", 0, "", false, false),
            new FieldValueMapping("arrayOfString[]", "string-array", 0, "", false, false),
            new FieldValueMapping("arrayOfMap[][:]", "string-map-array", 0, "", false, false),
            new FieldValueMapping("mapOfArray[:][]", "int-array-map", 0, "", false, false),
            new FieldValueMapping("mapOfArrayOfRecord[:][].name", "string", 0, "", false, false),
            new FieldValueMapping("mapOfArrayOfRecord[:][].age", "int", 0, "", true, false),
            new FieldValueMapping("arrayOfMapOfRecord[][:].name", "string", 0, "", false, false),
            new FieldValueMapping("arrayOfMapOfRecord[][:].age", "int", 0, "", true, false)
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
            new FieldValueMapping("theMap[:][].otherType.addTypeId", "string", 0, "", true, true),
            new FieldValueMapping("theMap[:][].otherType.name", "string", 0, "", true, true),
            new FieldValueMapping("theMap[:][].otherType.otherField", "string", 0, "", false, true),
            new FieldValueMapping("theMap[:][].addAmount", "bytes_decimal", 0, "", true, true),
            new FieldValueMapping("theMap[:][].addCode", "string", 0, "", false, true),
            new FieldValueMapping("theMap[:][].metadataMap[:]", "string-map", 0, "", false, true),
            new FieldValueMapping("theMap[:][].metadataArray[]", "string-array", 0, "", false, true),
            new FieldValueMapping("theMap[:][].metadataMapMap[:][:]", "string-map-map", 0, "", true, true),
            new FieldValueMapping("theMap[:][].metadataArrayArray[][]", "string-array-array", 0, "", true, true)
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
            new FieldValueMapping("Date", "int_date"),
            new FieldValueMapping("TimeMillis", "int_time-millis"),
            new FieldValueMapping("TimeMicros", "long_time-micros"),
            new FieldValueMapping("TimestampMillis", "long_timestamp-millis"),
            new FieldValueMapping("TimestampMicros", "long_timestamp-micros"),
            new FieldValueMapping("LocalTimestampMillis", "long_local-timestamp-millis"),
            new FieldValueMapping("LocalTimestampMicros", "long_local-timestamp-micros"),
            new FieldValueMapping("UUID", "string_uuid"),
            new FieldValueMapping("Decimal", "bytes_decimal"),
            new FieldValueMapping("DecimalFixed", "fixed_decimal")
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
            new FieldValueMapping("mainObject.arrayValue[].optional1", "string", 0, "", false, true),
            new FieldValueMapping("mainObject.arrayValue[].optional2", "string", 0, "", false, true),
            new FieldValueMapping("mainObject.arrayValue[].optional3", "string", 0, "", false, true)
        );
  }

}
