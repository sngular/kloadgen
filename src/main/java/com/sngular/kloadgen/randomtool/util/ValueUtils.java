/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.jmeter.threads.JMeterContextService;

public class ValueUtils {

  private ValueUtils() {

  }

  public static List<String> replaceValuesContext(final List<String> fieldValuesList) {
    final List<String> parameterList;
    if (CollectionUtils.isNotEmpty(fieldValuesList)) {
      parameterList = new ArrayList<>(fieldValuesList);
      parameterList.replaceAll(fieldValue ->
                                   fieldValue.matches("\\$\\{\\w*}")
                                       ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);
    } else {
      parameterList = Collections.emptyList();
    }

    return parameterList;
  }

  public static String replaceValueContext(final String fieldValue) {
    String value = fieldValue;
    if (fieldValue.matches("\\$\\{\\w*}")) {
      value = JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1));
    }
    return value;
  }

  public static Object castValue(final Object valueObject, final String type) {
    final Object castValue;
    final String value = valueObject.toString();
    castValue = switch (type) {
      case ValidTypeConstants.INT -> Integer.valueOf(value);
      case ValidTypeConstants.DOUBLE -> Double.valueOf(value);
      case ValidTypeConstants.LONG -> Long.valueOf(value);
      case ValidTypeConstants.FLOAT -> Float.valueOf(value);
      case ValidTypeConstants.SHORT -> Short.valueOf(value);
      case ValidTypeConstants.BOOLEAN -> Boolean.valueOf(value);
      case ValidTypeConstants.LONG_TIMESTAMP -> LocalDateTime.parse(value.trim()).toInstant(ZoneOffset.UTC).toEpochMilli();
      case ValidTypeConstants.STRING_TIMESTAMP -> LocalDateTime.parse(value.trim()).toString();
      case ValidTypeConstants.INT_DATE -> LocalDate.parse(value.trim());
      case ValidTypeConstants.INT_TIME_MILLIS, ValidTypeConstants.LONG_TIME_MICROS, ValidTypeConstants.LONG_LOCAL_TIMESTAMP_MILLIS -> LocalTime.parse(value.trim());
      case ValidTypeConstants.LONG_TIMESTAMP_MILLIS, ValidTypeConstants.LONG_TIMESTAMP_MICROS -> LocalDateTime.parse(value.trim()).toInstant(ZoneOffset.UTC);
      case ValidTypeConstants.TIMESTAMP, ValidTypeConstants.LONG_LOCAL_TIMESTAMP_MICROS -> LocalDateTime.parse(value.trim());
      case ValidTypeConstants.UUID, ValidTypeConstants.STRING_UUID -> UUID.fromString(value);
      case ValidTypeConstants.BYTES_DECIMAL, ValidTypeConstants.FIXED_DECIMAL -> new BigDecimal(value);
      default -> value;
    };
    return castValue;
  }

  public static String getValidTypeFromSchema(final Schema schema) {
    return schema.getLogicalType() == null
        ? schema.getType().getName()
        : String.format("%s_%s", schema.getType().getName(), schema.getLogicalType().getName());
  }
}
