/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;
import net.coru.kloadgen.randomtool.util.ValueUtils;

public class StatelessGeneratorTool {

  private final Map<String, Object> context = new HashMap<>();

  private final RandomMap randomMap = new RandomMap();

  private final RandomArray randomArray = new RandomArray();

  private final RandomObject randomObject = new RandomObject();

  public String generateRandomString(final Integer valueLength) {
    return (String) randomObject.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), Collections.emptyMap());
  }

  public Object generateObject(final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);

    final Object value;

    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value = RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, fieldValuesList, context);
      } else {
        value = RandomSequence.generateSeq(fieldName, fieldType, parameterList, context);
      }
    } else {
      value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    }
    return value;
  }

  public Object generateMap(final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Integer size) {

    if (checkIfNullFieldValueList(fieldValuesList) && (fieldType.endsWith("-array") || fieldType.endsWith("-map"))) {
      return fieldType.endsWith("-array") ? new ArrayList<>() : new HashMap<>();
    }
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    return randomMap.generateMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());
  }

  public boolean checkIfNullFieldValueList(final List<String> fieldValueList) {
    return fieldValueList == null || (fieldValueList.size() == 1 && fieldValueList.contains("null"));
  }

  public Object generateArray(final String fieldName, final String fieldType, final Integer arraySize, final Integer valueLength, final List<String> fieldValuesList) {

    if (checkIfNullFieldValueList(fieldValuesList)) {
      return fieldType.endsWith("-array") ? new ArrayList<>() : new HashMap<>();
    }

    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    Object value = randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = RandomSequence.generateSeq(fieldName, fieldType, parameterList, context);
    }

    return value;
  }
}
