package net.coru.kloadgen.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomToolTest {

  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2019, 12, 6, 12, 0, 0);

  private static Stream<Arguments> parametersForGenerateSingleRandomValue() {
    return Stream.of(
        Arguments.of("string", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("int", 1, Collections.singletonList("1"), 1),
        Arguments.of("long", 1, Collections.singletonList("1"), 1L),
        Arguments.of("short", 1, Collections.singletonList("1"), (short)1),
        Arguments.of("double", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("timestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), FIXED_DATE),
        Arguments.of("longTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("stringTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), "2019-12-06T12:00"),
        Arguments.of("uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"), UUID.fromString(
            "0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments.of("boolean", 1, Collections.singletonList("true"), Boolean.TRUE)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSingleRandomValue")
  void generateSingleRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList, Object expected) {
    assertThat(RandomTool.generateRandom(fieldType, valueLength, fieldValuesList)).isEqualTo(expected);
  }

  private static Stream<Arguments> parametersForGenerateArrayRandomValue() {
    return Stream.of(
        Arguments.of("string-array", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("int-array", 1, Collections.singletonList("1"), 1),
        Arguments.of("long-array", 1, Collections.singletonList("1"), 1L),
        Arguments.of("short-array", 1, Collections.singletonList("1"), (short) 1),
        Arguments.of("double-array", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("uuid-array", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5"))
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateArrayRandomValue")
  void generateArrayRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList, Object expected) {
    assertThat((List<Object>)RandomTool.generateRandomList(fieldType, 1, valueLength, fieldValuesList))
        .allMatch(value -> value.equals(expected));
  }

  private static Stream<Arguments> parametersForGenerateMapRandomValueFromList() {
    return Stream.of(
        Arguments.of("string-map", 1, Collections.singletonList("testString:testString"), Maps.of("testString", "testString"),1),
        Arguments.of("int-map", 1, Collections.singletonList("testString:1"),  Maps.of("testString",1),1),
        Arguments.of("long-map", 1, Collections.singletonList("testString:1"),  Maps.of("testString",1L),1),
        Arguments.of("short-map", 1, Collections.singletonList("testString:1"),  Maps.of("testString",(short)1),1),
        Arguments.of("double-map", 1, Collections.singletonList("testString:1.0"),  Maps.of("testString",1.0),1),
        Arguments.of("uuid-map", 1, Collections.singletonList("testString:0177f035-e51c-4a46-8b82-5b157371c2a5"),  Maps.of("testString", UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),1)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapRandomValueFromList")
  void generateMapRandomValueFromList(String fieldType, Integer valueLength, List<String> fieldValuesList, Map<String, Object> expected,Integer size) {
    Map.Entry<String, Object>[] expectedMap = expected.entrySet().toArray(new Map.Entry[1]);
    assertThat((Map<String, Object>)RandomTool.generateRandomMap(fieldType, valueLength, fieldValuesList,size))
        .containsExactly(expectedMap);
  }

  private static Stream<Arguments> parametersForGenerateMapArrayRandomValueFromList() {
    return Stream.of(
        Arguments.of("string-map-array", 1, Collections.singletonList("testString:testString"), Collections.singletonList(Maps.of("testString", "testString")), 1),
        Arguments.of("int-map-array", 1, Collections.singletonList("testString:1"), Collections.singletonList(Maps.of("testString",1)), 1),
        Arguments.of("long-map-array", 1, Collections.singletonList("testString:1"), Collections.singletonList(Maps.of("testString",1L)), 1),
        Arguments.of("short-map-array", 1, Collections.singletonList("testString:1"), Collections.singletonList(Maps.of("testString",(short)1)), 1),
        Arguments.of("double-map-array", 1, Collections.singletonList("testString:1.0"), Collections.singletonList(Maps.of("testString",1.0)), 1),
        Arguments.of("uuid-map-array", 1, Collections.singletonList("testString:0177f035-e51c-4a46-8b82-5b157371c2a5"), Collections.singletonList(Maps.of("testString", UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5"))), 1)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapArrayRandomValueFromList")
  void generateMapArrayRandomValueFromList(String fieldType, Integer valueLength, List<String> fieldValuesList, List<Map<String, Object>> expected, Integer size) {

    assertThat((List<Map<String, Object>>)RandomTool.generateRandomMap(fieldType, valueLength, fieldValuesList,size))
        .containsExactly(expected.get(0));
  }

  private static Stream<Arguments> parametersForGenerateMapFixedKeyRandomValue() {
    return Stream.of(
        Arguments.of("string-map", 1, Collections.singletonList("testString"), 1),
        Arguments.of("int-map", 1, Collections.singletonList("testString"), 1),
        Arguments.of("long-map", 1, Collections.singletonList("testString"), 1),
        Arguments.of("short-map", 1, Collections.singletonList("testString"), 1),
        Arguments.of("double-map", 1, Collections.singletonList("testString"), 1),
        Arguments.of("uuid-map", 1, Collections.singletonList("testString"), 1)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapFixedKeyRandomValue")
  void generateMapFixedKeyRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {
    String[] expectedKeys = fieldValuesList.toArray(new String[1]);
    Map<String, Object> result = (Map<String, Object>)RandomTool.generateRandomMap(fieldType, valueLength, fieldValuesList,size);
    assertThat(result).containsKeys(expectedKeys).doesNotContainValue(null);
  }

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
        Arguments.of("name", "seq", Collections.singletonList("0"), new HashMap<>(), "0", 0L),
        Arguments.of("name", "seq", Collections.singletonList("1"), new HashMap<>(Maps.of("name", 15L)), "16", 16L));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(String fieldName, String fieldType, List<String> fieldValuesList, Map<String, Object> context,
      Object expectedTyped, Object expectedStored) {

    assertThat(RandomTool.generateSeq(fieldName, fieldType, fieldValuesList, context)).isEqualTo(expectedTyped);
    assertThat(context).containsEntry(fieldName,expectedStored);
  }

}