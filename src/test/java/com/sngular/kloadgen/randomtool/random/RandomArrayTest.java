/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomArrayTest {

  private static Stream<Arguments> parametersForGenerateArrayRandomValue() {
    return Stream.of(
        Arguments.of("string-array", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("int-array", 1, Collections.singletonList("1"), 1),
        Arguments.of("long-array", 1, Collections.singletonList("1"), 1L),
        Arguments.of("short-array", 1, Collections.singletonList("1"), (short) 1),
        Arguments.of("double-array", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("map-array", 1, Collections.singletonList("testString:testString,testString:testString"), 2),
        Arguments.of("float-array", 1, Collections.singletonList("1.0"), 1.0f),
        Arguments.of("uuid-array", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
                     UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5"))
    );
  }

  private static Stream<Arguments> parametersForGenerateArrayRandomValueZero() {
    return Stream.of(
        Arguments.of("float-array", 0, Collections.emptyList()),
        Arguments.of("int-array", 0, Collections.emptyList()));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateArrayRandomValue")
  void generateArrayRandomValue(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Object expected) {
    Assertions.assertThat((List<Object>) new RandomArray().generateArray(fieldType, valueLength, fieldValuesList, 1, Collections.emptyMap()))
              .allMatch(value -> value.equals(expected));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateArrayRandomValueZero")
  void generateArrayRandomValueZero(final String fieldType, final Integer valueLength, final List<String> fieldValuesList) {
    Assertions.assertThat((List<Object>) new RandomArray().generateArray(fieldType, valueLength, fieldValuesList, 1, Collections.emptyMap()))
              .isNotNull();
    final Object number1 = new RandomArray().generateArray(fieldType, valueLength, fieldValuesList, 1, Collections.emptyMap());
    final Object number2 = new RandomArray().generateArray(fieldType, valueLength, fieldValuesList, 1, Collections.emptyMap());
    final List<Object> number3 = (List<Object>) new RandomArray().generateArray(fieldType, valueLength, fieldValuesList, 1, Collections.emptyMap());
    Assertions.assertThat(number1).isNotNull();
    Assertions.assertThat(number2).isNotNull();
    Assertions.assertThat(number3).isNotNull();
    Assertions.assertThat(number1).isNotEqualTo(number2);
  }
}