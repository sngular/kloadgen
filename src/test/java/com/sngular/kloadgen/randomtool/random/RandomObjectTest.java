/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.sngular.kloadgen.model.ConstraintTypeEnum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomObjectTest {

  private static final LocalDateTime FIXED_TIMESTAMP = LocalDateTime.of(2019, 12, 6, 10, 15, 30);

  private static final LocalDate FIXED_DATE = LocalDate.of(2019, 12, 6);

  private static final LocalTime FIXED_TIME = LocalTime.of(10, 15, 30);

  private static final String TIMESTAMP_STRING = "2019-12-06T10:15:30";

  private static final String DATE_STRING = "2019-12-06";

  private static final String TIME_STRING = "10:15:30";

  private static Stream<Arguments> parametersForGenerateSingleRandomValue() {
    return Stream.of(
        Arguments.of("string", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("int", 1, Collections.singletonList("1"), 1),
        Arguments.of("long", 1, Collections.singletonList("1"), 1L),
        Arguments.of("short", 1, Collections.singletonList("1"), (short) 1),
        Arguments.of("double", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("float", 1, Collections.singletonList("1.0"), 1.0f),
        Arguments.of("timestamp", 1, Collections.singletonList(TIMESTAMP_STRING), FIXED_TIMESTAMP),
        Arguments.of(
            "longTimestamp", 1, Collections.singletonList(TIMESTAMP_STRING), FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC).toEpochMilli()
        ),
        Arguments.of("stringTimestamp", 1, Collections.singletonList(TIMESTAMP_STRING), TIMESTAMP_STRING),
        Arguments.of(
            "uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")
        ),
        Arguments.of("boolean", 1, Collections.singletonList("true"), Boolean.TRUE)
    );
  }

  private static Stream<Arguments> parametersForGenerateSingleLogicalTypeRandomValue() {
    final Map<ConstraintTypeEnum, String> decimalConstraints = new HashMap<>();
    decimalConstraints.put(ConstraintTypeEnum.SCALE, "2");
    decimalConstraints.put(ConstraintTypeEnum.PRECISION, "5");
    return Stream.of(
        Arguments.of("int_date", 1, Collections.singletonList(DATE_STRING), FIXED_DATE, Collections.emptyMap()),
        Arguments.of("int_time-millis", 1, Collections.singletonList(TIME_STRING), FIXED_TIME, Collections.emptyMap()),
        Arguments.of("long_time-micros", 1, Collections.singletonList(TIME_STRING), FIXED_TIME, Collections.emptyMap()),
        Arguments.of("long_timestamp-millis", 1, Collections.singletonList(TIMESTAMP_STRING),
                     FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC), Collections.emptyMap()),
        Arguments.of("long_timestamp-micros", 1, Collections.singletonList(TIMESTAMP_STRING),
                     FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC), Collections.emptyMap()),
        Arguments.of("long_local-timestamp-millis", 1, Collections.singletonList(TIMESTAMP_STRING),
                     FIXED_TIMESTAMP, Collections.emptyMap()),
        Arguments.of("long_local-timestamp-micros", 1, Collections.singletonList(TIMESTAMP_STRING),
                     FIXED_TIMESTAMP, Collections.emptyMap()),
        Arguments.of(
            "string_uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5"), Collections.emptyMap()
        ),
        Arguments.of("bytes_decimal", 1, Collections.singletonList("55.555"), new BigDecimal("55.555"),
                     decimalConstraints),
        Arguments.of("fixed_decimal", 1, Collections.singletonList("55.555"), new BigDecimal("55.555"),
                     decimalConstraints)

    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSingleRandomValue")
  void generateSingleRandomValue(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Object expected) {
    Assertions.assertThat(new RandomObject().generateRandom(fieldType, valueLength, fieldValuesList, Collections.emptyMap())).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSingleLogicalTypeRandomValue")
  void generateSingleLogicalTypeRandomValue(
      final String fieldType, final Integer valueLength, final List<String> fieldValuesList,
      final Object expected, final Map<ConstraintTypeEnum, String> constraints) {
    Assertions.assertThat(new RandomObject().generateRandom(fieldType, valueLength, fieldValuesList, constraints)).isEqualTo(expected);
  }
}