/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.util;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class RandomTool {

  public static final Set<String> VALID_TYPES = SetUtils.hashSet("map", "enum", "string", "int", "long", "timestamp", "stringTimestamp", "short", "double", "longTimestamp", "uuid", "array", "boolean", "bytes");

  private RandomTool() {}

  protected static Object generateRandomMap(String fieldType, Integer valueLength, List<String> fieldValueList, Integer mapSize) {
    Object value;
    switch (fieldType) {
      case "int-map":
        value = generateIntMap(mapSize, fieldValueList, valueLength);
        break;
      case "long-map":
        value = generateLongMap(mapSize, fieldValueList, valueLength);
        break;
      case "double-map":
        value = generateDoubleMap(mapSize, fieldValueList, valueLength);
        break;
      case "short-map":
        value = generateShortMap(mapSize, fieldValueList, valueLength);
        break;
      case "string-map":
        value = generateStringMap(mapSize, fieldValueList, valueLength);
        break;
      case "uuid-map":
        value = generateUuidMap(mapSize, fieldValueList);
        break;
      case "boolean-map":
        value = generateBooleanMap(mapSize, fieldValueList);
        break;
      default:
        value = fieldType;
        break;
    }
    if (fieldType.endsWith("array")) {
      value = generateMapArray(fieldType, mapSize, fieldValueList, mapSize);
    }
    return value;
  }

  protected static Object generateMapArray(String type, Integer valueLength, List<String> fieldValueList, Integer arraySize) {
    List<Map<String, Object>> generatedMapArray = new ArrayList<>(valueLength);
    for (int i = 0; i < arraySize; i++) {
      generatedMapArray.add((Map)generateRandomMap(type.substring(0, type.length() - 6), valueLength, fieldValueList, arraySize));
    }
    return generatedMapArray;
  }

  protected static Object generateRandomList(String fieldType, int arraySize, int valueLength, List<String> fieldValueList) {
    Object value;
    switch (fieldType) {
      case "int-array":
        value = generateIntArray(valueLength, fieldValueList, arraySize);
        break;
      case "long-array":
        value = generateLongArray(valueLength, fieldValueList, arraySize);
        break;
      case "double-array":
        value = generateDoubleArray(valueLength, fieldValueList, arraySize);
        break;
      case "short-array":
        value = generateShortArray(valueLength, fieldValueList, arraySize);
        break;
      case "string-array":
        value = generateStringArray(valueLength, fieldValueList, arraySize);
        break;
      case "uuid-array":
        value = generateUuidArray(fieldValueList, arraySize);
        break;
      default:
        value = generateBooleanArray(fieldValueList, arraySize);
        break;
    }

    return value;
  }

  protected static Object generateRandom(String fieldType, Integer valueLength, List<String> fieldValueList) {
    Object value;
    switch (fieldType) {
      case "string":
        value = getStringValueOrRandom(valueLength, fieldValueList);
        break;
      case "int":
        value = getIntValueOrRandom(valueLength, fieldValueList);
        break;
      case "long":
        value = getLongValueOrRandom(valueLength, fieldValueList);
        break;
      case "short":
        value = getShortValueOrRandom(valueLength, fieldValueList);
        break;
      case "double":
        value = getDoubleValueOrRandom(valueLength, fieldValueList);
        break;
      case "bytes":
		    value = getByteRandom(valueLength);
		break;
      case "timestamp":
      case "longTimestamp":
      case "stringTimestamp":
        value = getTimestampValueOrRandom(fieldType, fieldValueList);
        break;
      case "uuid":
        value = getUUIDValueOrRandom(fieldValueList);
        break;
      case "boolean":
        value = getBooleanValueOrRandom(fieldValueList);
        break;
      default:
        value = fieldType;
        break;
    }
    return value;
  }


  protected static Object castValue(Object value, String type) {
    Object castValue;
    switch(type) {
      case "int":
        castValue = Integer.valueOf(value.toString());
        break;
      case "double":
        castValue = Double.valueOf(value.toString());
        break;
      case "long":
        castValue = Long.valueOf(value.toString());
        break;
      case "boolean":
        castValue = Boolean.valueOf(value.toString());
        break;
      default:
        castValue = value.toString();
        break;
    }
    return castValue;
  }

  private static List<Integer> generateIntArray(Integer valueLength, List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Integer> intArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      intArray.add(getIntValueOrRandom(valueLength, fieldValueList));
    }
    return intArray;
  }

  private static List<Long> generateLongArray(Integer valueLength, List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Long> longArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      longArray.add(getLongValueOrRandom(valueLength, fieldValueList));
    }
    return longArray;
  }

  private static List<Double> generateDoubleArray(Integer valueLength, List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Double> doubleArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      doubleArray.add(getDoubleValueOrRandom(valueLength, fieldValueList));
    }
    return doubleArray;
  }

  private static List<Short> generateShortArray(Integer valueLength, List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Short> shortArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      shortArray.add(getShortValueOrRandom(valueLength, fieldValueList));
    }
    return shortArray;
  }

  private static List<String> generateStringArray(Integer valueLength, List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<String> stringArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      stringArray.add(getStringValueOrRandom(valueLength, fieldValueList));
    }
    return stringArray;
  }

  private static List<UUID> generateUuidArray(List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<UUID> uuidArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      uuidArray.add(getUUIDValueOrRandom(fieldValueList));
    }
    return uuidArray;
  }

  private static List<Boolean> generateBooleanArray(List<String> fieldValueList, int arraySize) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Boolean> booleanArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      booleanArray.add(getBooleanValueOrRandom(fieldValueList));
    }
    return booleanArray;
  }

  private static Map<String, Integer> generateIntMap(int mapSize, List<String> fieldValueList, int valueLength) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Integer> intMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (intMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          intMap.put(tempValue[0], Integer.parseInt(tempValue[1]));
        } else {
          intMap.put(tempValue[0], getIntValueOrRandom(valueLength, Collections.emptyList()));
        }
      }
    }
    if (intMap.size() != mapSize) {
      for (int i = 0; i < Math.abs(intMap.size() - mapSize); i++) {
        intMap.put(getStringValueOrRandom(valueLength, Collections.emptyList()),
            getIntValueOrRandom(valueLength, Collections.emptyList()));
      }
    }
    return intMap;
  }

  private static Map<String, Long> generateLongMap(int mapSize, List<String> fieldValueList, int valueLength) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Long> longMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (longMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          longMap.put(tempValue[0], Long.parseLong(tempValue[1]));
        } else {
          longMap.put(tempValue[0], getLongValueOrRandom(valueLength, Collections.emptyList()));
        }
      }
    }
    if (longMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(longMap.size() - mapSize); i++) {
        longMap.put(getStringValueOrRandom(valueLength, Collections.emptyList()),
            getLongValueOrRandom(valueLength, Collections.emptyList()));
      }
    }
    return longMap;
  }

  private static Map<String, Double> generateDoubleMap(int mapSize, List<String> fieldValueList, int valueLength) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Double> doubleMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (doubleMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          doubleMap.put(tempValue[0], Double.parseDouble(tempValue[1]));
        } else {
          doubleMap.put(tempValue[0], getDoubleValueOrRandom(valueLength, Collections.emptyList()));
        }
      }
    }
    if (doubleMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(doubleMap.size() - mapSize); i++) {
        doubleMap.put(getStringValueOrRandom(valueLength, Collections.emptyList()),
            getDoubleValueOrRandom(valueLength, Collections.emptyList()));
      }
    }
    return doubleMap;
  }

  private static Map<String, Short> generateShortMap(int mapSize, List<String> fieldValueList, int valueLength) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Short> shortMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (shortMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          shortMap.put(tempValue[0], Short.parseShort(tempValue[1]));
        } else {
          shortMap.put(tempValue[0], getShortValueOrRandom(valueLength, Collections.emptyList()));
        }
      }
    }
    if (shortMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(shortMap.size() - mapSize); i++) {
        shortMap.put(getStringValueOrRandom(valueLength, Collections.emptyList()),
            getShortValueOrRandom(valueLength, Collections.emptyList()));
      }
    }
    return shortMap;
  }

  private static Map<String, String> generateStringMap(int mapSize, List<String> fieldValueList, int valueLength) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, String> stringMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (stringMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          stringMap.put(tempValue[0], tempValue[1]);
        } else {
          stringMap.put(tempValue[0], getStringValueOrRandom(valueLength, Collections.emptyList()));
        }
      }
    }
    if (stringMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(stringMap.size() - mapSize); i++) {
        stringMap.put(getStringValueOrRandom(valueLength, Collections.emptyList()),
            getStringValueOrRandom(valueLength, Collections.emptyList()));
      }
    }
    return stringMap;
  }

  private static Map<String, UUID> generateUuidMap(int mapSize, List<String> fieldValueList) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, UUID> uuidMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
    while (uuidMap.size() <  Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          uuidMap.put(tempValue[0], UUID.fromString(tempValue[1]));
        } else {
          uuidMap.put(tempValue[0], getUUIDValueOrRandom(Collections.emptyList()));
        }
      }
    }
    if (uuidMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(uuidMap.size() - mapSize); i++) {
        uuidMap.put(getStringValueOrRandom(0, Collections.emptyList()),
            getUUIDValueOrRandom(Collections.emptyList()));
      }
    }
    return uuidMap;
  }

  private static Map<String, Boolean> generateBooleanMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Boolean> booleanMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (booleanMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          booleanMap.put(tempValue[0], Boolean.parseBoolean(tempValue[1]));
        } else {
          booleanMap.put(tempValue[0], getBooleanValueOrRandom(Collections.emptyList()));
        }
      }
    }
    if (booleanMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(booleanMap.size() - mapSize); i++) {
        booleanMap.put(getStringValueOrRandom(0, Collections.emptyList()),
            getBooleanValueOrRandom(Collections.emptyList()));
      }
    }
    return booleanMap;
  }

  private static String[] getMapEntryValue(List<String> fieldValueList) {
    return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
  }

  private static Integer getIntValueOrRandom(Integer valueLength, List<String> fieldValueList) {
    int value;
    if (!fieldValueList.isEmpty()) {
      value = Integer.parseInt(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      value = RandomUtils.nextInt(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Long getLongValueOrRandom(Integer valueLength, List<String> fieldValueList) {
    long value;
    if (!fieldValueList.isEmpty()) {
      value = Long.parseLong(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      value = RandomUtils.nextLong(1, 9 * (long) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Double getDoubleValueOrRandom(Integer valueLength, List<String> fieldValueList) {
    double value;
    if (!fieldValueList.isEmpty()) {
      value = Double.parseDouble(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      value = RandomUtils.nextDouble(1, 9 * Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static ByteBuffer getByteRandom(Integer valueLength) {
    ByteBuffer value;
    if (valueLength == 0) {
      value =  ByteBuffer.wrap(RandomUtils.nextBytes(4));
    } else {
      value =  ByteBuffer.wrap(RandomUtils.nextBytes(valueLength));
    }
    return value;
  }

  private static String getStringValueOrRandom(Integer valueLength, List<String> fieldValueList) {
    String value;
    if (!fieldValueList.isEmpty()) {
      value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
    } else {
      value = RandomStringUtils.randomAlphabetic(valueLength == 0 ? RandomUtils.nextInt(1,20): valueLength);
    }
    return value;
  }

  private static Short getShortValueOrRandom(Integer valueLength, List<String> fieldValueList) {
    short value;
    if (!fieldValueList.isEmpty()) {
      value = Short.parseShort(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
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

  private static Object getTimestampValueOrRandom(String type, List<String> fieldValueList) {
    LocalDateTime value;
    if (!fieldValueList.isEmpty()) {
      value = LocalDateTime.parse(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
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

  private static UUID getUUIDValueOrRandom(List<String> fieldValueList) {
    UUID value = UUID.randomUUID();
    if (!fieldValueList.isEmpty()) {
      value = UUID.fromString(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  private static Boolean getBooleanValueOrRandom(List<String> fieldValueList) {
    boolean value = RandomUtils.nextBoolean();
    if (!fieldValueList.isEmpty()) {
      value = Boolean.parseBoolean(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  public static Object generateSeq(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {

    return RandomTool.castValue(
        context.compute(fieldName, (fieldNameMap,
            seqObject) -> seqObject == null ? (fieldValueList.isEmpty() ? 1L : Long.parseLong(fieldValueList.get(0)))
            : ((Long) seqObject) + 1),
        fieldType);
  }
}
