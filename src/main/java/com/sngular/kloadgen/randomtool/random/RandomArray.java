/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.randomtool.util.ValidTypeConstants;
import org.apache.commons.lang3.RandomUtils;

public class RandomArray {

  private final RandomObject randomObject;

  public RandomArray() {
    randomObject = new RandomObject();
  }

  protected static boolean isArray(final String type) {
    return type.toLowerCase().endsWith("array");
  }

  public final Object generateArray(
      final String fieldType, final Integer valueLength, final List<String> fieldValueList, final Integer arraySize,
      final Map<ConstraintTypeEnum, String> constraints) {

    Object value;

    switch (fieldType) {
      case ValidTypeConstants.INT_ARRAY:
      case ValidTypeConstants.NUMBER_ARRAY:
        value = generate(ValidTypeConstants.INT, arraySize, valueLength, fieldValueList, constraints);
        break;
      case ValidTypeConstants.LONG_ARRAY:
        value = generate(ValidTypeConstants.LONG, arraySize, valueLength, fieldValueList, constraints);
        break;
      case ValidTypeConstants.DOUBLE_ARRAY:
        value = generate(ValidTypeConstants.DOUBLE, arraySize, valueLength, fieldValueList, constraints);
        break;
      case ValidTypeConstants.SHORT_ARRAY:
        value = generate(ValidTypeConstants.SHORT, arraySize, valueLength, fieldValueList, constraints);
        break;
      case ValidTypeConstants.FLOAT_ARRAY:
        value = generate(ValidTypeConstants.FLOAT, arraySize, valueLength, fieldValueList, constraints);
        break;
      case ValidTypeConstants.STRING_ARRAY:
        value = generate(ValidTypeConstants.STRING, arraySize, valueLength, fieldValueList, constraints);
        break;
      case ValidTypeConstants.UUID_ARRAY:
        value = generate(ValidTypeConstants.UUID, arraySize, 0, fieldValueList, Collections.emptyMap());
        break;
      case ValidTypeConstants.BOOLEAN_ARRAY:
        value = generate(ValidTypeConstants.BOOLEAN, arraySize, 0, fieldValueList, Collections.emptyMap());
        break;
      default:
        value = new ArrayList<>();
        break;
    }

    if (fieldType.endsWith("-array-array")) {
      value = generateArrayOfArray(fieldType.replace("-array-array", "-array"), valueLength, fieldValueList, arraySize, arraySize,
                                   constraints);
    } else if (fieldType.endsWith("map")) {
      value = generateRandomArrayMap(fieldType.replace("-map", ""), valueLength, fieldValueList, arraySize, arraySize,
                                     constraints);
    }
    return value;
  }

  private Object generateRandomArrayMap(
      final String fieldType, final Integer valueLength, final List<String> fieldValueList, final Integer arraySize,
      final Integer innerArraySize, final Map<ConstraintTypeEnum, String> constraints) {
    final int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    final Map<String, Object> map = new java.util.HashMap<>(arraySize);

    for (int i = 0; i < size; i++) {
      map.put(
          (String) randomObject.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), constraints),
          generateArray(fieldType, valueLength, fieldValueList, innerArraySize, constraints)
      );
    }
    return map;
  }

  private Object generateArrayOfArray(
      final String fieldType, final Integer valueLength, final List<String> fieldValueList, final Integer arraySize, final Integer innerArraySize,
      final Map<ConstraintTypeEnum, String> constraints) {

    final int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    final List<Object> array = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      array.add(generateArray(fieldType, valueLength, fieldValueList, innerArraySize, constraints));
    }
    return array;
  }

  private List<Object> generate(
      final String type, final Integer arraySize, final Integer valueLength, final List<String> fieldValueList,
      final Map<ConstraintTypeEnum, String> constraints) {
    final int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    final List<Object> array = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      array.add(randomObject.generateRandom(type, valueLength, fieldValueList, constraints));
    }

    return array;
  }
}
