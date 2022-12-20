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
import java.util.List;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.jmeter.threads.JMeterContextService;

public class ValueUtils {

  private ValueUtils() {

  }

  public static List<String> replaceValuesContext(final List<String> fieldValuesList) {
    final List<String> parameterList = new ArrayList<>(fieldValuesList);

    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}")
                                     ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);
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
    switch (type) {
      case ValidTypeConstants.INT:
        castValue = Integer.valueOf(value);
        break;
      case ValidTypeConstants.DOUBLE:
        castValue = Double.valueOf(value);
        break;
      case ValidTypeConstants.LONG:
        castValue = Long.valueOf(value);
        break;
      case ValidTypeConstants.FLOAT:
        castValue = Float.valueOf(value);
        break;
      case ValidTypeConstants.SHORT:
        castValue = Short.valueOf(value);
        break;
      case ValidTypeConstants.BOOLEAN:
        castValue = Boolean.valueOf(value);
        break;
      case ValidTypeConstants.LONG_TIMESTAMP:
        castValue = LocalDateTime.parse(value.trim()).toInstant(ZoneOffset.UTC).toEpochMilli();
        break;
      case ValidTypeConstants.STRING_TIMESTAMP:
        castValue = LocalDateTime.parse(value.trim()).toString();
        break;
      case ValidTypeConstants.INT_DATE:
        castValue = LocalDate.parse(value.trim());
        break;
      case ValidTypeConstants.INT_TIME_MILLIS:
      case ValidTypeConstants.LONG_TIME_MICROS:
      case ValidTypeConstants.LONG_LOCAL_TIMESTAMP_MILLIS:
        castValue = LocalTime.parse(value.trim());
        break;
      case ValidTypeConstants.LONG_TIMESTAMP_MILLIS:
      case ValidTypeConstants.LONG_TIMESTAMP_MICROS:
        castValue = LocalDateTime.parse(value.trim()).toInstant(ZoneOffset.UTC);
        break;
      case ValidTypeConstants.TIMESTAMP:
      case ValidTypeConstants.LONG_LOCAL_TIMESTAMP_MICROS:
        castValue = LocalDateTime.parse(value.trim());
        break;
      case ValidTypeConstants.UUID:
      case ValidTypeConstants.STRING_UUID:
        castValue = UUID.fromString(value);
        break;
      case ValidTypeConstants.BYTES_DECIMAL:
      case ValidTypeConstants.FIXED_DECIMAL:
        castValue = new BigDecimal(value);
        break;
      default:
        castValue = value;
        break;
    }
    return castValue;
  }

  public static String getValidTypeFromSchema(final Schema schema) {
    return schema.getLogicalType() == null
        ? schema.getType().getName()
        : String.format("%s_%s", schema.getType().getName(), schema.getLogicalType().getName());
  }
}
