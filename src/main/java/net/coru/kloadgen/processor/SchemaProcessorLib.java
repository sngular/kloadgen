/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

public abstract class SchemaProcessorLib {

  private static final Map<String, Object> context = new HashMap<>();

  private static final RandomObject randomObject = new RandomObject();

  private static final RandomMap randomMap = new RandomMap();

  private static final RandomArray randomArray = new RandomArray();

  static boolean checkIfIsRecordMapArray(String cleanPath) {
    return StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]")).contains("][");
  }

  static boolean checkIfRecordMap(String cleanPath) {
    return cleanPath.contains(":].");
  }

  static boolean checkIfRecordArray(String cleanPath) {
    return cleanPath.contains("].");
  }

  static boolean checkIfIsRecordArrayMap(String cleanPath) {
    return !StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]")).contains("][");
  }

  static boolean checkIfMap(String typeFilter) {

    return typeFilter.matches("\\[\\d?:]");
  }

  static boolean checkIfArray(String typeFilter) {
    return typeFilter.matches("\\[\\d?]");
  }

  static boolean checkIfArrayMap(String type) {
    return type.endsWith("array-map");
  }

  static boolean checkIfMapArray(String type) {
    return type.endsWith("map-array");
  }

  static FieldValueMapping getSafeGetElement(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  static Integer calculateSize(String fieldName, String methodName) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String tempString = fieldName.substring(fieldName.lastIndexOf(methodName));

    tempString = tempString.isEmpty() ? fieldName.replace(methodName, "") : !tempString.contains("[") ? StringUtils.substringAfterLast(fieldName, methodName) : tempString;
    String arrayStringSize = "";
    Pattern pattern = Pattern.compile("\\[\\d*]");
    Matcher matcher = pattern.matcher(tempString);
    while (matcher.find()) {
      arrayStringSize = matcher.group();
    }
    arrayStringSize = StringUtils.isNotEmpty(arrayStringSize) ? StringUtils.substringBetween(arrayStringSize, "[", "]") : "";
    if (StringUtils.isNotEmpty(arrayStringSize) && StringUtils.isNumeric(arrayStringSize)) {
      arrayLength = Integer.parseInt(arrayStringSize);
    }
    return arrayLength;
  }

  static Integer calculateMapSize(String fieldName, String methodName) {
    int mapSize = RandomUtils.nextInt(1, 10);
    int start = fieldName.contains(methodName) ? fieldName.indexOf(methodName) : 0;
    String tempString = fieldName.substring(start,
                                            fieldName.lastIndexOf(methodName));
    tempString = tempString.isEmpty() ? fieldName.replace(methodName, "") : tempString;
    String mapStringSize = "";
    Pattern pattern = Pattern.compile("\\[\\d*:]");
    Matcher matcher = pattern.matcher(tempString);
    while (matcher.find()) {
      mapStringSize = matcher.group();
    }
    mapStringSize = !mapStringSize.isEmpty() ? StringUtils.substringBetween(mapStringSize, "[", ":]") : "";

    if (StringUtils.isNotEmpty(mapStringSize) && StringUtils.isNumeric(mapStringSize)) {
      mapSize = Integer.parseInt(mapStringSize);
    }
    return mapSize;

  }

  static String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName) {
    int startPosition = 0;
    String cleanPath;
    if (StringUtils.isNotEmpty(fieldName)) {
      startPosition = fieldValueMapping.getFieldName().indexOf(fieldName) + fieldName.length() + 1;
    }
    cleanPath = fieldValueMapping.getFieldName().substring(startPosition);
    if (cleanPath.matches("^(\\d*:?]).*$")) {
      cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
    }
    return cleanPath;
  }

  static String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    return getFullMethodName(fieldValueMapping, fieldName).replaceAll("\\[[0-9]*:?]", "");
  }

  static String getFullMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains(".") ? pathToClean.indexOf(".") : pathToClean.length();
    return pathToClean.substring(0, endOfField);
  }

  static String getMapCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains("[") ? pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[\\d*:?]", "");
  }

  static Object generateRandomMap(String fieldName, String fieldType, Integer mapSize, Integer fieldValueLength, Integer arraySize, List<String> fieldValuesList) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      for (int i = mapSize; i > 0; i--) {
        value.put(generateMapKey(),
                  randomObject.generateSeq(fieldName, fieldType, parameterList, context));
      }
    } else if (!parameterList.isEmpty() && parameterList.get(0).charAt(0) == "{".charAt(0)) {
      parameterList.set(0, parameterList.get(0).substring(1));
      value.put(generateMapKey(), randomObject.generateSequenceForFieldValueList(parameterList.get(0), fieldType, parameterList, context));
    } else {
      return randomMap.generateMap(fieldType, mapSize, parameterList, fieldValueLength, arraySize, Collections.emptyMap());
    }

    return value;
  }

  static Object generateRandomList(String fieldName, String fieldType, int arraySize, Integer valueLength, List<String> fieldValuesList) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    var value = new ArrayList<>(arraySize);
    if ("seq".equals(fieldType)) {
      for (int i = arraySize; i > 0; i--) {
        value.add(randomObject.generateSeq(fieldName, fieldType, parameterList, context));
      }
    } else {
      value = (ArrayList) randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    }

    return value;
  }

  static String generateMapKey() {
    return (String) randomObject.generateRandom("string", 2, Collections.emptyList(), Collections.emptyMap());
  }

  static Object createArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    return generateRandomList(fieldName, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList());
  }

  static Object createSimpleTypeMap(String fieldName, String fieldType, Integer mapSize, Integer fieldValueLength, List<String> fieldExpMappings) {
    return generateRandomMap(fieldName, fieldType, mapSize, fieldValueLength, 0, fieldExpMappings);
  }

  static Map<String, Object> createSimpleTypeArrayMap(
      String fieldName, String fieldType, Integer arraySize, Integer mapSize, Integer fieldValueLength, List<String> fieldExpMappings) {
    Map<String, Object> result = new HashMap<>(mapSize);
    String type = fieldType;
    if (type.endsWith("array-map")) {
      type = fieldType.replace("-map", "");
    }
    for (int i = 0; i < mapSize; i++) {
      var list = generateRandomList(fieldName, type, arraySize, fieldValueLength, fieldExpMappings);
      result.put(generateMapKey(), list);
    }
    return result;
  }

}
