/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomIteratorTest {

  private static Stream<Arguments> parametersForGenerateIteratorValueForField() {
    return Stream.of(
            Arguments.of("name", "int", emptyList(), new HashMap<>(), null),
            Arguments.of("name", "float", singletonList("1"), new HashMap<>(), 1f),
            Arguments.of("name", "long", singletonList("2"), new HashMap<>(), 2L),
            Arguments.of("name", "bytes_decimal", singletonList("1"), new HashMap<>(Maps.of("name", new BigDecimal("15"))), new BigDecimal("1")),
            Arguments.of("name15", "bytes_decimal", emptyList(), new HashMap<>(Maps.of("name15", new BigDecimal("15"))), null));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateIteratorValueForField")
  void testGenerateIteratorValueForField(String fieldName, String fieldType, List<String> fieldValuesList, Map<String, Object> context,
                                         Object expectedStored) {
    assertThat(RandomIterator.generateIt(fieldName, fieldType, fieldValuesList, context)).isEqualTo(expectedStored);
    if (Objects.isNull(expectedStored)) {
      assertThat(context).doesNotContainKey(fieldName);
    } else {
      assertThat(context).containsEntry(fieldName, expectedStored);
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
  void testGenerateRandomValueWithList(int size, List<String> values, List<String> expected) {
    var intList = new ArrayList<>();
    var context = new HashMap<String, Object>();
    for (int i=0; i <= size; i++) {
      intList.add(RandomIterator.generateIteratorForFieldValueList("ClientCode", "it", values, context));
    }
    assertThat(intList).containsExactlyElementsOf(expected);
  }
}