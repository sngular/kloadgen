/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomSequenceTest {

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
            Arguments.of("name", "int", emptyList(), new HashMap<>(), 1),
            Arguments.of("name", "float", emptyList(), new HashMap<>(), 1f),
            Arguments.of("name", "long", singletonList("0"), new HashMap<>(), 0L),
            Arguments.of("name", "bytes_decimal", singletonList("1"), new HashMap<>(Maps.of("name", new BigDecimal("15"))), new BigDecimal("16")));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(String fieldName, String fieldType, List<String> fieldValuesList, Map<String, Object> context,
                                         Object expectedStored) {
    assertThat(RandomSequence.generateSeq(fieldName, fieldType, fieldValuesList, context)).isEqualTo(expectedStored);
    assertThat(context).containsEntry(fieldName, expectedStored);
  }

  private static Stream<Arguments> parametersForGenerateRandomValueWithList() {
    return Stream.of(
            Arguments.of(18,
                    List.of("1", "2", "3", "5", "6", "7", "7", "9", "9", "9", "10", "14", "17", "17", "17", "17", "18", "19", "20")));
  }

  @ParameterizedTest
  @DisplayName("Testing Generate a Random Value With a List of Values")
  @MethodSource("parametersForGenerateRandomValueWithList")
  void testGenerateRandomValueWithList(final int size, final List<String> values) {
    final Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      final var intList = new ArrayList<>();
      final var context = new HashMap<String, Object>();
      for (int i=0; i <= size; i++) {
        intList.add(RandomSequence.generateSeq("ClientCode", "seq", values, context));
      }
    });

    final String expectedMessage = "Sequences do not accept more than one option as initial value";
    final String actualMessage = exception.getMessage();

    Assertions.assertTrue(actualMessage.contains(expectedMessage));
  }
}