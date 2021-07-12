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

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

public abstract class SchemaProcessorLib {

    private static final Map<String, Object> context = new HashMap<>();

    static boolean checkIfMap(String type) {
        return type.endsWith("map");
    }

    static boolean checkIfArrayMap(String type) {
        return type.endsWith("array-map");
    }

    static boolean checkIfMapArray(String type) {
        return type.endsWith("map-array");
    }

    static boolean checkIfObjectArray(String cleanPath){return cleanPath.contains("].");}

    static FieldValueMapping getSafeGetElement(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
    }

    static Integer calculateSize(String fieldName, String methodName) {
        int arrayLength = RandomUtils.nextInt(1, 10);
        String tempString = fieldName.substring(
                fieldName.lastIndexOf(methodName));
        String arrayLengthStr = StringUtils.substringBetween(tempString, "[", "]");
        if (StringUtils.isNotEmpty(arrayLengthStr) && StringUtils.isNumeric(arrayLengthStr)) {
            arrayLength = Integer.parseInt(arrayLengthStr);
        }
        return arrayLength;
    }

    static String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName) {
        int startPosition = 0;
        String cleanPath;
        if (StringUtils.isNotEmpty(fieldName)) {
            startPosition = fieldValueMapping.getFieldName().indexOf(fieldName) + fieldName.length() + 1;
        }
        cleanPath = fieldValueMapping.getFieldName().substring(startPosition);
        if (cleanPath.matches("^(\\d*]).*$")) {
            cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
        }
        return cleanPath;
    }

    static String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
        String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
        int endOfField = pathToClean.contains(".") ?
                pathToClean.indexOf(".") : pathToClean.contains("[") ? pathToClean.indexOf("[") : pathToClean.length();
        return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
    }

    static String getCleanMethodNameMap(FieldValueMapping fieldValueMapping, String fieldName) {
        String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
        int endOfField = pathToClean.contains("[") ? pathToClean.indexOf("[") : 0;
        return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
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

    static List generateRandomMapArray(String fieldName, String fieldType, int arraySize, int mapSize, List<String> fieldValuesList) {

        List<String> parameterList = new ArrayList<>(fieldValuesList);
        parameterList.replaceAll(fieldValue ->
                fieldValue.matches("\\$\\{\\w*}") ?
                        JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                        fieldValue
        );
        List<Map> value = new ArrayList<>(arraySize);
        while (arraySize > 0) {
            value.add(generateRandomMap(fieldName, fieldType, mapSize, fieldValuesList));
            arraySize--;
        }
        return value;
    }

}
