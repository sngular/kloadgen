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
import net.coru.kloadgen.randomtool.random.RandomIterator;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;

public class StatelessGeneratorTool {

  private final Map<String, Object> context = new HashMap<>();

  private final RandomMap randomMap = new RandomMap();

  private final RandomArray randomArray = new RandomArray();

  private final RandomObject randomObject = new RandomObject();

  private final RandomSequence randomSequence = new RandomSequence();

  private final RandomIterator randomIterator = new RandomIterator();

  public String generateRandomString(Integer valueLength) {
    return (String) randomObject.generateRandom(ValidTypeConstants.STRING, valueLength, Collections.emptyList(), Collections.emptyMap());
  }

  public Object generateObject(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);

    Object value;

    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomSequence.isTypeSupported(fieldType))) {
        return randomSequence.generateSequenceForFieldValueList(fieldName, fieldType, fieldValuesList, context);
      } else {
        value = randomSequence.generateSeq(fieldName, fieldType, parameterList, context);
      }
    } else if ("it".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomIterator.isTypeSupported(fieldType))) {
        return randomIterator.generateIteratorForFieldValueList(fieldName, fieldType, fieldValuesList, context);
      } else {
        value = randomIterator.generateIt(fieldName, fieldType, parameterList, context);
      }
    } else {
      value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    }
    return value;
  }

  public Object generateMap(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {

    if (checkIfNullFieldValueList(fieldValuesList) && (fieldType.endsWith("-array") || fieldType.endsWith("-map"))) {
      return fieldType.endsWith("-array") ? new ArrayList<>() : new HashMap<>();
    }
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    return randomMap.generateMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());
  }

  public Object generateArray(String fieldName, String fieldType, Integer arraySize, Integer valueLength, List<String> fieldValuesList) {

    if (checkIfNullFieldValueList(fieldValuesList)) {
      return fieldType.endsWith("-array") ? new ArrayList<>() : new HashMap<>();
    }

    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    Object value = randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = randomSequence.generateSeq(fieldName, fieldType, parameterList, context);
    }
    if ("it".equals(fieldType)) {
      value = randomIterator.generateIt(fieldName, fieldType, parameterList, context);
    }

    return value;
  }

  public boolean checkIfNullFieldValueList(List<String> fieldValueList) {
    return fieldValueList == null || (fieldValueList.size() == 1 && fieldValueList.contains("null"));
  }
}
