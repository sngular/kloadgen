/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class SchemaProcessorLib {

  protected static final Map<String, Object> context = new HashMap<>();

  private static final RandomObject randomObject = new RandomObject();

  private static final RandomMap randomMap = new RandomMap();

  private static final RandomArray randomArray = new RandomArray();

  static boolean checkIfIsRecordMapArray(String cleanPath) {
    var indexOfArrayIdentifier = StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]"));
    return indexOfArrayIdentifier.contains("][");

  }

  static boolean checkIfRecordMap(String cleanPath) {
    return cleanPath.contains(":].");
  }

  static boolean checkIfRecordArray(String cleanPath) {
    var substring = StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]"));
    return substring.contains("].");
  }

  static boolean checkIfIsRecordArrayMap(String cleanPath) {
    return !StringUtils.substring(cleanPath, cleanPath.indexOf("["), cleanPath.indexOf(":]")).contains("][");
  }

  static boolean checkIfMap(String typeFilter, String fieldType) {

    return typeFilter.matches("\\[\\d?:]") || fieldType.endsWith("map-map");
  }

  static boolean checkIfArray(String typeFilter, String fieldType) {
    return typeFilter.matches("\\[\\d?]") || fieldType.endsWith("array-array");
  }

  static boolean checkIfArrayMap(String type) {
    return type.endsWith("array-map");
  }

  static boolean checkIfMapArray(String type) {
    return type.endsWith("map-array");
  }

  protected static FieldValueMapping getSafeGetElement(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  protected static Integer calculateSize(String fieldName, String methodName) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    int start = fieldName.contains(methodName) ? fieldName.indexOf(methodName) : 0;
    String tempString = fieldName.substring(start,
                                            fieldName.lastIndexOf(methodName));

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

  protected static Integer calculateMapSize(String fieldName, String methodName) {
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

  protected static String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    return getFullMethodName(fieldValueMapping, fieldName, level).replaceAll("\\[[0-9]*:?]", "");
  }

  static String getFullMethodName(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName, level);
    int endOfField = pathToClean.contains(".") ? pathToClean.indexOf(".") : pathToClean.length();
    return pathToClean.substring(0, endOfField);
  }

  protected static String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    String[] splitPath = fieldValueMapping.getFieldName().split("\\.");
    return String.join(".", Arrays.copyOfRange(splitPath, level, splitPath.length));
  }

  protected static String getCompleteSubfieldName(String cleanPath) {
    return cleanPath.substring(0, cleanPath.indexOf(".") > 0 ? cleanPath.indexOf(".") + 1 : cleanPath.length());
  }

  static String getTypeFilter(String fieldName, String cleanPath) {
    String tmpCleanPath = cleanPath.replaceAll(fieldName, "");
    return tmpCleanPath.substring(0, tmpCleanPath.indexOf(".") > 0 ? tmpCleanPath.indexOf(".") + 1 : tmpCleanPath.length());
  }

  static String getMapCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName, level);
    int endOfField = pathToClean.contains("[") ? pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[\\d*:?]", "");
  }

  static boolean isTypeFilterMap(String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[[1-9]*:]");
  }

  static boolean isTypeFilterArray(String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[\\d*]");
  }

  static Integer calculateSizeFromTypeFilter(String singleTypeFilter) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String arrayStringSize = "";
    Pattern pattern = Pattern.compile("\\d*");
    Matcher matcher = pattern.matcher(singleTypeFilter);
    while (matcher.find()) {
      if (StringUtils.isNumeric(matcher.group(0))) {
        arrayStringSize = matcher.group(0);
      }
    }
    if (StringUtils.isNotEmpty(arrayStringSize) && StringUtils.isNumeric(arrayStringSize)) {
      arrayLength = Integer.parseInt(arrayStringSize);
    }
    return arrayLength;
  }

  static boolean hasMapOrArrayTypeFilter(String typeFilter) {
    return typeFilter.matches("\\[.*].*");
  }

  static String getFirstComplexType(String completeTypeFilterChain) {
    String firstElementTypeFilterChain = completeTypeFilterChain;
    if (StringUtils.isNotEmpty(firstElementTypeFilterChain)) {
      String[] splitElements = firstElementTypeFilterChain.split("\\.");
      if (splitElements.length > 0) {
        firstElementTypeFilterChain = splitElements[0] + ".";
      }
    }
    Pattern pattern = Pattern.compile("\\[.*?]");
    Matcher matcher = pattern.matcher(firstElementTypeFilterChain);
    return matcher.find() ? matcher.group() : firstElementTypeFilterChain;
  }

  static boolean isTypeFilterRecord(String singleTypeFilter) {
    return singleTypeFilter.startsWith(".");
  }

  static boolean isLastTypeFilterOfLastElement(final String completeTypeFilterChain) {
    return !completeTypeFilterChain.matches("\\[.*].*") && !completeTypeFilterChain.matches("\\.");
  }
}
