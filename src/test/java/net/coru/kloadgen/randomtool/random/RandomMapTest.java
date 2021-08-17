package net.coru.kloadgen.randomtool.random;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomMapTest {

  private static Stream<Arguments> parametersForGenerateMapRandomValueFromList() {
    return Stream.of(
        Arguments.of("string-map", 1, Collections.singletonList("testString:testString"), Maps.of("testString", "testString"), 1),
        Arguments.of("int-map", 1, Collections.singletonList("testString:1"), Maps.of("testString", 1), 1),
        Arguments.of("long-map", 1, Collections.singletonList("testString:1"), Maps.of("testString", 1L), 1),
        Arguments.of("short-map", 1, Collections.singletonList("testString:1"), Maps.of("testString", (short) 1), 1),
        Arguments.of("double-map", 1, Collections.singletonList("testString:1.0"), Maps.of("testString", 1.0), 1),
        Arguments.of(
            "uuid-map", 1, Collections.singletonList("testString:0177f035-e51c-4a46-8b82-5b157371c2a5"),
            Maps.of("testString", UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")), 1
        )
    );
  }

  private static Stream<Arguments> parametersForGenerateMapArrayRandomValueFromList() {
    return Stream.of(
        Arguments.of(
            "string-map-array", 1, Collections.singletonList("testString:testString"),
            Collections.singletonList(Maps.of("testString", "testString")), 1
        ),
        Arguments.of(
            "int-map-array", 1, Collections.singletonList("testString:1"), Collections.singletonList(Maps.of("testString", 1)), 1
        ),
        Arguments.of(
            "long-map-array", 1, Collections.singletonList("testString:1"), Collections.singletonList(Maps.of("testString", 1L)), 1
        ),
        Arguments.of(
            "short-map-array", 1, Collections.singletonList("testString:1"), Collections.singletonList(Maps.of("testString", (short) 1)), 1
        ),
        Arguments.of(
            "double-map-array", 1, Collections.singletonList("testString:1.0"),
            Collections.singletonList(Maps.of("testString", 1.0)), 1
        ),
        Arguments.of(
            "uuid-map-array", 1, Collections.singletonList("testString:0177f035-e51c-4a46-8b82-5b157371c2a5"),
            Collections.singletonList(Maps.of("testString", UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5"))), 1
        )
    );
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
  @MethodSource("parametersForGenerateMapRandomValueFromList")
  void generateMapRandomValueFromList(String fieldType, Integer valueLength, List<String> fieldValuesList, Map<String, Object> expected,
      Integer size) {
    Map.Entry<String, Object>[] expectedMap = expected.entrySet().toArray(new Map.Entry[1]);
    Map<String, Object> result =
        (Map<String, Object>) new RandomMap().generateMap(fieldType, valueLength, fieldValuesList, size, Collections.emptyMap());
    assertThat(result).containsExactly(expectedMap);
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapArrayRandomValueFromList")
  void generateMapArrayRandomValueFromList(String fieldType, Integer valueLength, List<String> fieldValuesList,
      List<Map<String, Object>> expected, Integer size) {

    List<Map<String, Object>> result =
        (List<Map<String, Object>>) new RandomMap().generateMap(fieldType, valueLength, fieldValuesList, size, Collections.emptyMap());
    assertThat(result).containsExactly(expected.get(0));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapFixedKeyRandomValue")
  void generateMapFixedKeyRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {
    String[] expectedKeys = fieldValuesList.toArray(new String[1]);
    Map<String, Object> result =
        (Map<String, Object>) new RandomMap().generateMap(fieldType, valueLength, fieldValuesList, size, Collections.emptyMap());
    assertThat(result).containsKeys(expectedKeys).doesNotContainValue(null);
  }
}