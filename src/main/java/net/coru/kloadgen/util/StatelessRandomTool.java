/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.threads.JMeterContextService;

public class StatelessRandomTool {

  private final Map<String, Object> context = new HashMap<>();

  public Object generateRandom(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
        fieldValue.matches("\\$\\{\\w*}") ?
            JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
            fieldValue);

    Object value = RandomTool.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = RandomTool.generateSeq(fieldName, fieldType, parameterList, context);
    }

    return value;
  }

  public Object generateRandomMap(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                     fieldValue.matches("\\$\\{\\w*}") ?
                                             JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                             fieldValue);

    return RandomTool.generateRandomMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());

  }

  public Object generateRandomArray(String fieldName, String fieldType, Integer arraySize, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                     fieldValue.matches("\\$\\{\\w*}") ?
                                             JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                             fieldValue);

    Object value = RandomTool.generateRandomArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = RandomTool.generateSeq(fieldName, fieldType, parameterList, context);
    }

    return value;
  }
}