/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.util;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap.SimpleEntry;
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

  protected static Object generateRandomMap(String fieldType, Integer mapSize, List<String> fieldValuesList, Integer arraySize) {
    Object value;
    switch (fieldType) {
      case "int-map":
        value = generateIntMap(mapSize, fieldValuesList);
        break;
      case "long-map":
        value = generateLongMap(mapSize, fieldValuesList);
        break;
      case "double-map":
        value = generateDoubleMap(mapSize, fieldValuesList);
        break;
      case "short-map":
        value = generateShortMap(mapSize, fieldValuesList);
        break;
      case "string-map":
        value = generateStringMap(mapSize, fieldValuesList);
        break;
      case "uuid-map":
        value = generateUuidMap(mapSize, fieldValuesList);
        break;
      case "boolean-map":
        value = generateBooleanMap(mapSize, fieldValuesList);
        break;
      default:
        value = fieldType;
        break;
    }
    if (fieldType.endsWith("array")) {
      value = generateMapArray(fieldType, mapSize, fieldValuesList, arraySize);
    }
    return value;
  }

  protected static Object generateMapArray(String type, Integer valueLength, List<String> fieldValuesList, Integer arraySize) {
    List<Map<String, Object>> generatedMapArray = new ArrayList<>(valueLength);
    for (int i = 0; i < arraySize; i++) {
      generatedMapArray.add((Map)generateRandomMap(type.substring(0, type.length() - 6), valueLength, fieldValuesList, arraySize));
    }
    return generatedMapArray;
  }

  protected static Object generateRandom(String fieldType, Integer valueLength, List<String> fieldValuesList) {
    Object value;
    switch (fieldType) {
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
      case "float":
        value = getFloatValueOrRandom(valueLength, fieldValuesList);
        break;
      case "bytes":
		    value = getByteRandom(valueLength);
		    break;
      case "timestamp":
      case "longTimestamp":
      case "stringTimestamp":
        value = getTimestampValueOrRandom(fieldType, fieldValuesList);
        break;
      case "uuid":
        value = getUUIDValueOrRandom(fieldValuesList);
        break;
      case "boolean":
        value = getBooleanValueOrRandom(fieldValuesList);
        break;
      default:
        value = generateRandomArray(fieldType, valueLength, fieldValuesList, 0);
        break;
    }
    return value;
  }

  public static Object generateRandomArray(String fieldType, int valueLength, List<String> fieldValuesList, int arraySize) {
    Object value;
    switch(fieldType) {
      case "int-array":
        value = generateIntArray(arraySize, valueLength, fieldValuesList);
        break;
      case "long-array":
        value = generateLongArray(arraySize, valueLength, fieldValuesList);
        break;
      case "double-array":
        value = generateDoubleArray(arraySize, valueLength, fieldValuesList);
        break;
      case "float-array":
        value = generateFloatArray(arraySize, valueLength, fieldValuesList);
        break;
      case "short-array":
        value = generateShortArray(arraySize, valueLength, fieldValuesList);
        break;
      case "string-array":
        value = generateStringArray(arraySize, valueLength, fieldValuesList);
        break;
      case "uuid-array":
        value = generateUuidArray(arraySize, fieldValuesList);
        break;
      case "boolean-array":
        value = generateBooleanArray(arraySize, fieldValuesList);
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

  private static List<Integer> generateIntArray(int arraySize, Integer valueLength, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Integer> intArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      intArray.add(getIntValueOrRandom(valueLength, fieldValueList));
    }
    return intArray;
  }

  private static List<Long> generateLongArray(int arraySize, Integer valueLength, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Long> longArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      longArray.add(getLongValueOrRandom(valueLength, fieldValueList));
    }
    return longArray;
  }

  private static List<Double> generateDoubleArray(int arraySize, Integer valueLength, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Double> doubleArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      doubleArray.add(getDoubleValueOrRandom(valueLength, fieldValueList));
    }
    return doubleArray;
  }

  private static List<Float> generateFloatArray(int arraySize, Integer valueLength, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Float> floatArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      floatArray.add(getFloatValueOrRandom(valueLength, fieldValueList));
    }
    return floatArray;
  }

  private static List<Short> generateShortArray(int arraySize, Integer valueLength, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Short> shortArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      shortArray.add(getShortValueOrRandom(valueLength, fieldValueList));
    }
    return shortArray;
  }

  private static List<String> generateStringArray(int arraySize, Integer valueLength, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<String> stringArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      stringArray.add(getStringValueOrRandom(valueLength, fieldValueList));
    }
    return stringArray;
  }

  private static List<UUID> generateUuidArray(int arraySize, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<UUID> uuidArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      uuidArray.add(getUUIDValueOrRandom(fieldValueList));
    }
    return uuidArray;
  }

  private static List<Boolean> generateBooleanArray(int arraySize, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Boolean> booleanArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      booleanArray.add(getBooleanValueOrRandom(fieldValueList));
    }
    return booleanArray;
  }

  private static Map<String, Integer> generateIntMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Integer> intMap = new HashMap<>();
    while (intMap.size() < size) {
      Map.Entry<String, Integer> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], Integer.parseInt(tempValue[1]));
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getIntValueOrRandom(0, Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getIntValueOrRandom(0, Collections.emptyList()));
      }
      intMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return intMap;
  }

  private static Map<String, Long> generateLongMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Long> longMap = new HashMap<>();
    while (longMap.size() < size) {
      Map.Entry<String, Long> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], Long.parseLong(tempValue[1]));
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getLongValueOrRandom(0, Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getLongValueOrRandom(0, Collections.emptyList()));
      }
      longMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return longMap;
  }

  private static Map<String, Double> generateDoubleMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Double> doubleMap = new HashMap<>();
    while (doubleMap.size() < size) {
      Map.Entry<String, Double> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], Double.parseDouble(tempValue[1]));
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getDoubleValueOrRandom(0, Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getDoubleValueOrRandom(0, Collections.emptyList()));
      }
      doubleMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return doubleMap;
  }

  private static Map<String, Short> generateShortMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Short> shortMap = new HashMap<>();
    while (shortMap.size() < size) {
      Map.Entry<String, Short> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], Short.parseShort(tempValue[1]));
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getShortValueOrRandom(0, Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getShortValueOrRandom(0, Collections.emptyList()));
      }
      shortMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return shortMap;
  }

  private static Map<String, String> generateStringMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, String> stringMap = new HashMap<>();
    while (stringMap.size() < size) {
      Map.Entry<String, String> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], tempValue[1]);
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getStringValueOrRandom(0, Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getStringValueOrRandom(0, Collections.emptyList()));
      }
      stringMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return stringMap;
  }

  private static Map<String, UUID> generateUuidMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, UUID> uuidMap = new HashMap<>();
    while (uuidMap.size() < size) {
      Map.Entry<String, UUID> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], UUID.fromString(tempValue[1]));
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getUUIDValueOrRandom(Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getUUIDValueOrRandom(Collections.emptyList()));
      }
      uuidMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return uuidMap;
  }

  private static Map<String, Boolean> generateBooleanMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Boolean> booleanMap = new HashMap<>();
    for (int i=0; i<size; i++) {
      Map.Entry<String, Boolean> mapValue;
      if (fieldValueList.size() > 0) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          mapValue = new SimpleEntry<>(tempValue[0], Boolean.parseBoolean(tempValue[1]));
        } else {
          mapValue = new SimpleEntry<>(tempValue[0], getBooleanValueOrRandom(Collections.emptyList()));
        }
      } else {
        mapValue = new SimpleEntry<>(getStringValueOrRandom(0, Collections.emptyList()),
            getBooleanValueOrRandom(Collections.emptyList()));
      }
      booleanMap.put(mapValue.getKey(), mapValue.getValue());
    }
    return booleanMap;
  }

  private static String[] getMapEntryValue(List<String> fieldValueList) {
    return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
  }

  private static Integer getIntValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    int value;
    if (fieldValuesList.size() > 0) {
      value = Integer.parseInt(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    } else {
      value = RandomUtils.nextInt(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Long getLongValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    long value;
    if (fieldValuesList.size() > 0) {
      value = Long.parseLong(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    } else {
      value = RandomUtils.nextLong(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Double getDoubleValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    double value;
    if (fieldValuesList.size() > 0) {
      value = Double.parseDouble(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    } else {
      value = RandomUtils.nextDouble(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
    }
    return value;
  }

  private static Float getFloatValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    float value;
    if (fieldValuesList.size() > 0) {
      value = Float.parseFloat(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    } else {
      value = RandomUtils.nextFloat(1, 9 * (int) Math.pow(10, calculateSize(valueLength)));
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

  private static String getStringValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    String value;
    if (fieldValuesList.size() > 0) {
      value = fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim();
    } else {
      value = RandomStringUtils.randomAlphabetic(valueLength == 0 ? RandomUtils.nextInt(1,20): valueLength);
    }
    return value;
  }

  private static Short getShortValueOrRandom(Integer valueLength, List<String> fieldValuesList) {
    short value;
    if (fieldValuesList.size() > 0) {
      value = Short.parseShort(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
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
      value = LocalDateTime.parse(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
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

  private static UUID getUUIDValueOrRandom(List<String> fieldValuesList) {
    UUID value = UUID.randomUUID();
    if (fieldValuesList.size() > 0) {
      value = UUID.fromString(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    }
    return value;
  }

  private static Boolean getBooleanValueOrRandom(List<String> fieldValuesList) {
    boolean value = RandomUtils.nextBoolean();
    if (fieldValuesList.size() > 0) {
      value = Boolean.parseBoolean(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    }
    return value;
  }

  public static Object generateSeq(String fieldName, String fieldType, List<String> fieldValuesList, Map<String, Object> context) {

    return RandomTool.castValue(
        context.compute(fieldName, (fieldNameMap,
            seqObject) -> seqObject == null ? (fieldValuesList.isEmpty() ? 1L : Long.parseLong(fieldValuesList.get(0)))
            : ((Long) seqObject) + 1),
        fieldType);
  }
}
