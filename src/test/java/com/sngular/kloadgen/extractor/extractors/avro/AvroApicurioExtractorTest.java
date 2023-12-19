package com.sngular.kloadgen.extractor.extractors.avro;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.avro.Schema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvroApicurioExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final Extractor<Schema> avroApicurioExtractor = new AvroApicurioExtractor();

  @Test
  @DisplayName("Should extract Embedded Record")
  void testFlatPropertiesEmbeddedAvros() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/embedded-avros-example-test.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(4)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("fieldMySchema.testInt_id").fieldType("int").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("fieldMySchema.testLong").fieldType("long").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("fieldMySchema.fieldString").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("timestamp").fieldType("long").fieldValueList("").required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Should extract Optional Enum")
  void testOptionalEnum() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/optionalEnum.avsc");

    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(1)
              .containsExactlyInAnyOrder(
                  FieldValueMapping
                      .builder()
                      .fieldName("aggregateAttribute.fruitList.fruits[].fruitType")
                      .fieldType("enum")
                      .fieldValueList("")
                      .valueLength(0)
                      .required(true)
                      .isAncestorRequired(true)
                      .build()
              );
  }

  @Test
  @DisplayName("Should extract Optional Map with Array/Record")
  void testFlatPropertiesOptionalMapArray() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/testOptionalMap.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(8)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("mapOfString[:]").fieldType("string-map").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false)
                                   .build(),
                  FieldValueMapping.builder().fieldName("arrayOfString[]").fieldType("string-array").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false)
                                   .build(),
                  FieldValueMapping.builder().fieldName("arrayOfMap[][:]").fieldType("string-map-array").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false)
                                   .build(),
                  FieldValueMapping.builder().fieldName("mapOfArray[:][]").fieldType("int-array-map").fieldValueList("").valueLength(0).required(false).isAncestorRequired(false)
                                   .build(),
                  FieldValueMapping.builder().fieldName("mapOfArrayOfRecord[:][].name").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("mapOfArrayOfRecord[:][].age").fieldType("int").fieldValueList("").valueLength(0).required(true).isAncestorRequired(false)
                                   .build(),
                  FieldValueMapping.builder().fieldName("arrayOfMapOfRecord[][:].name").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("arrayOfMapOfRecord[][:].age").fieldType("int").fieldValueList("").valueLength(0).required(true).isAncestorRequired(false)
                                   .build()
              );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesMap() throws Exception {
    final String testFile = fileHelper.getContent("/avro-files/testMap.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(9)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("theMap[:][].otherType.addTypeId").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].otherType.name").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].otherType.otherField").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].addAmount").fieldType("bytes_decimal").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].addCode").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].metadataMap[:]").fieldType("string-map").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].metadataArray[]").fieldType("string-array").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].metadataMapMap[:][:]").fieldType("string-map-map").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("theMap[:][].metadataArrayArray[][]").fieldType("string-array-array").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Should extract Logical times")
  void testFlatPropertiesLogicalTypes() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/testLogicalTypes.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(10)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("Date").fieldType("int_date").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("TimeMillis").fieldType("int_time-millis").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("TimeMicros").fieldType("long_time-micros").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("TimestampMillis").fieldType("long_timestamp-millis").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("TimestampMicros").fieldType("long_timestamp-micros").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("LocalTimestampMillis").fieldType("long_local-timestamp-millis").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("LocalTimestampMicros").fieldType("long_local-timestamp-micros").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("UUID").fieldType("string_uuid").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("Decimal").fieldType("bytes_decimal").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("DecimalFixed").fieldType("fixed_decimal").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Should extract Optional Array")
  void testFlatPropertiesOptionalArray() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/issue.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(3)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional1").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional2").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional3").fieldType("string").fieldValueList("").valueLength(0).required(false)
                                   .isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Should extract Union Record")
  void testFlatPropertiesUnionRecordAvros() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/testUnionRecord.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(8)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("validateInnerObject.attribute1").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("validateInnerObject.attribute2").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("products[].Price.price").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("products[].Price.priceType").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("products[].Price.currency").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("products[].Price.discount").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("products[].Price.validateInnerObject.attribute1").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("products[].Price.validateInnerObject.attribute2").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Should Extract Union Record At Any Order In The Inner Array")
  void testFlatPropertiesRecordUnionReverseOrder() throws Exception {

    final String testFile = fileHelper.getContent("/avro-files/testUnionReverseOrder.avsc");
    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroApicurioExtractor.processSchema(schema);

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(5)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("validateInnerObject.attribute1").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("validateInnerObject.attribute2").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("products[].Price.validateInnerObject.attribute1").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("products[].Price.validateInnerObject.attribute2").fieldType("string").fieldValueList("").valueLength(0).required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("products[].Price.price").fieldType("string").fieldValueList("").valueLength(0).required(true).isAncestorRequired(true)
                                   .build());
  }

}
