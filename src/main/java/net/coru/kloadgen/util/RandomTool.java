package net.coru.kloadgen.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class RandomTool {

  public static final Set<String> VALID_TYPES = SetUtils.hashSet("string", "int", "long", "timestamp", "stringTimestamp", "short", "double", "longTimestamp", "uuid", "array");

  private RandomTool() {
  }

  public static Object generateRandom(String valueExpression, Integer valueLength, List<String> fieldValuesList) {
    Object value;
    switch (valueExpression) {
      case "string":
        value = getStringValueOrRandom(valueLength, fieldValuesList);
        break;
      case "int":
        value = getIntValueOrRandom(valueLength, fieldValuesList);
        break;
      case "long":
        value = getLongValueOrRandom(valueLength, fieldValuesList);
        break;
      case "short":
        value = getShortValueOrRandom(valueLength, fieldValuesList);
        break;
      case "double":
        value = getDoubleValueOrRandom(valueLength, fieldValuesList);
        break;
      case "timestamp":
      case "longTimestamp":
      case "stringTimestamp":
        value = getTimestampValueOrRandom(valueExpression, fieldValuesList);
        break;
      case "uuid":
        value = UUID.randomUUID().toString();
        break;
      case "boolean":
        value = RandomUtils.nextBoolean();
        break;
      case "int-array":
        value = generateIntArray(valueLength, fieldValuesList);
        break;
      case "long-array":
        value = generateLongArray(valueLength, fieldValuesList);
        break;
      case "double-array":
        value = generateDoubleArray(valueLength, fieldValuesList);
        break;
      case "short-array":
        value = generateShortArray(valueLength, fieldValuesList);
        break;
      case "string-array":
        value = generateStringArray(valueLength, fieldValuesList);
        break;
      default:
        value = valueExpression;
        break;
    }
    return value;
  }
  public static Object generateRandom(String valueExpression, Integer valueLength, List<String> fieldValuesList, Field field) {

    Object value = generateRandom(valueExpression, valueLength, fieldValuesList);
    if ("ENUM".equalsIgnoreCase(field.schema().getType().getName())) {
       if ("ENUM".equalsIgnoreCase(valueExpression)) {
         List<String> enumValueList= field.schema().getEnumSymbols();
         value = new GenericData.EnumSymbol(field.schema(), enumValueList.get(RandomUtils.nextInt(0, enumValueList.size())));
       } else {
         value = new GenericData.EnumSymbol(field.schema(), valueExpression);
       }
    } else if (valueExpression.equalsIgnoreCase(value.toString())) {
      switch (field.schema().getType().getName().toUpperCase()) {
        case "INT":
          value = Integer.valueOf(valueExpression);
          break;
        case "DOUBLE":
          value = Double.valueOf(valueExpression);
          break;
        case "LONG":
          value = Long.valueOf(valueExpression);
          break;
        case "SHORT":
          value = Short.valueOf(valueExpression);
          break;
        case "UNION":
          value = ("null".equalsIgnoreCase(value.toString())) ? null : valueExpression;
          break;
        default:
          value = valueExpression;
          break;
      }
    }
    return value;
  }

  private static List<Integer> generateIntArray(Integer valueLength, List<String> fieldValueList) {
    int size = RandomUtils.nextInt(1,5);
    List<Integer> intArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      intArray.add(getIntValueOrRandom(valueLength, fieldValueList));
    }
    return intArray;
  }

  private static List<Long> generateLongArray(Integer valueLength, List<String> fieldValueList) {
    int size = RandomUtils.nextInt(1,5);
    List<Long> longArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      longArray.add(getLongValueOrRandom(valueLength, fieldValueList));
    }
    return longArray;
  }

  private static List<Double> generateDoubleArray(Integer valueLength, List<String> fieldValueList) {
    int size = RandomUtils.nextInt(1,5);
    List<Double> doubleArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      doubleArray.add(getDoubleValueOrRandom(valueLength, fieldValueList));
    }
    return doubleArray;
  }

  private static List<Short> generateShortArray(Integer valueLength, List<String> fieldValueList) {
    int size = RandomUtils.nextInt(1,5);
    List<Short> shortArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      shortArray.add(getShortValueOrRandom(valueLength, fieldValueList));
    }
    return shortArray;
  }

  private static List<String> generateStringArray(Integer valueLength, List<String> fieldValueList) {
    int size = RandomUtils.nextInt(1,5);
    List<String> stringArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      stringArray.add(getStringValueOrRandom(valueLength, fieldValueList));
    }
    return stringArray;
  }

  private static Integer getIntValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    int value;
    if (fieldValuesList.size() >0 ) {
      value = Integer.parseInt(fieldValuesList.get(RandomUtils.nextInt(0,fieldValuesList.size())));
    } else {
      value = RandomUtils.nextInt(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Long getLongValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    long value;
    if (fieldValuesList.size() > 0) {
      value = Long.parseLong(fieldValuesList.get(RandomUtils.nextInt(0,fieldValuesList.size())));
    } else {
      value = RandomUtils.nextLong(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Double getDoubleValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    double value;
    if (fieldValuesList.size() > 0) {
      value = Double.parseDouble(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())));
    } else {
      value = RandomUtils.nextDouble(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static String getStringValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    String value;
    if (fieldValuesList.size() > 0) {
      value = fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size()));
    } else {
      value = RandomStringUtils.randomAlphabetic(valueLength);
    }
    return value;
  }

  private static Short getShortValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    short value;
    if (fieldValuesList.size() > 0) {
      value = Short.parseShort(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())));
    } else {
      if (valueLength < 5 ) {
        value = (short) RandomUtils.nextInt(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
      } else {
        value = (short) RandomUtils.nextInt(1, 32767);
      }
    }
    return value;
  }

  private static int calculateSize(int valueLength) {
    return valueLength > 0 ? valueLength -1 : 0;
  }

  private static Object getTimestampValueOrRandom(String type, List<String> fieldValuesList) {
    LocalDateTime value;
    if (fieldValuesList.size() > 0) {
      value = LocalDateTime.parse(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())));
    } else {
      value = LocalDateTime.now();
    }
    if ("longTimestamp".equalsIgnoreCase(type)) {
      return value.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if ("stringTimestamp".equalsIgnoreCase(type)) {
      return value.toString();
    }
    return value;
  }
}
