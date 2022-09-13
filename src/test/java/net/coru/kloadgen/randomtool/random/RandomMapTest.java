/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.groovy.util.Maps;
import org.assertj.core.api.Assertions;
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
  void generateMapRandomValueFromList(
      final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Map<String, Object> expected, final Integer size) {
    final Map.Entry<String, Object>[] expectedMap = expected.entrySet().toArray(new Map.Entry[1]);
    final Map<String, Object> result =
        (Map<String, Object>) new RandomMap().generateMap(fieldType, valueLength, fieldValuesList, size, Collections.emptyMap());
    Assertions.assertThat(result).containsExactly(expectedMap);
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapArrayRandomValueFromList")
  void generateMapArrayRandomValueFromList(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final List<Map<String, Object>> expected,
      final Integer size) {

    final List<Map<String, Object>> result =
        (List<Map<String, Object>>) new RandomMap().generateMap(fieldType, valueLength, fieldValuesList, size, Collections.emptyMap());
    Assertions.assertThat(result).containsExactly(expected.get(0));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateMapFixedKeyRandomValue")
  void generateMapFixedKeyRandomValue(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Integer size) {
    final String[] expectedKeys = fieldValuesList.toArray(new String[1]);
    final Map<String, Object> result =
        (Map<String, Object>) new RandomMap().generateMap(fieldType, valueLength, fieldValuesList, size, Collections.emptyMap());
    Assertions.assertThat(result).containsKeys(expectedKeys).doesNotContainValue(null);
  }
}