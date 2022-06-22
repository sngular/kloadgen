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
import net.coru.kloadgen.randomtool.random.RandomSequence;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

public final class SchemaProcessorLib {

  private static final Map<String, Object> CONTEXT = new HashMap<>();

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final RandomMap RANDOM_MAP = new RandomMap();

  private static final RandomArray RANDOM_ARRAY = new RandomArray();

  private SchemaProcessorLib() {
  }

  static boolean checkIfIsRecordMapArray(final String cleanPath) {
    final var indexOfArrayIdentifier = StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]"));
    return indexOfArrayIdentifier.contains("][");

  }

  static boolean checkIfRecordMap(final String cleanPath) {
    return cleanPath.contains(":].");
  }

  static boolean checkIfRecordArray(final String cleanPath) {
    final var substring = StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]"));
    return substring.contains("].");
  }

  static boolean checkIfIsRecordArrayMap(final String cleanPath) {
    return !StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]")).contains("][");
  }

  static boolean checkIfMap(final String typeFilter, final String fieldType) {

    return typeFilter.matches("\\[\\d?:]") || fieldType.endsWith("map-map");
  }

  static boolean checkIfArray(final String typeFilter, final String fieldType) {
    return typeFilter.matches("\\[\\d?]") || fieldType.endsWith("array-array");
  }

  static boolean checkIfArrayMap(final String type) {
    return type.endsWith("array-map");
  }

  static boolean checkIfMapArray(final String type) {
    return type.endsWith("map-array");
  }

  static FieldValueMapping getSafeGetElement(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  static Integer calculateSize(final String fieldName, final String methodName) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    final int start = fieldName.contains(methodName) ? fieldName.indexOf(methodName) : 0;
    String tempString = fieldName.substring(start,
                                            fieldName.lastIndexOf(methodName));

    tempString = tempString.isEmpty() ? fieldName.replace(methodName, "") :
        extractNumber(!tempString.contains("["), StringUtils.substringAfterLast(fieldName, methodName), tempString);
    String arrayStringSize = "";
    final Pattern pattern = Pattern.compile("\\[\\d*]");
    final Matcher matcher = pattern.matcher(tempString);
    while (matcher.find()) {
      arrayStringSize = matcher.group();
    }
    arrayStringSize = StringUtils.isNotEmpty(arrayStringSize) ? StringUtils.substringBetween(arrayStringSize, "[", "]") : "";
    if (StringUtils.isNotEmpty(arrayStringSize) && StringUtils.isNumeric(arrayStringSize)) {
      arrayLength = Integer.parseInt(arrayStringSize);
    }
    return arrayLength;
  }

  private static String extractNumber(final boolean tempString, final String fieldName, final String tempString1) {
    return tempString ? fieldName : tempString1;
  }

  static Integer calculateMapSize(final String fieldName, final String methodName) {
    int mapSize = RandomUtils.nextInt(1, 10);
    final int start = fieldName.contains(methodName) ? fieldName.indexOf(methodName) : 0;
    String tempString = fieldName.substring(start,
                                            fieldName.lastIndexOf(methodName));
    tempString = tempString.isEmpty() ? fieldName.replace(methodName, "") : tempString;
    String mapStringSize = "";
    final Pattern pattern = Pattern.compile("\\[\\d*:]");
    final Matcher matcher = pattern.matcher(tempString);
    while (matcher.find()) {
      mapStringSize = matcher.group();
    }
    mapStringSize = !mapStringSize.isEmpty() ? StringUtils.substringBetween(mapStringSize, "[", ":]") : "";

    if (StringUtils.isNotEmpty(mapStringSize) && StringUtils.isNumeric(mapStringSize)) {
      mapSize = Integer.parseInt(mapStringSize);
    }
    return mapSize;

  }

  static String cleanUpPath(final FieldValueMapping fieldValueMapping, final String fieldName) {
    int startPosition = 0;
    String cleanPath;
    if (StringUtils.isNotEmpty(fieldName)) {
      startPosition = fieldValueMapping.getFieldName().indexOf(fieldName) + fieldName.length() + 1;
    }
    cleanPath = fieldValueMapping.getFieldName().substring(startPosition);
    if (cleanPath.matches("^(\\d*:*]).*$")) {
      cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
    }
    return cleanPath;
  }

  static String getCleanMethodName(final FieldValueMapping fieldValueMapping, final String fieldName) {
    return getFullMethodName(fieldValueMapping, fieldName).replaceAll("\\[[0-9]*:?]", "");
  }

  static String getFullMethodName(final FieldValueMapping fieldValueMapping, final String fieldName) {
    final String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    final int endOfField = pathToClean.contains(".") ? pathToClean.indexOf(".") : pathToClean.length();
    return pathToClean.substring(0, endOfField);
  }

  static String getMapCleanMethodName(final FieldValueMapping fieldValueMapping, final String fieldName) {
    final String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    final int endOfField = pathToClean.contains("[") ? pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[\\d*:?]", "");
  }

  static Object generateRandomMap(final String fieldName, final String fieldType, final Integer mapSize, final Integer fieldValueLength, final Integer arraySize,
      final List<String> fieldValuesList) {

    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}")
                                     ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    final var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.put(generateMapKey(), RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
      } else {
        for (int i = mapSize; i > 0; i--) {
          value.put(generateMapKey(), RandomSequence.generateSeq(fieldName, fieldType, parameterList, CONTEXT));
        }
      }
    } else {
      return RANDOM_MAP.generateMap(fieldType, mapSize, parameterList, fieldValueLength, arraySize, Collections.emptyMap());
    }

    return value;
  }

  static Object generateRandomList(final String fieldName, final String fieldType, final int arraySize, final Integer valueLength, final List<String> fieldValuesList) {

    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}")
                                     ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    final List value = new ArrayList<>(arraySize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.add(RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
      } else {
        for (int i = arraySize; i > 0; i--) {
          value.add(RandomSequence.generateSeq(fieldName, fieldType, parameterList, CONTEXT));
        }
      }
    } else {
      value.addAll((ArrayList) RANDOM_ARRAY.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap()));
    }

    return value;
  }

  static String generateMapKey() {
    return (String) RANDOM_OBJECT.generateRandom("string", 2, Collections.emptyList(), Collections.emptyMap());
  }

  static Object createArray(final String fieldName, final Integer arraySize, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    return generateRandomList(fieldName, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList());
  }

  static Object createSimpleTypeMap(final String fieldName, final String fieldType, final Integer mapSize, final Integer fieldValueLength, final List<String> fieldExpMappings) {
    return generateRandomMap(fieldName, fieldType, mapSize, fieldValueLength, 0, fieldExpMappings);
  }

  static Map<String, Object> createSimpleTypeArrayMap(
      final String fieldName, final String fieldType, final Integer arraySize, final Integer mapSize, final Integer fieldValueLength, final List<String> fieldExpMappings) {
    final Map<String, Object> result = new HashMap<>(mapSize);
    String type = fieldType;
    if (type.endsWith("array-map")) {
      type = fieldType.replace("-map", "");
    }
    for (int i = 0; i < mapSize; i++) {
      final var list = generateRandomList(fieldName, type, arraySize, fieldValueLength, fieldExpMappings);
      result.put(generateMapKey(), list);
    }
    return result;
  }

}
