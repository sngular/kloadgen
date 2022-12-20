/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.randomtool.util.ValidTypeConstants;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AvroGeneratorToolTest {

  private static final LocalDateTime FIXED_TIMESTAMP = LocalDateTime.of(2019, 12, 6, 10, 15, 30);
  private static final LocalDate FIXED_DATE = LocalDate.of(2019, 12, 6);
  private static final LocalTime FIXED_TIME = LocalTime.of(10, 15, 30);
  private static final String TIMESTAMP_STRING = "2019-12-06T10:15:30";
  private static final String DATE_STRING = "2019-12-06";
  private static final String TIME_STRING = "10:15:30";

  private static Stream<Arguments> parametersForGenerateRandomValueForField() {
    return Stream.of(
        Arguments.of("string", 1, Collections.singletonList("testString"), new Field("name", SchemaBuilder.builder().stringType()),
                     "testString"),
        Arguments.of("string", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("int", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().stringType()), "1"),
        Arguments.of("int", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("long", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().longType()), 1L),
        Arguments.of("short", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().intType()), (short) 1),
        Arguments.of("double", 1, Collections.singletonList("1.0"), new Field("name", SchemaBuilder.builder().doubleType()), 1.0),
        Arguments.of("float", 1, Collections.singletonList("1.0"), new Field("name", SchemaBuilder.builder().floatType()), 1.0f),
        Arguments
            .of("timestamp", 1, Collections.singletonList(TIMESTAMP_STRING), new Field("name", SchemaBuilder.builder().stringType()),
                FIXED_TIMESTAMP),
        Arguments
            .of("longTimestamp", 1, Collections.singletonList(TIMESTAMP_STRING), new Field("name", SchemaBuilder.builder().longType()),
                FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("stringTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"),
                     new Field("name", SchemaBuilder.builder().stringType()),
                     "2019-12-06T12:00"),
        Arguments.of("uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
                     new Field("name", SchemaBuilder.builder().stringType()), UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments
            .of("boolean", 1, Collections.singletonList("true"), new Field("name", SchemaBuilder.builder().booleanType()), Boolean.TRUE),
        Arguments.of("boolean", 1, Collections.singletonList("true"), new Field("name", SchemaBuilder.builder().stringType()), "true"),
        Arguments
            .of("string", 1, Collections.singletonList("true"), new Field("name",
                                                                          SchemaBuilder.builder().booleanType()), Boolean.TRUE)
    );
  }

  private static Stream<Arguments> parametersForGenerateRandomValueForFieldLogicalTypes() {
    final Schema decimalSchemaBytes = SchemaBuilder.builder().bytesType();
    final Schema decimalSchemaFixed = SchemaBuilder.builder().fixed("decimal").size(5);
    LogicalTypes.decimal(5, 2).addToSchema(decimalSchemaBytes);
    LogicalTypes.decimal(5, 2).addToSchema(decimalSchemaFixed);

    final Map<ConstraintTypeEnum, String> decimalConstraints = new HashMap<>();
    decimalConstraints.put(ConstraintTypeEnum.SCALE, "2");
    decimalConstraints.put(ConstraintTypeEnum.PRECISION, "5");

    return Stream.of(
        Arguments.of("int_date", 1, Collections.singletonList(DATE_STRING), new Field("name",
                                                                                      SchemaBuilder.builder().intType()), FIXED_DATE, Collections.emptyMap()),
        Arguments.of("int_time-millis", 1, Collections.singletonList(TIME_STRING), new Field("name",
                                                                                             SchemaBuilder.builder().intType()), FIXED_TIME, Collections.emptyMap()),
        Arguments.of("long_time-micros", 1, Collections.singletonList(TIME_STRING), new Field("name",
                                                                                              SchemaBuilder.builder().longType()), FIXED_TIME, Collections.emptyMap()),
        Arguments.of("long_timestamp-millis", 1, Collections.singletonList(TIMESTAMP_STRING), new Field("name", SchemaBuilder.builder().longType()),
                     FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC), Collections.emptyMap()),
        Arguments.of("long_timestamp-micros", 1, Collections.singletonList(TIMESTAMP_STRING), new Field("name", SchemaBuilder.builder().longType()),
                     FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC), Collections.emptyMap()),
        Arguments.of("long_local-timestamp-millis", 1, Collections.singletonList(TIMESTAMP_STRING), new Field("name", SchemaBuilder.builder().longType()), FIXED_TIMESTAMP,
                     Collections.emptyMap()),
        Arguments.of("long_local-timestamp-micros", 1, Collections.singletonList(TIMESTAMP_STRING), new Field("name", SchemaBuilder.builder().longType()), FIXED_TIMESTAMP,
                     Collections.emptyMap()),
        Arguments.of("string_uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"), new Field("name", SchemaBuilder.builder().stringType()),
                     UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5").toString(), Collections.emptyMap()),
        Arguments.of("bytes_decimal", 1, Collections.singletonList("44.444"), new Field(
            "name", decimalSchemaBytes), new BigDecimal("44.444"), decimalConstraints),
        Arguments.of("fixed_decimal", 1, Collections.singletonList("55.555"), new Field(
            "name", decimalSchemaBytes), new BigDecimal("55.555").toString(), decimalConstraints)
    );
  }

  @ParameterizedTest
  @DisplayName("Testing Random Value for Field")
  @MethodSource("parametersForGenerateRandomValueForField")
  void testGenerateRandomValueForField(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Field field, final Object expected) {
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList(String.join(",", fieldValuesList))
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    Assertions.assertThat(new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, Collections.emptyMap())).isEqualTo(expected);
  }

  @ParameterizedTest
  @DisplayName("Testing Random Value for Field with Logical Types")
  @MethodSource("parametersForGenerateRandomValueForFieldLogicalTypes")
  void testGenerateRandomValueForFieldLogicalTypes(final String fieldType, final Integer valueLength, final List<String> fieldValuesList,
      final Field field, final Object expected,
      final Map<ConstraintTypeEnum, String> constraints) {
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList(String.join(",", fieldValuesList))
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    Assertions.assertThat(new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, constraints)).isEqualTo(expected);
  }

  private static Stream<Arguments> parametersForGenerateRandomValue() {
    return Stream.of(
        Arguments.of("int", 5, Collections.emptyList(), new Field("name", SchemaBuilder.builder().intType())),
        Arguments.of("long", 6, Collections.emptyList(), new Field("name", SchemaBuilder.builder().longType())),
        Arguments.of("float", 5, Collections.emptyList(), new Field("name", SchemaBuilder.builder().floatType())),
        Arguments.of("double", 6, Collections.emptyList(), new Field("name", SchemaBuilder.builder().doubleType())));
  }

  @Disabled("Test failure sometimes")
  @ParameterizedTest
  @DisplayName("Testing Generate a Random Value")
  @MethodSource("parametersForGenerateRandomValue")
  void testGenerateRandomValue(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Field field) {
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList(String.join(",", fieldValuesList))
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    final Object number = new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, Collections.emptyMap());
    Assertions.assertThat(number).isInstanceOfAny(Long.class, Integer.class, Double.class, Float.class);
    Assertions.assertThat(String.valueOf(number)).hasSize(valueLength);
  }

  private static Stream<Arguments> parametersForGenerateFieldValuesListIterator() {
    return Stream.of(
        Arguments.of(18,
                     List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10", "14", "17", "17", "17", "17", "18", "19", "20"),
                     ValidTypeConstants.INT,
                     List.of(1, 2, 3, 5, 6, 7, 7, 9, 9, 9, 10, 14, 17, 17, 17, 17, 18, 19, 20)),
        Arguments.of(20,
                     List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10", "14", "17", "17", "17", "17", "18", "19", "20"),
                     ValidTypeConstants.INT,
                     List.of(1, 2, 3, 5, 6, 7, 7, 9, 9, 9, 10, 14, 17, 17, 17, 17, 18, 19, 20, 1, 2)),
        Arguments.of(4,
                     List.of("first", "second", "third"),
                     ValidTypeConstants.STRING,
                     List.of("first", "second", "third", "first", "second")));
  }

  @ParameterizedTest
  @DisplayName("Testing generate an iterator of a list of values")
  @MethodSource("parametersForGenerateFieldValuesListIterator")
  void testGenerateFieldValuesListSequence(final int size, final List<String> fieldValuesList, final String fieldType, final List<Object> expected) {
    final var intList = new ArrayList<>();
    final Schema schema = fieldType.equals(ValidTypeConstants.INT) ? SchemaBuilder.builder().intType() : SchemaBuilder.builder().stringType();
    final Field field = new Field("name", schema);
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType("it")
                                                           .valueLength(0)
                                                           .fieldValueList(String.join(",", fieldValuesList))
                                                           .build();
    final var avroGeneratorTool = new AvroGeneratorTool();
    for (int i = 0; i <= size; i++) {
      intList.add(avroGeneratorTool.generateObject(field.schema(), fieldValueMapping, Collections.emptyMap()));
    }
    Assertions.assertThat(intList).containsExactlyElementsOf(expected);
  }

  private static Stream<Arguments> parametersForGenerateRandomValueForEnums() {
    return Stream.of(
        Arguments.of("enum", 1, Collections.singletonList("RED"),
                     new Field("name", SchemaBuilder.builder().enumeration("ENUM1").symbols("RED", "BLUE", "GREEN")), "RED"));
  }

  @ParameterizedTest
  @DisplayName("Testing Generate a Random Value for Enums")
  @MethodSource("parametersForGenerateRandomValueForEnums")
  void testGenerateRandomValueForEnums(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Field field, final Object expected) {
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList(String.join(",", fieldValuesList))
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    Assertions.assertThat(new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, Collections.emptyMap())).hasFieldOrPropertyWithValue("symbol", expected);
  }

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
        Arguments.of("seq", 1, Collections.singletonList("0"), new Field("name", SchemaBuilder.builder().intType()), 0),
        Arguments.of("seq", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("seq", 1, Collections.singletonList("2"), new Field("name", SchemaBuilder.builder().intType()), 2),
        Arguments.of("it", 1, Collections.singletonList("0"), new Field("name", SchemaBuilder.builder().stringType()), "0"),
        Arguments.of("it", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("it", 1, Collections.singletonList("2"), new Field("name", SchemaBuilder.builder().intType()), 2));
  }

  @ParameterizedTest
  @DisplayName("Testing Generate a Random Value for Field")
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Field field,
      final Object expectedTyped) {
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList(String.join(",", fieldValuesList))
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    Assertions.assertThat(new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, Collections.emptyMap())).isEqualTo(expectedTyped);
  }

  private static Stream<Arguments> parametersForShouldRecoverVariableFromContext() {
    return Stream.of(
        Arguments.of("string", 1, "testString", new Field("name", SchemaBuilder.builder().stringType()), "testString"),
        Arguments.of("int", 1, "1", new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("long", 1, "1", new Field("name", SchemaBuilder.builder().longType()), 1L),
        Arguments.of("short", 1, "1", new Field("name", SchemaBuilder.builder().intType()), (short) 1),
        Arguments.of("double", 1, "1.0", new Field("name", SchemaBuilder.builder().doubleType()), 1.0),
        Arguments.of("float", 1, "1.0", new Field("name", SchemaBuilder.builder().floatType()), 1.0f),
        Arguments.of("timestamp", 1, TIMESTAMP_STRING, new Field("name", SchemaBuilder.builder().stringType()),
                     FIXED_TIMESTAMP),
        Arguments.of("longTimestamp", 1, TIMESTAMP_STRING, new Field("name", SchemaBuilder.builder().longType()),
                     FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("stringTimestamp", 1, "2019-12-06T12:00", new Field("name", SchemaBuilder.builder().stringType()), "2019-12-06T12:00"),
        Arguments.of("uuid", 1, "0177f035-e51c-4a46-8b82-5b157371c2a5", new Field("name", SchemaBuilder.builder().stringType()),
                     UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments.of("boolean", 1, "true", new Field("name", SchemaBuilder.builder().booleanType()), Boolean.TRUE)
    );
  }

  private static Stream<Arguments> parametersForShouldRecoverVariableFromContextLogicalTypes() {
    final var decimalSchemaBytes = SchemaBuilder.builder().bytesType();
    final var decimalSchemaFixed = SchemaBuilder.builder().fixed("decimal").size(5);
    LogicalTypes.decimal(5, 2).addToSchema(decimalSchemaBytes);
    LogicalTypes.decimal(5, 2).addToSchema(decimalSchemaFixed);

    final Map<ConstraintTypeEnum, String> decimalConstraints = new HashMap<>();
    decimalConstraints.put(ConstraintTypeEnum.SCALE, "2");
    decimalConstraints.put(ConstraintTypeEnum.PRECISION, "5");

    return Stream.of(
        Arguments.of("int_date", 1, DATE_STRING, new Field("name",
                                                           SchemaBuilder.builder().intType()), FIXED_DATE, Collections.emptyMap()),
        Arguments.of("int_time-millis", 1, TIME_STRING, new Field("name",
                                                                  SchemaBuilder.builder().intType()), FIXED_TIME, Collections.emptyMap()),
        Arguments.of("long_time-micros", 1, TIME_STRING, new Field("name",
                                                                   SchemaBuilder.builder().longType()), FIXED_TIME, Collections.emptyMap()),
        Arguments.of("long_timestamp-millis", 1, TIMESTAMP_STRING, new Field("name", SchemaBuilder.builder().longType()), FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC),
                     Collections.emptyMap()),
        Arguments.of("long_timestamp-micros", 1, TIMESTAMP_STRING, new Field("name", SchemaBuilder.builder().longType()), FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC),
                     Collections.emptyMap()),
        Arguments.of("long_local-timestamp-millis", 1, TIMESTAMP_STRING, new Field("name", SchemaBuilder.builder().longType()), FIXED_TIMESTAMP, Collections.emptyMap()),
        Arguments.of("long_local-timestamp-micros", 1, TIMESTAMP_STRING, new Field("name", SchemaBuilder.builder().longType()), FIXED_TIMESTAMP, Collections.emptyMap()),
        Arguments.of("string_uuid", 1, "0177f035-e51c-4a46-8b82-5b157371c2a5", new Field("name", SchemaBuilder.builder().stringType()),
                     UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5").toString(), Collections.emptyMap()),
        Arguments.of("bytes_decimal", 1, "44.444", new Field(
            "name", decimalSchemaBytes), new BigDecimal("44.444"), decimalConstraints),
        Arguments.of("fixed_decimal", 1, "55.555", new Field(
            "name", decimalSchemaBytes), new BigDecimal("55.555").toString(), decimalConstraints)
    );
  }

  @ParameterizedTest
  @DisplayName("Testing Recover Variable from Context")
  @MethodSource("parametersForShouldRecoverVariableFromContext")
  void shouldRecoverVariableFromContext(final String fieldType, final Integer valueLength, final String value, final Field field, final Object expected) {
    final var variables = new JMeterVariables();
    variables.put("VARIABLE", value);
    JMeterContextService.getContext().setVariables(variables);
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList("${VARIABLE}")
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    Assertions.assertThat(new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, Collections.emptyMap())).isEqualTo(expected);
  }

  @ParameterizedTest
  @DisplayName("Testing Recover Variable from Context Logical Types")
  @MethodSource("parametersForShouldRecoverVariableFromContextLogicalTypes")
  void shouldRecoverVariableFromContext(final String fieldType, final Integer valueLength, final String value, final Field field,
      final Object expected, final Map<ConstraintTypeEnum, String> constraints) {
    final var variables = new JMeterVariables();
    variables.put("VARIABLE", value);
    JMeterContextService.getContext().setVariables(variables);
    final var fieldValueMapping = FieldValueMapping.builder()
                                                           .fieldName(field.name())
                                                           .fieldType(fieldType)
                                                           .valueLength(valueLength)
                                                           .fieldValueList("${VARIABLE}")
                                                           .required(true)
                                                           .isAncestorRequired(true)
                                                           .build();
    Assertions.assertThat(new AvroGeneratorTool().generateObject(field.schema(), fieldValueMapping, constraints)).isEqualTo(expected);
  }
}