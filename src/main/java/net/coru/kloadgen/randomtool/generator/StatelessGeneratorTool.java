/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.util.ValueUtils;

public class StatelessGeneratorTool {

  private final Map<String, Object> context = new HashMap<>();

  private final RandomMap randomMap = new RandomMap();

  private final RandomArray randomArray = new RandomArray();

  private final RandomObject randomObject = new RandomObject();

  public Object generateObject(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);

    Object value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && '{' == fieldValuesList.get(0).charAt(0)) {
        fieldValuesList.set(0, fieldValuesList.get(0).substring(1));
        return randomObject.generateSequenceForFieldValueList(fieldValuesList.get(0), fieldType, fieldValuesList, context);
      } else {
        value = randomObject.generateSeq(fieldName, fieldType, parameterList, context);
      }
    }
    return value;
  }

  public Object generateMap(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    return randomMap.generateMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());
  }

  public Object generateArray(String fieldName, String fieldType, Integer arraySize, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);

    Object value = randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = randomObject.generateSeq(fieldName, fieldType, parameterList, context);
    }

    return value;
  }
}
