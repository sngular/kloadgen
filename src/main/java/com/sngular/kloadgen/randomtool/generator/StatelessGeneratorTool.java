/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.randomtool.random.RandomArray;
import com.sngular.kloadgen.randomtool.random.RandomIterator;
import com.sngular.kloadgen.randomtool.random.RandomMap;
import com.sngular.kloadgen.randomtool.random.RandomObject;
import com.sngular.kloadgen.randomtool.random.RandomSequence;
import com.sngular.kloadgen.randomtool.util.ValidTypeConstants;
import com.sngular.kloadgen.randomtool.util.ValueUtils;

public class StatelessGeneratorTool {

  private static final RandomMap RANDOM_MAP = new RandomMap();

  private static final RandomArray RANDOM_ARRAY = new RandomArray();

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private final Map<String, Object> context = new HashMap<>();

  public final String generateRandomString(final Integer valueLength) {
    return (String) RANDOM_OBJECT.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), Collections.emptyMap());
  }

  public final Object generateObject(final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList) {
    final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);

    final Object value;

    if ("seq".equals(fieldType)) {
      value = RandomSequence.generateSeq(fieldName, fieldType, parameterList, context);
    } else if ("it".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomIterator.isTypeNotSupported(fieldType))) {
        value = RandomIterator.generateIteratorForFieldValueList(fieldName, fieldType, fieldValuesList, context);
      } else {
        value = RandomIterator.generateIt(fieldName, fieldType, parameterList, context);
      }
    } else {
      value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    }
    return value;
  }

  public final Object generateMap(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Integer size) {
    final Object result;
    if (checkIfNullFieldValueList(fieldValuesList) && (fieldType.endsWith("-array") || fieldType.endsWith("-map"))) {
      result = fieldType.endsWith("-array") ? new ArrayList<>() : new HashMap<>();
    } else {
      final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
      result = RANDOM_MAP.generateMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());
    }
    return result;
  }

  public final boolean checkIfNullFieldValueList(final List<String> fieldValueList) {
    return fieldValueList == null || fieldValueList.size() == 1 && fieldValueList.contains("null");
  }

  public final Object generateArray(final String fieldName, final String fieldType, final Integer arraySize, final Integer valueLength, final List<String> fieldValuesList) {

    Object result;
    if (checkIfNullFieldValueList(fieldValuesList)) {
      result = fieldType.endsWith("-array") ? new ArrayList<>() : new HashMap<>();
    } else {

      final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
      result = RANDOM_ARRAY.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
      if ("seq".equals(fieldType)) {
        result = RandomSequence.generateSeq(fieldName, fieldType, parameterList, context);
      } else if ("it".equals(fieldType)) {
        result = RandomIterator.generateIt(fieldName, fieldType, parameterList, context);
      }
    }
    return result;
  }
}
