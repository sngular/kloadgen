package net.coru.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.ValidTypes;
import org.apache.commons.lang3.RandomUtils;

public class RandomMap {

  private final RandomObject randomObject;

  public RandomMap() {
    randomObject = new RandomObject();
  }

  public Object generateMap(String fieldType, Integer mapSize, List<String> fieldValueList, Integer arraySize,
      Map<ConstraintTypeEnum, String> constrains) {
    Object value;
    switch (fieldType) {
      case ValidTypes.INT_MAP:
        value = generateIntMap(mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.LONG_MAP:
        value = generateLongMap(mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.DOUBLE_MAP:
        value = generateDoubleMap(mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.SHORT_MAP:
        value = generateShortMap(mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.FLOAT_MAP:
        value = generateFloatMap(mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.STRING_MAP:
        value = generateStringMap(mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.UUID_MAP:
        value = generateUuidMap(mapSize, fieldValueList);
        break;
      case ValidTypes.BOOLEAN_MAP:
        value = generateBooleanMap(mapSize, fieldValueList);
        break;
      default:
        value = fieldType;
        break;
    }
    if (fieldType.endsWith("array")) {
      value = generateRandomMapArray(fieldType, mapSize, fieldValueList, arraySize, constrains);
    }
    return value;
  }

  private Object generateRandomMapArray(String type, Integer valueLength, List<String> fieldValueList, Integer arraySize,
      Map<ConstraintTypeEnum, String> constrains) {
    List<Map<String, Object>> generatedMapArray = new ArrayList<>(valueLength);
    for (int i = 0; i < arraySize; i++) {
      generatedMapArray.add(
          (Map<String, Object>)generateMap(type.substring(0, type.length() - 6), valueLength, fieldValueList, arraySize, constrains)
      );
    }
    return generatedMapArray;
  }

  private Map<String, Integer> generateIntMap(Integer mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Integer> intMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (intMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          intMap.put(tempValue[0], Integer.parseInt(tempValue[1]));
        } else {
          intMap.put(
              tempValue[0],
              (Integer) randomObject.generateRandom(ValidTypes.INT, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }
    if (intMap.size() != mapSize) {
      for (int i = 0; i < Math.abs(intMap.size() - mapSize); i++) {
        intMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            (Integer) randomObject.generateRandom(ValidTypes.INT, valueLength, Collections.emptyList(), constrains)
        );
      }
    }
    return intMap;
  }

  private Map<String, Long> generateLongMap(int mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Long> longMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (longMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          longMap.put(tempValue[0], Long.parseLong(tempValue[1]));
        } else {
          longMap.put(
              tempValue[0],
              (Long) randomObject.generateRandom(ValidTypes.LONG, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }
    if (longMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(longMap.size() - mapSize); i++) {
        longMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            (Long) randomObject.generateRandom(ValidTypes.LONG, valueLength, Collections.emptyList(), constrains)
        );
      }
    }
    return longMap;
  }

  private Map<String, Double> generateDoubleMap(int mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Double> doubleMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (doubleMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          doubleMap.put(tempValue[0], Double.parseDouble(tempValue[1]));
        } else {
          doubleMap.put(
              tempValue[0],
              (Double) randomObject.generateRandom(ValidTypes.DOUBLE, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }
    if (doubleMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(doubleMap.size() - mapSize); i++) {
        doubleMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            (Double) randomObject.generateRandom(ValidTypes.DOUBLE, valueLength, Collections.emptyList(), constrains)
        );
      }
    }
    return doubleMap;
  }

  private Map<String, Float> generateFloatMap(int mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {

    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Float> floatMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (floatMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          floatMap.put(tempValue[0], Float.parseFloat(tempValue[1]));
        } else {
          floatMap.put(
              tempValue[0],
              (Float) randomObject.generateRandom(ValidTypes.FLOAT, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }
    if (floatMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(floatMap.size() - mapSize); i++) {
        floatMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            (Float) randomObject.generateRandom(ValidTypes.FLOAT, valueLength, Collections.emptyList(), constrains)
        );
      }
    }
    return floatMap;
  }

  private Map<String, Short> generateShortMap(int mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Short> shortMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (shortMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          shortMap.put(tempValue[0], Short.parseShort(tempValue[1]));
        } else {
          shortMap.put(
              tempValue[0],
              (Short) randomObject.generateRandom(ValidTypes.SHORT, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }
    if (shortMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(shortMap.size() - mapSize); i++) {
        shortMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            (Short) randomObject.generateRandom(ValidTypes.SHORT, valueLength, Collections.emptyList(), constrains)
        );
      }
    }
    return shortMap;
  }

  private Map<String, String> generateStringMap(int mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, String> stringMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (stringMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          stringMap.put(tempValue[0], tempValue[1]);
        } else {
          stringMap.put(
              tempValue[0],
              (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }
    if (stringMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(stringMap.size() - mapSize); i++) {
        stringMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains)
        );
      }
    }
    return stringMap;
  }

  private Map<String, UUID> generateUuidMap(int mapSize, List<String> fieldValueList) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, UUID> uuidMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (uuidMap.size() <  Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          uuidMap.put(tempValue[0], UUID.fromString(tempValue[1]));
        } else {
          uuidMap.put(
              tempValue[0],
              (UUID) randomObject.generateRandom(ValidTypes.UUID, 0, Collections.emptyList(), Collections.emptyMap())
          );
        }
      }
    }
    if (uuidMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(uuidMap.size() - mapSize); i++) {
        uuidMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, 0, Collections.emptyList(), Collections.emptyMap()),
            (UUID) randomObject.generateRandom(ValidTypes.UUID, 0, Collections.emptyList(), Collections.emptyMap())
        );
      }
    }
    return uuidMap;
  }

  private Map<String, Boolean> generateBooleanMap(Integer mapSize, List<String> fieldValueList) {
    int size = mapSize>0?mapSize:RandomUtils.nextInt(1,5);
    Map<String, Boolean> booleanMap = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (booleanMap.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          booleanMap.put(tempValue[0], Boolean.parseBoolean(tempValue[1]));
        } else {
          booleanMap.put(
              tempValue[0],
              (Boolean) randomObject.generateRandom(ValidTypes.BOOLEAN, 0, Collections.emptyList(), Collections.emptyMap())
          );
        }
      }
    }
    if (booleanMap.size() != mapSize) {
      for (int i = 0; i <= Math.abs(booleanMap.size() - mapSize); i++) {
        booleanMap.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, 0, Collections.emptyList(), Collections.emptyMap()),
            (Boolean) randomObject.generateRandom(ValidTypes.BOOLEAN, 0, Collections.emptyList(), Collections.emptyMap())
        );
      }
    }
    return booleanMap;
  }

  private static String[] getMapEntryValue(List<String> fieldValueList) {
    return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
  }
}
