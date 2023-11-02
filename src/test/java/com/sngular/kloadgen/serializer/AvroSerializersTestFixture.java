package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.math3.util.Pair;
import org.apache.kafka.common.serialization.Serializer;

public final class AvroSerializersTestFixture {

  static final List<Serializer<GenericRecord>> SERIALIZER_LIST = List.of(
    new GenericAvroRecordSerializer<>(),
    new GenericAvroRecordBinarySerializer<>()
  );

  static final Pair<File, List<FieldValueMapping>> TEST_SUBENTITY_ARRAY = new Pair<>(
      new FileHelper().getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("subEntity.anotherLevel.subEntityIntArray[2]", "int-array"),
          SerializerTestFixture.createFieldValueMapping("subEntity.anotherLevel.subEntityRecordArray[2].name", "string"),
          SerializerTestFixture.createFieldValueMapping("topLevelIntArray[3]", "int-array"),
          SerializerTestFixture.createFieldValueMapping("topLevelRecordArray[3].name", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_EMBEDED_AVROS_EXAMPLE = new Pair<>(
      new FileHelper().getFile("/avro-files/embedded-avros-example-test.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("fieldMySchema.testInt_id", "int", "[100]"),
          SerializerTestFixture.createFieldValueMapping("fieldMySchema.testLong", "long"),
          SerializerTestFixture.createFieldValueMapping("fieldMySchema.fieldString", "string"),
          SerializerTestFixture.createFieldValueMapping("timestamp", "long"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ISSUE = new Pair<>(
      new FileHelper().getFile("/avro-files/issue.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("mainObject.arrayValue[].optional1", "string"),
          SerializerTestFixture.createFieldValueMapping("mainObject.arrayValue[].optional2", "string"),
          SerializerTestFixture.createFieldValueMapping("mainObject.arrayValue[].optional3", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_OPTIONAL_ENUM = new Pair<>(
      new FileHelper().getFile("/avro-files/optionalEnum.avsc"),
      Arrays.asList(SerializerTestFixture.createFieldValueMapping("aggregateAttribute.fruitList.fruits[].fruitType", "enum"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_FILE_ISSUE = new Pair<>(
      new FileHelper().getFile("/avro-files/testFileIssue.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("timestamp", "long_timestamp-millis"),
          SerializerTestFixture.createFieldValueMapping("queueId", "string"),
          SerializerTestFixture.createFieldValueMapping("name", "string"),
          SerializerTestFixture.createFieldValueMapping("description", "string"),
          SerializerTestFixture.createFieldValueMapping("accountDefault", "boolean"),
          SerializerTestFixture.createFieldValueMapping("accountId", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_LOGICAL_TYPES = new Pair<>(
      new FileHelper().getFile("/avro-files/testLogicalTypes.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("Date", "int_date"),
          SerializerTestFixture.createFieldValueMapping("TimeMillis", "int_time-millis"),
          SerializerTestFixture.createFieldValueMapping("TimeMicros", "long_time-micros"),
          SerializerTestFixture.createFieldValueMapping("TimestampMillis", "long_timestamp-millis"),
          SerializerTestFixture.createFieldValueMapping("TimestampMicros", "long_timestamp-micros"),
          SerializerTestFixture.createFieldValueMapping("LocalTimestampMillis", "long_local-timestamp-millis"),
          SerializerTestFixture.createFieldValueMapping("LocalTimestampMicros", "long_local-timestamp-micros"),
          SerializerTestFixture.createFieldValueMapping("UUID", "string_uuid"),
          SerializerTestFixture.createFieldValueMapping("Decimal", "bytes_decimal"),
          SerializerTestFixture.createFieldValueMapping("DecimalFixed", "fixed_decimal"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MAP = new Pair<>(
      new FileHelper().getFile("/avro-files/testMap.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("theMap[:][].otherType.addTypeId", "string"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].otherType.name", "string"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].otherType.otherField", "string"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].addAmount", "bytes_decimal"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].addCode", "string"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].metadataMap[:]", "string-map"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].metadataArray[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].metadataMapMap[:][:]", "string-map-map"),
          SerializerTestFixture.createFieldValueMapping("theMap[:][].metadataArrayArray[][]", "string-array-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_NULL_ON_OPTIONAL_FIELDS = new Pair<>(
      new FileHelper().getFile("/avro-files/testNullOnOptionalFields.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("name", "string"),
          SerializerTestFixture.createFieldValueMapping("favorite_number", "int"),
          SerializerTestFixture.createFieldValueMapping("favorite_color", "string"),
          SerializerTestFixture.createFieldValueMapping("emails[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("phones[]", "int-array"),
          SerializerTestFixture.createFieldValueMapping("friends[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("favorite_cars[].brand", "string"),
          SerializerTestFixture.createFieldValueMapping("favorite_cars[].power", "int"),
          SerializerTestFixture.createFieldValueMapping("favorite_cars[].parts[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("favorite_cars2[].brand2", "string"),
          SerializerTestFixture.createFieldValueMapping("favorite_cars2[].power2", "int"),
          SerializerTestFixture.createFieldValueMapping("favorite_cars2[].parts[]2", "string-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_OPTIONAL_MAP = new Pair<>(
      new FileHelper().getFile("/avro-files/testOptionalMap.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("mapOfString[:]", "string-map"),
          SerializerTestFixture.createFieldValueMapping("arrayOfString[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("arrayOfMap[][:]", "string-map-array"),
          SerializerTestFixture.createFieldValueMapping("mapOfArray[:][]", "int-array-map"),
          SerializerTestFixture.createFieldValueMapping("mapOfArrayOfRecord[:][].name", "string"),
          SerializerTestFixture.createFieldValueMapping("mapOfArrayOfRecord[:][].age", "int"),
          SerializerTestFixture.createFieldValueMapping("arrayOfMapOfRecord[][:].name", "string"),
          SerializerTestFixture.createFieldValueMapping("arrayOfMapOfRecord[][:].age", "int"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_UNION_RECORD = new Pair<>(
      new FileHelper().getFile("/avro-files/testUnionRecord.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("validateInnerObject.attribute1", "string"),
          SerializerTestFixture.createFieldValueMapping("validateInnerObject.attribute2", "string"),
          SerializerTestFixture.createFieldValueMapping("products[].Price.price", "string"),
          SerializerTestFixture.createFieldValueMapping("products[].Price.priceType", "string"),
          SerializerTestFixture.createFieldValueMapping("products[].Price.currency", "string"),
          SerializerTestFixture.createFieldValueMapping("products[].Price.discount", "string"),
          SerializerTestFixture.createFieldValueMapping("products[].Price.validateInnerObject.attribute1", "string"),
          SerializerTestFixture.createFieldValueMapping("products[].Price.validateInnerObject.attribute2", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_USER = new Pair<>(
      new FileHelper().getFile("/avro-files/userTest.avsc"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("id", "int"),
          SerializerTestFixture.createFieldValueMapping("twitterAccounts[].status", "enum"),
          SerializerTestFixture.createFieldValueMapping("twitterAccounts[].status2", "enum"),
          SerializerTestFixture.createFieldValueMapping("toDoItems[].status3", "enum"),
          SerializerTestFixture.createFieldValueMapping("toDoItems[].status4", "enum"))
  );

  private AvroSerializersTestFixture() {
  }
}
