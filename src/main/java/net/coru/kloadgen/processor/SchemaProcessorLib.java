/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SchemaProcessorLib {

    private static final Map<String, Object> context = new HashMap<>();

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

    static boolean checkIfMap(String type) {
        return type.endsWith("map");
    }

    static boolean checkIfArray(String type) {
        return type.endsWith("array");
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

        String tempString = fieldName.substring(0,
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

    static Integer calculateMapSize(String fieldName, String methodName) {
        int mapSize = RandomUtils.nextInt(1, 10);
        String tempString = fieldName.substring(0,
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
        if (cleanPath.matches("^(\\d*:*]).*$")) {
            cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
        }
        return cleanPath;
    }

    static String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
        String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
        int endOfField = pathToClean.contains(".") ?
                pathToClean.indexOf(".") : pathToClean.contains("[") ? pathToClean.indexOf("[") : pathToClean.length();
        return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*:?]", "");
    }

    static String getMapCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
        String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
        int endOfField = pathToClean.contains("[") ? pathToClean.indexOf("[") : 0;
        return pathToClean.substring(0, endOfField).replaceAll("\\[\\d*:?]", "");
    }

    static Map generateRandomMap(String fieldName, String fieldType, Integer mapSize, List<String> fieldValuesList) {

        List<String> parameterList = new ArrayList<>(fieldValuesList);
        parameterList.replaceAll(fieldValue ->
                fieldValue.matches("\\$\\{\\w*}") ?
                        JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                        fieldValue
        );

        Map value = new HashMap<>(mapSize);
        if ("seq".equals(fieldType)) {
            while (mapSize > 0) {
                value.put(RandomTool.generateRandom("String", 4, Collections.emptyList(), Collections.emptyMap()),
                        RandomTool.generateSeq(fieldName, fieldType, parameterList, context));
                mapSize--;
            }
        } else {
            value = new HashMap<>(RandomTool.generateRandomMap(fieldType, mapSize, parameterList, Collections.emptyMap()));
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

        List value = new ArrayList<>(arraySize);
        if ("seq".equals(fieldType)) {
            while (arraySize > 0) {
                value.add(RandomTool.generateSeq(fieldName, fieldType, parameterList, context));
                arraySize--;
            }
        } else {
            value = new ArrayList<>(RandomTool.generateRandomArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap()));
        }

        return value;
    }

    static String generateMapKey() {
        return (String) RandomTool.generateRandom("string", 2, Collections.emptyList(), Collections.emptyMap());
    }

    static Object createArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
        return generateRandomList(fieldName, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList());
    }

    static Object createSimpleTypeMap(String fieldName, String fieldType, Integer mapSize, List<String> fieldExpMappings) {
        return generateRandomMap(fieldName, fieldType, mapSize, fieldExpMappings);
    }

    static List<Map> createSimpleTypeMapArray(String fieldName, String fieldType, Integer arraySize, Integer mapSize, Integer fieldValueLength, List<String> fieldExpMappings) {
        List<Map> result = new ArrayList(arraySize);
        if (fieldType.endsWith("map-array")) {
            fieldType = fieldType.replace("-map", "");
        }
        while (arraySize > 0) {
            result.add(generateRandomMap(fieldName, fieldType, mapSize, fieldExpMappings));
            arraySize--;
        }

        return result;
    }

    static Map<String, Object> createSimpleTypeArrayMap(String fieldName, String fieldType, Integer arraySize, Integer mapSize, Integer fieldValueLength, List<String> fieldExpMappings) {
        Map<String, Object> result = new HashMap<>(mapSize);
        if (fieldType.endsWith("array-map")) {
            fieldType = fieldType.replace("-map", "");
        }
        for (int i = 0; i < mapSize; i++) {
            var list = generateRandomList(fieldName, fieldType, arraySize, fieldValueLength, fieldExpMappings);
            result.put(generateMapKey(), list);
        }
        return result;
    }

}
