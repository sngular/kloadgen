/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.generator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StatelessGeneratorToolTest {

  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2019, 12, 6, 12, 0, 0);

  private static Stream<Arguments> parametersForGenerateRandomValueForField() {
    return Stream.of(
        Arguments.of("name", "string", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("name", "int", 1, Collections.singletonList("1"), 1),
        Arguments.of("name", "long", 1, Collections.singletonList("1"), 1L),
        Arguments.of("name", "short", 1, Collections.singletonList("1"), (short) 1),
        Arguments.of("name", "double", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("name", "timestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), FIXED_DATE),
        Arguments.of("name", "longTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"),
                     FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("name", "stringTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), "2019-12-06T12:00"),
        Arguments.of("name", "uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
                     UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments.of("name", "boolean", 1, Collections.singletonList("true"), Boolean.TRUE));
  }

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
        Arguments.of("name", "seq", 1, Collections.singletonList("0"), "0"),
        Arguments.of("name", "seq", 1, Collections.singletonList("1"), "1"),
        Arguments.of("name", "seq", 1, Collections.singletonList("2"), "2"),
        Arguments.of("name", "it", 1, Collections.singletonList("0"), "0"),
        Arguments.of("name", "it", 1, Collections.singletonList("1"), "1"),
        Arguments.of("name", "it", 1, Collections.singletonList("2"), "2"));
  }

  private static Stream<Arguments> parametersForShouldRecoverVariableFromContext() {
    return Stream.of(
        Arguments.of("name", "string", 1, "testString", "testString"),
        Arguments.of("name", "int", 1, "1", 1),
        Arguments.of("name", "long", 1, "1", 1L),
        Arguments.of("name", "short", 1, "1", (short) 1),
        Arguments.of("name", "double", 1, "1.0", 1.0),
        Arguments.of("name", "timestamp", 1, "2019-12-06T12:00:00", FIXED_DATE),
        Arguments.of("name", "longTimestamp", 1, "2019-12-06T12:00:00", FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("name", "stringTimestamp", 1, "2019-12-06T12:00", "2019-12-06T12:00"),
        Arguments.of("name", "uuid", 1, "0177f035-e51c-4a46-8b82-5b157371c2a5", UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments.of("name", "boolean", 1, "true", Boolean.TRUE)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateRandomValueForField")
  void testGenerateRandomValueForField(
      final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Object expected) {
    Assertions.assertThat(new StatelessGeneratorTool().generateObject(fieldName, fieldType, valueLength, fieldValuesList)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(
      final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Object expectedTyped) {
    Assertions.assertThat(new StatelessGeneratorTool().generateObject(fieldName, fieldType, valueLength, fieldValuesList)).isEqualTo(expectedTyped);
  }

  @ParameterizedTest
  @MethodSource("parametersForShouldRecoverVariableFromContext")
  void shouldRecoverVariableFromContext(final String fieldName, final String fieldType, final Integer valueLength, final String value, final Object expected) {
    final JMeterVariables variables = new JMeterVariables();
    variables.put("VARIABLE", value);
    JMeterContextService.getContext().setVariables(variables);
    Assertions.assertThat(new StatelessGeneratorTool().generateObject(fieldName, fieldType, valueLength, Collections.singletonList("${VARIABLE}")))
              .isEqualTo(expected);
  }
}