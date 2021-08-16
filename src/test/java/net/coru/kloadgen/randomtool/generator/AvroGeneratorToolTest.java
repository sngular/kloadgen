package net.coru.kloadgen.randomtool.generator;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AvroGeneratorToolTest {

  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2019, 12, 6, 12, 0, 0);

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
            .of("timestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), new Field("name", SchemaBuilder.builder().stringType()),
                FIXED_DATE),
        Arguments
            .of("longTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), new Field("name", SchemaBuilder.builder().longType()),
                FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("stringTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"),
            new Field("name", SchemaBuilder.builder().stringType()),
            "2019-12-06T12:00"),
        Arguments.of("uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
            new Field("name", SchemaBuilder.builder().stringType()), UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments
            .of("boolean", 1, Collections.singletonList("true"), new Field("name", SchemaBuilder.builder().booleanType()), Boolean.TRUE),
        Arguments.of("boolean", 1, Collections.singletonList("true"), new Field("name", SchemaBuilder.builder().stringType()), "true"),
        Arguments
            .of("string", 1, Collections.singletonList("true"), new Field("name", SchemaBuilder.builder().booleanType()), Boolean.TRUE));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateRandomValueForField")
  void testGenerateRandomValueForField(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field, Object expected) {
    assertThat(new AvroGeneratorTool().generateObject(field, fieldType, valueLength, fieldValuesList, Collections.emptyMap())).isEqualTo(expected);
  }

  private static Stream<Arguments> parametersForGenerateRandomValue() {
    return Stream.of(
        Arguments.of("int", 5, emptyList(), new Field("name", SchemaBuilder.builder().intType())),
        Arguments.of("long", 6, emptyList(), new Field("name", SchemaBuilder.builder().longType())),
        Arguments.of("float", 5, emptyList(), new Field("name", SchemaBuilder.builder().floatType())),
        Arguments.of("double", 6, emptyList(), new Field("name", SchemaBuilder.builder().doubleType())));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateRandomValue")
  void testGenerateRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field) {
    Object number = new AvroGeneratorTool().generateObject(field, fieldType, valueLength, fieldValuesList, Collections.emptyMap());
    assertThat(number).isInstanceOfAny(Long.class, Integer.class, Double.class, Float.class);
    assertThat(String.valueOf(number)).hasSize(valueLength);
  }

  private static Stream<Arguments> parametersForGenerateRandomValueForEnums() {
    return Stream.of(
        Arguments.of("enum", 1, Collections.singletonList("RED"),
            new Field("name", SchemaBuilder.builder().enumeration("ENUM1").symbols("RED", "BLUE", "GREEN")), "RED"));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateRandomValueForEnums")
  void testGenerateRandomValueForEnums(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field, Object expected) {
    assertThat(new AvroGeneratorTool().generateObject(field, fieldType, valueLength, fieldValuesList, Collections.emptyMap()))
        .hasFieldOrPropertyWithValue("symbol", expected);
  }

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
        Arguments.of("seq", 1, Collections.singletonList("0"), new Field("name", SchemaBuilder.builder().stringType()), "0"),
        Arguments.of("seq", 1, Collections.singletonList("1"), new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("seq", 1, Collections.singletonList("2"), new Field("name", SchemaBuilder.builder().intType()), 2));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field,
      Object expectedTyped) {
    assertThat(new AvroGeneratorTool().generateObject(field, fieldType, valueLength, fieldValuesList, Collections.emptyMap())).isEqualTo(expectedTyped);
  }

  private static Stream<Arguments> parametersForShouldRecoverVariableFromContext() {
    return Stream.of(
        Arguments.of("string", 1, "testString", new Field("name", SchemaBuilder.builder().stringType()), "testString"),
        Arguments.of("int", 1, "1", new Field("name", SchemaBuilder.builder().intType()), 1),
        Arguments.of("long", 1, "1", new Field("name", SchemaBuilder.builder().longType()), 1L),
        Arguments.of("short", 1, "1", new Field("name", SchemaBuilder.builder().intType()), (short) 1),
        Arguments.of("double", 1, "1.0", new Field("name", SchemaBuilder.builder().doubleType()), 1.0),
        Arguments.of("float", 1, "1.0", new Field("name", SchemaBuilder.builder().floatType()), 1.0f),
        Arguments.of("timestamp", 1, "2019-12-06T12:00:00", new Field("name", SchemaBuilder.builder().stringType()), FIXED_DATE),
        Arguments.of("longTimestamp", 1, "2019-12-06T12:00:00", new Field("name", SchemaBuilder.builder().longType()),
            FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("stringTimestamp", 1, "2019-12-06T12:00", new Field("name", SchemaBuilder.builder().stringType()), "2019-12-06T12:00"),
        Arguments.of("uuid", 1, "0177f035-e51c-4a46-8b82-5b157371c2a5", new Field("name", SchemaBuilder.builder().stringType()),
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments.of("boolean", 1, "true", new Field("name", SchemaBuilder.builder().booleanType()), Boolean.TRUE)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForShouldRecoverVariableFromContext")
  void shouldRecoverVariableFromContext(String fieldType, Integer valueLength, String value, Field field, Object expected) {
    JMeterVariables variables = new JMeterVariables();
    variables.put("VARIABLE", value);
    JMeterContextService.getContext().setVariables(variables);
    assertThat(new AvroGeneratorTool().generateObject(field, fieldType, valueLength, Collections.singletonList("$" +
            "{VARIABLE}"),Collections.emptyMap()))
        .isEqualTo(expected);
  }
}