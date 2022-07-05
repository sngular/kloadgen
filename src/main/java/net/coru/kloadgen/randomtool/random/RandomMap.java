/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;
import org.apache.commons.lang3.RandomUtils;

public class RandomMap {

  private final RandomObject randomObject;

  public RandomMap() {
    randomObject = new RandomObject();
  }

  private static String[] getMapEntryValue(List<String> fieldValueList) {
    return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
  }

  public Object generateMap(
      String fieldType, Integer mapSize, List<String> fieldValueList, Integer arraySize,
      Map<ConstraintTypeEnum, String> constraints) {
    Object value;

    switch (fieldType) {
      case ValidTypeConstants.INT:
        value = generate(ValidTypeConstants.INT, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.LONG:
        value = generate(ValidTypeConstants.LONG, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.DOUBLE:
        value = generate(ValidTypeConstants.DOUBLE, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.SHORT:
        value = generate(ValidTypeConstants.SHORT, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.NUMBER:
      case ValidTypeConstants.FLOAT:
        value = generate(ValidTypeConstants.FLOAT, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.STRING:
        value = generate(ValidTypeConstants.STRING, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.UUID:
        value = generate(ValidTypeConstants.UUID, mapSize, fieldValueList, mapSize, Collections.emptyMap());
        break;
      case ValidTypeConstants.BOOLEAN:
        value = generate(ValidTypeConstants.BOOLEAN, mapSize, fieldValueList, mapSize, Collections.emptyMap());
        break;
      default:
        value = fieldType;
        break;
    }

    if (fieldType.endsWith("array")) {
      value = generateRandomMapArray(fieldType, mapSize, fieldValueList, mapSize, arraySize, constraints);
    } else if (fieldType.endsWith("map-map")) {
      value = generateMapOfMap(fieldType.replace("-map-map", "-map"), mapSize, mapSize, fieldValueList, arraySize, constraints);
    }

    return value;
  }

  public Object generateMap(
      String fieldType, Integer mapSize, List<String> fieldValueList, Integer valueLength, Integer arraySize,
      Map<ConstraintTypeEnum, String> constraints) {
    Object value;

    switch (fieldType) {
      case ValidTypeConstants.INT:
        value = generate(ValidTypeConstants.INT, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.LONG:
        value = generate(ValidTypeConstants.LONG, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.DOUBLE:
        value = generate(ValidTypeConstants.DOUBLE, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.SHORT:
        value = generate(ValidTypeConstants.SHORT, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.FLOAT:
        value = generate(ValidTypeConstants.FLOAT, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.STRING:
        value = generate(ValidTypeConstants.STRING, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.UUID:
        value = generate(ValidTypeConstants.UUID, mapSize, fieldValueList, valueLength, Collections.emptyMap());
        break;
      case ValidTypeConstants.BOOLEAN:
        value = generate(ValidTypeConstants.BOOLEAN, mapSize, fieldValueList, valueLength, Collections.emptyMap());
        break;
      default:
        value = fieldType;
        break;
    }

    if (fieldType.endsWith("array")) {
      value = generateRandomMapArray(fieldType, mapSize, fieldValueList, valueLength, arraySize, constraints);
    } else if (fieldType.endsWith("map-map")) {
      value = generateMapOfMap(fieldType.replace("-map-map", "-map"), mapSize, mapSize, fieldValueList, valueLength, constraints);
    }

    return value;
  }

  private Object generateRandomMapArray(
      String type, Integer mapSize, List<String> fieldValueList, Integer valueLength, Integer arraySize,
      Map<ConstraintTypeEnum, String> constraints) {

    List<Map<String, Object>> generatedMapArray = new ArrayList<>(arraySize);
    int tempValueLength = valueLength;
    if (valueLength == 0) {
      tempValueLength = (int) Math.floor(Math.random() * (9 - 1 + 1) + 1);
    }
    for (int i = 0; i < arraySize; i++) {
      String newType = type.substring(0, type.length() - 6);
      generatedMapArray.add((Map<String, Object>) generateMap(newType, mapSize, fieldValueList, tempValueLength, arraySize, constraints));
    }

    return generatedMapArray;
  }

  private Map<String, Object> generate(
      String type, Integer mapSize, List<String> fieldValueList, int valueLength,
      Map<ConstraintTypeEnum, String> constraints) {
    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
    Map<String, Object> map = new HashMap<>(size);
    if (!fieldValueList.isEmpty()) {
      while (map.size() < Math.min(size, fieldValueList.size())) {
        String[] tempValue = getMapEntryValue(fieldValueList);
        if (tempValue.length > 1) {
          switch (type) {
            case ValidTypeConstants.INT:
              map.put(tempValue[0], Integer.parseInt(tempValue[1]));
              break;
            case ValidTypeConstants.LONG:
              map.put(tempValue[0], Long.parseLong(tempValue[1]));
              break;
            case ValidTypeConstants.FLOAT:
              map.put(tempValue[0], Float.parseFloat(tempValue[1]));
              break;
            case ValidTypeConstants.DOUBLE:
              map.put(tempValue[0], Double.parseDouble(tempValue[1]));
              break;
            case ValidTypeConstants.SHORT:
              map.put(tempValue[0], Short.parseShort(tempValue[1]));
              break;
            case ValidTypeConstants.UUID:
              map.put(tempValue[0], UUID.fromString(tempValue[1]));
              break;
            default:
              map.put(tempValue[0], tempValue[1]);
              break;
          }

        } else {
          map.put(
              tempValue[0],
              randomObject.generateRandom(type, valueLength, Collections.emptyList(), constraints)
          );
        }
      }
    }

    if (map.size() != mapSize) {
      for (int i = 0; i <= Math.abs(map.size() - mapSize); i++) {
        map.put(
            (String) randomObject.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), constraints),
            randomObject.generateRandom(type, valueLength, Collections.emptyList(), constraints)
        );
      }
    }

    return map;
  }

  private Map<String, Object> generateMapOfMap(
      String type, Integer mapSize, Integer innerMapSize, List<String> fieldValueList, int valueLength, Map<ConstraintTypeEnum, String> constraints) {

    int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
    Map<String, Object> map = new HashMap<>(size);

    for (int i = 0; i <= Math.abs(map.size() - mapSize); i++) {
      map.put(
          (String) randomObject.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), constraints),
          generateMap(type, innerMapSize, fieldValueList, valueLength, 0, constraints)
      );
    }
    return map;
  }
}
