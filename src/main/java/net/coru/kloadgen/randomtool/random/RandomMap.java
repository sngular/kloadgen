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
        value = generate(ValidTypes.INT, mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.LONG_MAP:
        value = generate(ValidTypes.LONG, mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.DOUBLE_MAP:
        value = generate(ValidTypes.DOUBLE, mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.SHORT_MAP:
        value = generate(ValidTypes.SHORT, mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.FLOAT_MAP:
        value = generate(ValidTypes.FLOAT, mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.STRING_MAP:
        value = generate(ValidTypes.STRING, mapSize, fieldValueList, mapSize, constrains);
        break;
      case ValidTypes.UUID_MAP:
        value = generate(ValidTypes.UUID, mapSize, fieldValueList, mapSize, Collections.emptyMap());
        break;
      case ValidTypes.BOOLEAN_MAP:
        value = generate(ValidTypes.BOOLEAN, mapSize, fieldValueList, mapSize, Collections.emptyMap());
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
      String newType = type.substring(0, type.length() - 6);
      generatedMapArray.add((Map<String, Object>) generateMap(newType, valueLength, fieldValueList, arraySize, constrains));
    }

    return generatedMapArray;
  }

  private Map<String, Object> generate(String type, Integer mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1,5);
    Map<String, Object> map = new HashMap<>(size);

    if (!fieldValueList.isEmpty()) {
      while (map.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          switch (type) {
            case ValidTypes.INT:
              map.put(tempValue[0], Integer.parseInt(tempValue[1]));
              break;
            case ValidTypes.LONG:
              map.put(tempValue[0], Long.parseLong(tempValue[1]));
              break;
            case ValidTypes.FLOAT:
              map.put(tempValue[0], Float.parseFloat(tempValue[1]));
              break;
            case ValidTypes.DOUBLE:
              map.put(tempValue[0], Double.parseDouble(tempValue[1]));
              break;
            case ValidTypes.SHORT:
              map.put(tempValue[0], Short.parseShort(tempValue[1]));
              break;
            case ValidTypes.UUID:
              map.put(tempValue[0], UUID.fromString(tempValue[1]));
              break;
            default:
              map.put(tempValue[0], tempValue[1]);
              break;
          }

        } else {
          map.put(
              tempValue[0],
              randomObject.generateRandom(type, valueLength, Collections.emptyList(), constrains)
          );
        }
      }
    }

    if (map.size() != mapSize) {
      for (int i = 0; i <= Math.abs(map.size() - mapSize); i++) {
        map.put(
            (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, Collections.emptyList(), constrains),
            randomObject.generateRandom(type, valueLength, Collections.emptyList(), constrains)
        );
      }
    }

    return map;
  }

  private static String[] getMapEntryValue(List<String> fieldValueList) {
    return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
  }
}
