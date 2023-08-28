/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.randomtool.util.ValidTypeConstants;
import org.apache.commons.lang3.RandomUtils;

public class RandomMap {

  private final RandomObject randomObject;

  public RandomMap() {
    randomObject = new RandomObject();
    randomArray = new RandomArray();
  }

  private final RandomArray randomArray;

  private static String[] getMapEntryValue(final List<String> fieldValueList) {
    return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
  }

  private static String[] getMapEntryValueAndRemove(final List<String> fieldValueList) {
    final int randomAux = RandomUtils.nextInt(0, fieldValueList.size());
    final String[] resultAux = fieldValueList.get(randomAux).trim().split(":");
    fieldValueList.remove(randomAux);
    return resultAux;
  }

  public final Object generateMap(
      final String fieldType, final Integer mapSize, final List<String> fieldValueList, final Integer arraySize,
      final Map<ConstraintTypeEnum, String> constraints) {
    Object value;

    switch (fieldType) {
      case ValidTypeConstants.INT_MAP:
        value = generate(ValidTypeConstants.INT, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.LONG_MAP:
        value = generate(ValidTypeConstants.LONG, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.DOUBLE_MAP:
        value = generate(ValidTypeConstants.DOUBLE, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.SHORT_MAP:
        value = generate(ValidTypeConstants.SHORT, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.NUMBER_MAP:
      case ValidTypeConstants.FLOAT_MAP:
        value = generate(ValidTypeConstants.FLOAT, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.STRING_MAP:
        value = generate(ValidTypeConstants.STRING, mapSize, fieldValueList, mapSize, constraints);
        break;
      case ValidTypeConstants.UUID_MAP:
        value = generate(ValidTypeConstants.UUID, mapSize, fieldValueList, mapSize, Collections.emptyMap());
        break;
      case ValidTypeConstants.BOOLEAN_MAP:
        value = generate(ValidTypeConstants.BOOLEAN, mapSize, fieldValueList, mapSize, Collections.emptyMap());
        break;
      case ValidTypeConstants.STRING_ARRAY:
        value = generate(ValidTypeConstants.STRING_ARRAY, mapSize, fieldValueList, mapSize, constraints);
        break;
      default:
        value = fieldType;
        break;
    }

    if (fieldType.endsWith("map-map")) {
      value = generateMapOfMap(fieldType.replace("-map-map", "-map"), mapSize, mapSize, fieldValueList, arraySize, constraints);
    }

    return value;
  }

  public final Object generateMap(
      final String fieldType, final Integer mapSize, final List<String> fieldValueList, final Integer valueLength, final Integer arraySize,
      final Map<ConstraintTypeEnum, String> constraints) {
    Object value;

    switch (fieldType) {
      case ValidTypeConstants.INT_MAP:
        value = generate(ValidTypeConstants.INT, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.LONG_MAP:
        value = generate(ValidTypeConstants.LONG, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.DOUBLE_MAP:
        value = generate(ValidTypeConstants.DOUBLE, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.SHORT_MAP:
        value = generate(ValidTypeConstants.SHORT, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.FLOAT_MAP:
        value = generate(ValidTypeConstants.FLOAT, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.STRING_MAP:
        value = generate(ValidTypeConstants.STRING, mapSize, fieldValueList, valueLength, constraints);
        break;
      case ValidTypeConstants.UUID_MAP:
        value = generate(ValidTypeConstants.UUID, mapSize, fieldValueList, valueLength, Collections.emptyMap());
        break;
      case ValidTypeConstants.BOOLEAN_MAP:
        value = generate(ValidTypeConstants.BOOLEAN, mapSize, fieldValueList, valueLength, Collections.emptyMap());
        break;
      case ValidTypeConstants.STRING_ARRAY:
        value = generate(ValidTypeConstants.STRING_ARRAY, mapSize, fieldValueList, mapSize, constraints);
        break;
      default:
        value = fieldType;
        break;
    }

    if (fieldType.endsWith("map-map")) {
      value = generateMapOfMap(fieldType.replace("-map-map", "-map"), mapSize, mapSize, fieldValueList, valueLength, constraints);
    }

    return value;
  }

  private Map<String, Object> generate(
      final String type, final Integer mapSize, final List<String> fieldValueList, final int valueLength,
      final Map<ConstraintTypeEnum, String> constraints) {
    final int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
    final Map<String, Object> map = new HashMap<>(size);
    final List<String> fieldValueListAux = new ArrayList<>(fieldValueList);
    if (!fieldValueList.isEmpty()) {
      while (map.size() < Math.min(size, fieldValueList.size())) {
        final String[] tempValue = getMapEntryValueAndRemove(fieldValueListAux);
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
            case ValidTypeConstants.STRING_ARRAY:
              /* array.addAll() */
              String[] array = tempValue[1].substring(tempValue[1].indexOf("[")).replaceAll("[^a-zA-Z\\s*,\\s*^0-9]", "").split("\\s*,\\s*", -1);
              map.put(tempValue[0], randomArray.generateArray(type, array.length, List.of(array), array.length, constraints));
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
      final String type, final Integer mapSize, final Integer innerMapSize, final List<String> fieldValueList, final int valueLength,
      final Map<ConstraintTypeEnum, String> constraints) {

    final int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
    final Map<String, Object> map = new HashMap<>(size);

    for (int i = 0; i <= Math.abs(map.size() - mapSize); i++) {
      map.put(
          (String) randomObject.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), constraints),
          generateMap(type, innerMapSize, fieldValueList, valueLength, 0, constraints)
      );
    }
    return map;
  }
}
