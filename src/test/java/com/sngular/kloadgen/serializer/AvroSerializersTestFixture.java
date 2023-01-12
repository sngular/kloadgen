package com.sngular.kloadgen.serializer;

import static com.sngular.kloadgen.serializer.SerializerTestFixture.createFieldValueMapping;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.math3.util.Pair;
import org.apache.kafka.common.serialization.Serializer;

public final class AvroSerializersTestFixture {

  static final List<Serializer<GenericRecord>> serializerList = List.of(
      new GenericAvroRecordSerializer<>(),
      new GenericAvroRecordBinarySerializer<>()
  );

  static final Pair<File, List<FieldValueMapping>> TEST_SUBENTITY_ARRAY = new Pair<>(
      new FileHelper().getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc"),
      Arrays.asList(
          createFieldValueMapping("subEntity.anotherLevel.subEntityIntArray[2]", "int-array"),
          createFieldValueMapping("subEntity.anotherLevel.subEntityRecordArray[2].name", "string"),
          createFieldValueMapping("topLevelIntArray[3]", "int-array"),
          createFieldValueMapping("topLevelRecordArray[3].name", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_EMBEDED_AVROS_EXAMPLE = new Pair<>(
      new FileHelper().getFile("/avro-files/embedded-avros-example-test.avsc"),
      Arrays.asList(
          createFieldValueMapping("fieldMySchema.testInt_id", "int", "[100]"),
          createFieldValueMapping("fieldMySchema.testLong", "long"),
          createFieldValueMapping("fieldMySchema.fieldString", "string"),
          createFieldValueMapping("timestamp", "long"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ISSUE = new Pair<>(
      new FileHelper().getFile("/avro-files/issue.avsc"),
      Arrays.asList(
          createFieldValueMapping("mainObject.arrayValue[].optional1", "string"),
          createFieldValueMapping("mainObject.arrayValue[].optional2", "string"),
          createFieldValueMapping("mainObject.arrayValue[].optional3", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_OPTIONAL_ENUM = new Pair<>(
      new FileHelper().getFile("/avro-files/optionalEnum.avsc"),
      Arrays.asList(createFieldValueMapping("aggregateAttribute.fruitList.fruits[].fruitType", "enum"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_FILE_ISSUE = new Pair<>(
      new FileHelper().getFile("/avro-files/testFileIssue.avsc"),
      Arrays.asList(
          createFieldValueMapping("timestamp", "long_timestamp-millis"),
          createFieldValueMapping("queueId", "string"),
          createFieldValueMapping("name", "string"),
          createFieldValueMapping("description", "string"),
          createFieldValueMapping("accountDefault", "boolean"),
          createFieldValueMapping("accountId", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_LOGICAL_TYPES = new Pair<>(
      new FileHelper().getFile("/avro-files/testLogicalTypes.avsc"),
      Arrays.asList(
          createFieldValueMapping("Date", "int_date"),
          createFieldValueMapping("TimeMillis", "int_time-millis"),
          createFieldValueMapping("TimeMicros", "long_time-micros"),
          createFieldValueMapping("TimestampMillis", "long_timestamp-millis"),
          createFieldValueMapping("TimestampMicros", "long_timestamp-micros"),
          createFieldValueMapping("LocalTimestampMillis", "long_local-timestamp-millis"),
          createFieldValueMapping("LocalTimestampMicros", "long_local-timestamp-micros"),
          createFieldValueMapping("UUID", "string_uuid"),
          createFieldValueMapping("Decimal", "bytes_decimal"),
          createFieldValueMapping("DecimalFixed", "fixed_decimal"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MAP = new Pair<>(
      new FileHelper().getFile("/avro-files/testMap.avsc"),
      Arrays.asList(
          createFieldValueMapping("theMap[:][].otherType.addTypeId", "string"),
          createFieldValueMapping("theMap[:][].otherType.name", "string"),
          createFieldValueMapping("theMap[:][].otherType.otherField", "string"),
          createFieldValueMapping("theMap[:][].addAmount", "bytes_decimal"),
          createFieldValueMapping("theMap[:][].addCode", "string"),
          createFieldValueMapping("theMap[:][].metadataMap[:]", "string-map"),
          createFieldValueMapping("theMap[:][].metadataArray[]", "string-array"),
          createFieldValueMapping("theMap[:][].metadataMapMap[:][:]", "string-map-map"),
          createFieldValueMapping("theMap[:][].metadataArrayArray[][]", "string-array-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_NULL_ON_OPTIONAL_FIELDS = new Pair<>(
      new FileHelper().getFile("/avro-files/testNullOnOptionalFields.avsc"),
      Arrays.asList(
          createFieldValueMapping("name", "string"),
          createFieldValueMapping("favorite_number", "int"),
          createFieldValueMapping("favorite_color", "string"),
          createFieldValueMapping("emails[]", "string-array"),
          createFieldValueMapping("phones[]", "int-array"),
          createFieldValueMapping("friends[]", "string-array"),
          createFieldValueMapping("favorite_cars[].brand", "string"),
          createFieldValueMapping("favorite_cars[].power", "int"),
          createFieldValueMapping("favorite_cars[].parts[]", "string-array"),
          createFieldValueMapping("favorite_cars2[].brand2", "string"),
          createFieldValueMapping("favorite_cars2[].power2", "int"),
          createFieldValueMapping("favorite_cars2[].parts[]2", "string-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_OPTIONAL_MAP = new Pair<>(
      new FileHelper().getFile("/avro-files/testOptionalMap.avsc"),
      Arrays.asList(
          createFieldValueMapping("mapOfString[:]", "string-map"),
          createFieldValueMapping("arrayOfString[]", "string-array"),
          createFieldValueMapping("arrayOfMap[][:]", "string-map-array"),
          createFieldValueMapping("mapOfArray[:][]", "int-array-map"),
          createFieldValueMapping("mapOfArrayOfRecord[:][].name", "string"),
          createFieldValueMapping("mapOfArrayOfRecord[:][].age", "int"),
          createFieldValueMapping("arrayOfMapOfRecord[][:].name", "string"),
          createFieldValueMapping("arrayOfMapOfRecord[][:].age", "int"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_UNION_RECORD = new Pair<>(
      new FileHelper().getFile("/avro-files/testUnionRecord.avsc"),
      Arrays.asList(
          createFieldValueMapping("validateInnerObject.attribute1", "string"),
          createFieldValueMapping("validateInnerObject.attribute2", "string"),
          createFieldValueMapping("products[].Price.price", "string"),
          createFieldValueMapping("products[].Price.priceType", "string"),
          createFieldValueMapping("products[].Price.currency", "string"),
          createFieldValueMapping("products[].Price.discount", "string"),
          createFieldValueMapping("products[].Price.validateInnerObject.attribute1", "string"),
          createFieldValueMapping("products[].Price.validateInnerObject.attribute2", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_USER = new Pair<>(
      new FileHelper().getFile("/avro-files/userTest.avsc"),
      Arrays.asList(
          createFieldValueMapping("id", "int"),
          createFieldValueMapping("twitterAccounts[].status", "enum"),
          createFieldValueMapping("twitterAccounts[].status2", "enum"),
          createFieldValueMapping("toDoItems[].status3", "enum"),
          createFieldValueMapping("toDoItems[].status4", "enum"))
  );
}
