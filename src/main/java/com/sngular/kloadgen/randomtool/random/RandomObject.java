/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.randomtool.util.ValidTypeConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public final class RandomObject {

  private final Random rand = new Random();

  public boolean isTypeValid(final String type) {
    return ValidTypeConstants.VALID_OBJECT_TYPES.contains(type);
  }

  public Object generateRandom(
      final String fieldType, final Integer valueLength, final List<String> fieldValueList,
      final Map<ConstraintTypeEnum, String> constraints) {
    final String fixFieldType = StringUtils.defaultString(fieldType, "string");
    return switch (fixFieldType.toLowerCase()) {
      case ValidTypeConstants.STRING -> getStringValueOrRandom(valueLength, fieldValueList, constraints);
      case ValidTypeConstants.INT -> {
        try {
          yield getIntegerValueOrRandom(valueLength, fieldValueList, constraints).intValueExact();
        } catch (final ArithmeticException exception) {
          yield Integer.MAX_VALUE;
        }
      }
      case ValidTypeConstants.LONG -> {
        try {
          yield getIntegerValueOrRandom(valueLength, fieldValueList, constraints).longValueExact();
        } catch (final ArithmeticException exception) {
          yield Long.MAX_VALUE;
        }
      }
      case ValidTypeConstants.SHORT -> {
        try {
          yield getIntegerValueOrRandom(valueLength, fieldValueList, constraints).shortValueExact();
        } catch (final ArithmeticException exception) {
          yield Short.MAX_VALUE;
        }
      }
      case ValidTypeConstants.DOUBLE -> {
        try {
          yield getDecimalValueOrRandom(valueLength, fieldValueList, constraints).doubleValue();
        } catch (final ArithmeticException exception) {
          yield Double.MAX_VALUE;
        }
      }
      case ValidTypeConstants.NUMBER, ValidTypeConstants.FLOAT -> {
        try {
          yield getDecimalValueOrRandom(valueLength, fieldValueList, constraints).floatValue();
        } catch (final ArithmeticException exception) {
          yield Float.MAX_VALUE;
        }
      }
      case ValidTypeConstants.BYTES -> {
        try {
          yield getIntegerValueOrRandom(valueLength, Collections.emptyList(), Collections.emptyMap()).byteValueExact();
        } catch (final ArithmeticException exception) {
          yield Byte.MAX_VALUE;
        }
      }
      case ValidTypeConstants.TIMESTAMP, ValidTypeConstants.LONG_TIMESTAMP, ValidTypeConstants.STRING_TIMESTAMP -> getTimestampValueOrRandom(fieldType, fieldValueList);
      case ValidTypeConstants.BOOLEAN -> getBooleanValueOrRandom(fieldValueList);
      case ValidTypeConstants.ENUM -> getEnumValueOrRandom(fieldValueList);
      case ValidTypeConstants.INT_DATE -> getDateValueOrRandom(fieldValueList);
      case ValidTypeConstants.INT_TIME_MILLIS -> getTimeMillisValueOrRandom(fieldValueList);
      case ValidTypeConstants.LONG_TIME_MICROS -> getTimeMicrosValueOrRandom(fieldValueList);
      case ValidTypeConstants.LONG_TIMESTAMP_MILLIS -> getTimestampMillisValueOrRandom(fieldValueList);
      case ValidTypeConstants.LONG_TIMESTAMP_MICROS -> getTimestampMicrosValueOrRandom(fieldValueList);
      case ValidTypeConstants.LONG_LOCAL_TIMESTAMP_MILLIS -> getLocalTimestampMillisValueOrRandom(fieldValueList);
      case ValidTypeConstants.LONG_LOCAL_TIMESTAMP_MICROS -> getLocalTimestampMicrosValueOrRandom(fieldValueList);
      case ValidTypeConstants.UUID, ValidTypeConstants.STRING_UUID -> getUUIDValueOrRandom(fieldValueList);
      case ValidTypeConstants.BYTES_DECIMAL, ValidTypeConstants.FIXED_DECIMAL -> getDecimalValueOrRandom(fieldValueList, constraints);
      case ValidTypeConstants.INT_YEAR, ValidTypeConstants.INT_MONTH, ValidTypeConstants.INT_DAY -> getDateValueOrRandom(fieldType, fieldValueList);
      case ValidTypeConstants.INT_HOURS, ValidTypeConstants.INT_MINUTES, ValidTypeConstants.INT_SECONDS, ValidTypeConstants.INT_NANOS -> getTimeOfDayValueOrRandom(fieldType, fieldValueList);
      default -> fieldType;
    };
  }

  private BigInteger getIntegerValueOrRandom(final Integer valueLength, final List<String> fieldValueList, final Map<ConstraintTypeEnum, String> constraints) {
    final BigInteger value;

    if (!fieldValueList.isEmpty()) {
      value = new BigInteger(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      final Number minimum = calculateMinimum(valueLength, constraints);
      Number maximum;
      if (valueLength == 0) {
        maximum = 1000;
        final int num = rand.nextInt((Integer) maximum);
        value = new BigInteger(String.valueOf(num));
      } else {
        maximum = calculateMaximum(valueLength, constraints);

        if (constraints.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
          final int multipleOf = Integer.parseInt(constraints.get(ConstraintTypeEnum.MULTIPLE_OF));
          maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
          value = BigInteger.valueOf(RandomUtils.nextLong(minimum.longValue(), maximum.longValue()) * multipleOf);
        } else {
          value = BigInteger.valueOf(RandomUtils.nextLong(minimum.longValue(), maximum.longValue()));
        }
      }
    }
    return value;
  }

  private BigDecimal getDecimalValueOrRandom(final Integer valueLength, final List<String> fieldValueList, final Map<ConstraintTypeEnum, String> constraints) {
    final BigDecimal value;

    if (!fieldValueList.isEmpty()) {
      value = new BigDecimal(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      final Number minimum = calculateMinimum(valueLength - 1, constraints);

      Number maximum;
      maximum = calculateMaximum(valueLength - 1, constraints);

      if (constraints.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
        final int multipleOf = Integer.parseInt(constraints.get(ConstraintTypeEnum.MULTIPLE_OF));
        maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
        value = BigDecimal.valueOf(RandomUtils.nextDouble(minimum.doubleValue(), maximum.doubleValue()) * multipleOf);
      } else {
        value = new BigDecimal(BigInteger.valueOf(new Random().nextInt(100001)), 2);
      }
    }
    return value;
  }

  private static BigDecimal getDecimalValueOrRandom(
      final List<String> fieldValueList,
      final Map<ConstraintTypeEnum, String> constraints) {
    final BigDecimal result;
    final int scale;
    final int precision;

    if (Objects.nonNull(constraints.get(ConstraintTypeEnum.PRECISION))) {
      precision = Integer.parseInt(constraints.get(ConstraintTypeEnum.PRECISION));
      scale = Objects.nonNull(constraints.get(ConstraintTypeEnum.SCALE))
                  ? Integer.parseInt(constraints.get(ConstraintTypeEnum.SCALE)) : 0;

      if (precision <= 0) {
        throw new KLoadGenException("Decimal precision must be greater dan 0");
      }
      if (scale < 0 || scale > precision) {
        throw new KLoadGenException("Scale must be zero or a positive integer less than or equal to the precision");
      }

      if (fieldValueList.isEmpty()) {
        result = BigDecimal.valueOf(randomNumberWithLength(precision), scale);
      } else {
        result = new BigDecimal(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
      }

    } else {
      throw new KLoadGenException("Missing decimal precision");
    }
    return result;
  }

  private String getStringValueOrRandom(
      final Integer valueLength, final List<String> fieldValueList,
      final Map<ConstraintTypeEnum, String> constraints) {
    String value;
    if (!fieldValueList.isEmpty() && !StringUtils.isEmpty(fieldValueList.get(0))) {
      value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
    } else {
      if (constraints.containsKey(ConstraintTypeEnum.REGEX)) {
        final RgxGen rxGenerator = new RgxGen(constraints.get(ConstraintTypeEnum.REGEX));
        value = rxGenerator.generate();
        if (valueLength > 0 || constraints.containsKey(ConstraintTypeEnum.MAXIMUM_VALUE)) {
          value = value.substring(0, getMaxLength(valueLength, constraints.get(ConstraintTypeEnum.MAXIMUM_VALUE)));
        }
      } else {
        value = RandomStringUtils.randomAlphabetic(valueLength == 0 ? RandomUtils.nextInt(1, 20) : valueLength);
      }
    }
    return value;
  }

  private int getMaxLength(final Integer valueLength, final String maxValueStr) {
    int maxValue = Integer.parseInt(StringUtils.defaultIfEmpty(maxValueStr, "0"));
    if (valueLength > 0 && maxValue == 0) {
      maxValue = valueLength;
    }
    return maxValue;
  }

  private Object getTimestampValueOrRandom(final String type, final List<String> fieldValueList) {
    final LocalDateTime value;
    if (!fieldValueList.isEmpty()) {
      value = LocalDateTime.parse(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      value = LocalDateTime.now();
    }
    Object resultValue = value;
    if ("longTimestamp".equalsIgnoreCase(type)) {
      resultValue = value.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if ("stringTimestamp".equalsIgnoreCase(type)) {
      resultValue = value.toString();
    }
    return resultValue;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private UUID getUUIDValueOrRandom(final List<String> fieldValueList) {
    UUID value = UUID.randomUUID();
    if (!fieldValueList.isEmpty()) {
      value = UUID.fromString(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  private Boolean getBooleanValueOrRandom(final List<String> fieldValueList) {
    boolean value = RandomUtils.nextBoolean();
    if (!fieldValueList.isEmpty()) {
      value = Boolean.parseBoolean(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  private String getEnumValueOrRandom(final List<String> fieldValueList) {
    final String value;
    if (!fieldValueList.isEmpty()) {
      value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
    } else {
      throw new KLoadGenException("Wrong enums values, problem in the parsing process");
    }
    return value;
  }

  private Number calculateMaximum(final int valueLength, final Map<ConstraintTypeEnum, String> constraints) {
    final Number maximum;
    if (constraints.containsKey(ConstraintTypeEnum.MAXIMUM_VALUE)) {
      if (constraints.containsKey(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) {
        maximum = Long.parseLong(constraints.get(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) - 1L;
      } else {
        maximum = Long.parseLong(constraints.get(ConstraintTypeEnum.MAXIMUM_VALUE));
      }
    } else {
      maximum = new BigDecimal(StringUtils.rightPad("9", valueLength, '0'));
    }
    return maximum;
  }

  private Number calculateMinimum(final int valueLength, final Map<ConstraintTypeEnum, String> constraints) {
    final Number minimum;
    if (constraints.containsKey(ConstraintTypeEnum.MINIMUM_VALUE)) {
      if (constraints.containsKey(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) {
        minimum = Long.parseLong(constraints.get(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) - 1;
      } else {
        minimum = Long.parseLong(constraints.get(ConstraintTypeEnum.MINIMUM_VALUE));
      }
    } else {
      minimum = Long.parseLong(StringUtils.rightPad("1", valueLength, '0'));
    }
    return minimum;
  }

  private Integer getDateValueOrRandom(final String fieldType, final List<String> fieldValueList) {
    final LocalDate localDate = getDateValueOrRandom(fieldValueList);
    final int result;

    if ("int_year".equalsIgnoreCase(fieldType)) {
      result = localDate.getYear();
    } else if ("int_month".equalsIgnoreCase(fieldType)) {
      result = localDate.getMonthValue();
    } else if ("int_day".equalsIgnoreCase(fieldType)) {
      result = localDate.getDayOfMonth();
    } else {
      throw new KLoadGenException("FieldType wrong or not supported");
    }
    return result;
  }

  private static LocalDate getDateValueOrRandom(final List<String> fieldValueList) {
    final LocalDate resultDate;
    final long minDay = (int) LocalDate.of(1900, 1, 1).toEpochDay();
    final long maxDay = (int) LocalDate.of(2100, 1, 1).toEpochDay();
    final long randomDay = minDay + RandomUtils.nextLong(0, maxDay - minDay);
    if (fieldValueList.isEmpty()) {
      resultDate = LocalDate.ofEpochDay(randomDay);
    } else {
      resultDate = LocalDate.parse(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return resultDate;
  }

  private Integer getTimeOfDayValueOrRandom(final String fieldType, final List<String> fieldValueList) {
    final LocalTime localTime = getRandomLocalTime(fieldValueList);
    final int result;

    if ("int_hours".equalsIgnoreCase(fieldType)) {
      result = localTime.getHour();
    } else if ("int_minutes".equalsIgnoreCase(fieldType)) {
      result = localTime.getMinute();
    } else if ("int_seconds".equalsIgnoreCase(fieldType)) {
      result = localTime.getSecond();
    } else if ("int_nanos".equalsIgnoreCase(fieldType)) {
      result = localTime.getNano();
    } else {
      throw new KLoadGenException("FieldType wrong or not supported");
    }
    return result;
  }

  private static LocalTime getRandomLocalTime(final List<String> fieldValueList) {
    final long nanoMin = 0;
    final long nanoMax = 24L * 60L * 60L * 1_000_000_000L - 1L;
    final LocalTime result;
    if (fieldValueList.isEmpty()) {
      result = LocalTime.ofNanoOfDay(RandomUtils.nextLong(nanoMin, nanoMax));
    } else {
      result = getLocalTime(fieldValueList);
    }
    return result;
  }

  private static LocalTime getLocalTime(final List<String> fieldValueList) {
    final String fieldValue = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
    final Pattern pattern = Pattern.compile("([+|-]\\d{2}:\\d{2})");
    final Matcher matcher = pattern.matcher(fieldValue);
    final LocalTime result;
    if (matcher.find()) {
      final String offSet = matcher.group(1);
      final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_TIME;
      final LocalTime localtime = LocalTime.parse(fieldValue, formatter);

      final int hours = Integer.parseInt(offSet.substring(2, 3));
      final int minutes = Integer.parseInt(offSet.substring(5, 6));

      if (offSet.startsWith("-")) {
        result = localtime.minusHours(hours).minusMinutes(minutes);
      } else {
        result = localtime.plusHours(hours).plusMinutes(minutes);
      }
    } else {
      result = LocalTime.parse(fieldValue);
    }
    return result;
  }

  private static LocalTime getTimeMillisValueOrRandom(final List<String> fieldValueList) {
    return getRandomLocalTime(fieldValueList);
  }

  private static LocalTime getTimeMicrosValueOrRandom(final List<String> fieldValueList) {
    return getRandomLocalTime(fieldValueList);
  }

  private static LocalDateTime getRandomLocalDateTime(final List<String> fieldValueList) {
    final LocalDateTime value;
    final long minDay = LocalDateTime.of(1900, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
    final long maxDay = LocalDateTime.of(2100, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
    final long randomSeconds = minDay + RandomUtils.nextLong(0, maxDay - minDay);

    if (fieldValueList.isEmpty()) {
      value = LocalDateTime.ofEpochSecond(randomSeconds, RandomUtils.nextInt(0, 1_000_000_000 - 1), ZoneOffset.UTC);
    } else {
      value = LocalDateTime.parse(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  private static Instant getTimestampMillisValueOrRandom(final List<String> fieldValueList) {
    return getRandomLocalDateTime(fieldValueList).toInstant(ZoneOffset.UTC);
  }

  private static Instant getTimestampMicrosValueOrRandom(final List<String> fieldValueList) {
    return getRandomLocalDateTime(fieldValueList).toInstant(ZoneOffset.UTC);
  }

  private static LocalDateTime getLocalTimestampMillisValueOrRandom(final List<String> fieldValueList) {
    return getRandomLocalDateTime(fieldValueList);
  }

  private static LocalDateTime getLocalTimestampMicrosValueOrRandom(final List<String> fieldValueList) {
    return getRandomLocalDateTime(fieldValueList);
  }

  private static long randomNumberWithLength(final int n) {
    final long min = (long) Math.pow(10, n - 1.0);
    return RandomUtils.nextLong(min, min * 10);
  }

}
