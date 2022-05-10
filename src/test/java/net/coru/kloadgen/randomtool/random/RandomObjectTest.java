/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import net.coru.kloadgen.model.ConstraintTypeEnum;
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
        Arguments.of("string", 1, singletonList("testString"), "testString"),
        Arguments.of("int", 1, singletonList("1"), 1),
        Arguments.of("long", 1, singletonList("1"), 1L),
        Arguments.of("short", 1, singletonList("1"), (short) 1),
        Arguments.of("double", 1, singletonList("1.0"), 1.0),
        Arguments.of("float", 1, singletonList("1.0"), 1.0f),
        Arguments.of("timestamp", 1, singletonList(TIMESTAMP_STRING), FIXED_TIMESTAMP),
        Arguments.of(
            "longTimestamp", 1, singletonList(TIMESTAMP_STRING), FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC).toEpochMilli()
        ) ,
        Arguments.of("stringTimestamp" , 1 , singletonList(TIMESTAMP_STRING) , TIMESTAMP_STRING) ,
        Arguments.of(
            "uuid" , 1 , singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5") ,
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")
        ) ,
        Arguments.of("boolean" , 1 , singletonList("true") , Boolean.TRUE)
    );
  }

  private static Stream<Arguments> parametersForGenerateSingleLogicalTypeRandomValue() {
    Map<ConstraintTypeEnum, String> decimalConstraints = new HashMap<>();
    decimalConstraints.put(ConstraintTypeEnum.SCALE , "2");
    decimalConstraints.put(ConstraintTypeEnum.PRECISION , "5");
    return Stream.of(
        Arguments.of("int_date" , 1 , singletonList(DATE_STRING) , FIXED_DATE , emptyMap()) ,
        Arguments.of("int_time-millis" , 1 , singletonList(TIME_STRING) , FIXED_TIME , emptyMap()) ,
        Arguments.of("long_time-micros" , 1 , singletonList(TIME_STRING) , FIXED_TIME , emptyMap()) ,
        Arguments.of("long_timestamp-millis" , 1 , singletonList(TIMESTAMP_STRING) ,
                    FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC), emptyMap()),
            Arguments.of("long_timestamp-micros", 1, singletonList(TIMESTAMP_STRING),
                    FIXED_TIMESTAMP.toInstant(ZoneOffset.UTC), emptyMap()),
            Arguments.of("long_local-timestamp-millis", 1, singletonList(TIMESTAMP_STRING),
                    FIXED_TIMESTAMP, emptyMap()),
            Arguments.of("long_local-timestamp-micros", 1, singletonList(TIMESTAMP_STRING),
                    FIXED_TIMESTAMP, emptyMap()),
            Arguments.of(
                    "string_uuid", 1, singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
                    UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5"), emptyMap()
            ),
            Arguments.of("bytes_decimal", 1, singletonList("55.555"), new BigDecimal("55.555"),
                    decimalConstraints),
            Arguments.of("fixed_decimal", 1, singletonList("55.555"), new BigDecimal("55.555"),
                    decimalConstraints)

    );
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSingleRandomValue")
  void generateSingleRandomValue(String fieldType , Integer valueLength , List<String> fieldValuesList , Object expected) {
    assertThat(new RandomObject().generateRandom(fieldType , valueLength , fieldValuesList , emptyMap())).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSingleLogicalTypeRandomValue")
  void generateSingleLogicalTypeRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList,
                                            Object expected, Map<ConstraintTypeEnum,String> constraints) {
    assertThat(new RandomObject().generateRandom(fieldType, valueLength, fieldValuesList, constraints)).isEqualTo(expected);
  }
}