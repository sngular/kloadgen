/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

public abstract class SchemaProcessorLib {

  protected static final Map<String, Object> context = new HashMap<>();

  private static final RandomObject randomObject = new RandomObject();

  private static final RandomMap randomMap = new RandomMap();

  private static final RandomArray randomArray = new RandomArray();

  private static final RandomSequence randomSequence = new RandomSequence();

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

  protected static String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    String[] splitPath = fieldValueMapping.getFieldName().split("\\.");
    return String.join(".", Arrays.copyOfRange(splitPath, level, splitPath.length));
  }

  protected static String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    return getFullMethodName(fieldValueMapping, fieldName, level).replaceAll("\\[[0-9]*:?]", "");
  }

  protected static String getCompleteSubfieldName(String cleanPath) {
    return cleanPath.substring(0, cleanPath.indexOf(".")>0?cleanPath.indexOf(".")+1:cleanPath.length());
  }

  static String getTypeFilter(String fieldName, String cleanPath) {
    String tmpCleanPath = cleanPath.replaceAll(fieldName, "");
    return tmpCleanPath.substring(0, tmpCleanPath.indexOf(".")>0?tmpCleanPath.indexOf(".")+1:tmpCleanPath.length());
  }

  static String getFullMethodName(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName, level);
    int endOfField = pathToClean.contains(".") ? pathToClean.indexOf(".") : pathToClean.length();
    return pathToClean.substring(0, endOfField);
  }

  protected static String getMapCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName, final int level) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName, level);
    int endOfField = pathToClean.contains("[") ? pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[\\d*:?]", "");
  }

  /*static Object generateRandomMap(String fieldName, String fieldType, Integer mapSize, Integer fieldValueLength, Integer arraySize, List<String> fieldValuesList) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomSequence.isTypeSupported(fieldType))) {
        value.put(generateMapKey(), randomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, context));
      } else {
        for (int i = mapSize; i > 0; i--) {
          value.put(generateMapKey(), randomSequence.generateSeq(fieldName, fieldType, parameterList, context));
        }
      }
    } else {
      return randomMap.generateMap(fieldType, mapSize, parameterList, fieldValueLength, arraySize, Collections.emptyMap());
    }

    return value;
  }*/

  /*static Object generateRandomList(String fieldName, String fieldType, int arraySize, Integer valueLength, List<String> fieldValuesList) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );
    List value = new ArrayList<>(arraySize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomSequence.isTypeSupported(fieldType))) {
        value.add(randomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, context));
      } else {
        for (int i = arraySize; i > 0; i--) {
          value.add(randomSequence.generateSeq(fieldName, fieldType, parameterList, context));
        }
      }
    } else {
      value = (ArrayList) randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    }

    return value;
  }

  protected static String generateMapKey() {
    return (String) randomObject.generateRandom("string", 2, Collections.emptyList(), Collections.emptyMap());
  }

  protected static Object createArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    return generateRandomList(fieldName, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList());
  }*/

  /*protected static Object createSimpleTypeMap(String fieldName, String fieldType, Integer mapSize, Integer fieldValueLength, List<String> fieldExpMappings) {
    return generateRandomMap(fieldName, fieldType, mapSize, fieldValueLength, 0, fieldExpMappings);
  }*/

  /*protected static Map<String, Object> createSimpleTypeArrayMap(
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

  protected String getNameObjectCollection(String fieldName) {
    return fieldName.matches("[\\w\\d]*[\\[\\:*\\]]*\\[\\:*\\]\\.[\\w\\d]*.*") ?
        fieldName.substring(0, fieldName.indexOf(".") + 1) : fieldName;
  }

  protected ProcessorFieldTypeEnum getFieldType(FieldValueMapping fieldValueMapping, String cleanPath) {
    //String fieldNameProcessed = getFirstPartOfFieldValueMappingName(fieldValueMapping);
    if (cleanPath.contains("[")) {
      String completeFieldName = getNameObjectCollection(cleanPath);
      if (completeFieldName.contains("].")) {
        if (completeFieldName.contains("][")) {
          if (completeFieldName.contains("[:][:].")) {
            return ProcessorFieldTypeEnum.RECORD_MAP_MAP;
          }
          else if (completeFieldName.contains("[][].")) {
            return ProcessorFieldTypeEnum.RECORD_ARRAY_ARRAY;
          }
          else if (completeFieldName.contains("[:][].")) {
            return ProcessorFieldTypeEnum.RECORD_MAP_ARRAY;
          }
          else if (completeFieldName.contains("[][:].")) {
            return ProcessorFieldTypeEnum.RECORD_ARRAY_MAP;
          }
          else {
            throw new KLoadGenException("Wrong configuration Map - Array");
          }
        } else {
          if (completeFieldName.contains("[:]")) {
            return ProcessorFieldTypeEnum.RECORD_MAP;
          }
          else if (completeFieldName.contains("[]")) {
            return ProcessorFieldTypeEnum.RECORD_ARRAY;
          }
          else {
            throw new KLoadGenException("Wrong configuration of Map or Array");
          }
        }
      } else {
        if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("array-map")) {
          return ProcessorFieldTypeEnum.BASIC_ARRAY_MAP;
        }
        else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
          return ProcessorFieldTypeEnum.BASIC_MAP_ARRAY;
        }
        else if (completeFieldName.contains("[:]")) {
          return ProcessorFieldTypeEnum.BASIC_MAP;
        }
        else if (completeFieldName.contains("[]")) {
          return ProcessorFieldTypeEnum.BASIC_ARRAY;
        }
        else {
          throw new KLoadGenException("Wrong configuration of last element Map or Array");
        }
      }
    } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {
      return ProcessorFieldTypeEnum.BASIC;
    } else {
      return ProcessorFieldTypeEnum.FINAL;
    }
  }*/
}
