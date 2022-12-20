/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.groovy.util.Maps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomIteratorTest {

  private static Stream<Arguments> parametersForGenerateIteratorValueForField() {
    return Stream.of(
            Arguments.of("name", "int", Collections.emptyList(), new HashMap<>(), null),
            Arguments.of("name", "float", Collections.singletonList("1"), new HashMap<>(), 1f),
            Arguments.of("name", "long", Collections.singletonList("2"), new HashMap<>(), 2L),
            Arguments.of("name", "bytes_decimal", Collections.singletonList("1"), new HashMap<>(Maps.of("name", new BigDecimal("15"))), new BigDecimal("1")),
            Arguments.of("name15", "bytes_decimal", Collections.emptyList(), new HashMap<>(Maps.of("name15", new BigDecimal("15"))), null));
  }

  @ParameterizedTest
  @DisplayName("Testing Generate an Iterator With an empty list or a list with one value")
  @MethodSource("parametersForGenerateIteratorValueForField")
  void testGenerateIteratorValueForField(final String fieldName, final String fieldType, final List<String> fieldValuesList, final Map<String, Object> context,
      final Object expectedStored) {
    Assertions.assertThat(RandomIterator.generateIt(fieldName, fieldType, fieldValuesList, context)).isEqualTo(expectedStored);
    if (Objects.isNull(expectedStored)) {
      Assertions.assertThat(context).doesNotContainKey(fieldName);
    } else {
      Assertions.assertThat(context).containsEntry(fieldName, expectedStored);
    }
  }

  private static Stream<Arguments> parametersForGenerateValueWithList() {
    return Stream.of(
            Arguments.of(10,
                    List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10"),
                    List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10")),
            Arguments.of(12,
                    List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10"),
                    List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10", "1", "2")));
  }

  @ParameterizedTest
  @DisplayName("Testing Generate an Iterator With a List of Values")
  @MethodSource("parametersForGenerateValueWithList")
  void testGenerateRandomValueWithList(final int size, final List<String> values, final List<String> expected) {
    final var intList = new ArrayList<>();
    final var context = new HashMap<String, Object>();
    for (int i = 0; i <= size; i++) {
      intList.add(RandomIterator.generateIteratorForFieldValueList("ClientCode", "it", values, context));
    }
    Assertions.assertThat(intList).containsExactlyElementsOf(expected);
  }
}