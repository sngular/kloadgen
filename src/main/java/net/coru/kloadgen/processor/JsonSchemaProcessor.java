/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.StatelessRandomTool;

import java.util.*;

public class JsonSchemaProcessor extends SchemaProcessorLib {

    private static final ObjectMapper mapper = new ObjectMapper();

    private List<FieldValueMapping> fieldExprMappings;

    private StatelessRandomTool randomToolJson;

    public void processSchema(List<FieldValueMapping> fieldExprMappings) {
        this.fieldExprMappings = fieldExprMappings;
        randomToolJson = new StatelessRandomTool();
    }

    @SneakyThrows
    public ObjectNode next() {
        ObjectNode entity = JsonNodeFactory.instance.objectNode();
        if (!fieldExprMappings.isEmpty()) {
            ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
            FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
            while (!fieldExpMappingsQueue.isEmpty()) {
                String fieldName = getCleanMethodName(fieldValueMapping, "");
                if (cleanUpPath(fieldValueMapping, "").contains("[")) {
                    if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map")) {
                        fieldExpMappingsQueue.poll();
                        entity.putPOJO(fieldName, createBasicMap(fieldName, fieldValueMapping.getFieldType(),
                                calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                fieldValueMapping.getValueLength(),
                                fieldValueMapping.getFieldValuesList()));
                    } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
                        fieldExpMappingsQueue.poll();
                        entity.putPOJO(fieldName, createSimpleMapArray(fieldName, fieldValueMapping.getFieldType(),
                                calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                calculateMapSize(fieldValueMapping.getFieldName(), fieldName), fieldValueMapping.getValueLength(),
                                fieldValueMapping.getFieldValuesList()));
                    } else if (cleanUpPath(fieldValueMapping, "").contains("][]")) {
                        entity.putArray(fieldName).addAll(
                                createObjectMap(
                                        fieldName,
                                        calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                        fieldExpMappingsQueue));
                        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                    } else if (isBasicArray(fieldValueMapping.getFieldType())) {
                        fieldExpMappingsQueue.poll();
                        entity
                                .putArray(fieldName)
                                .addAll(createBasicArray(
                                        fieldName,
                                        fieldValueMapping.getFieldType(),
                                        calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                        fieldValueMapping.getValueLength(),
                                        fieldValueMapping.getFieldValuesList()));
                        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                    } else {
                        entity.putArray(fieldName).addAll(
                                createObjectArray(
                                        fieldName,
                                        calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                        fieldExpMappingsQueue));
                        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                    }
                } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {
                    entity.set(fieldName, createObject(fieldName, fieldExpMappingsQueue));
                    fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                } else {
                    entity.putPOJO(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                            mapper.convertValue(
                                    randomToolJson.generateRandom(fieldName,
                                            fieldValueMapping.getFieldType(),
                                            fieldValueMapping.getValueLength(),
                                            fieldValueMapping.getFieldValuesList()), JsonNode.class));
                    fieldExpMappingsQueue.remove();
                    fieldValueMapping = fieldExpMappingsQueue.peek();
                }
            }
        }
        return entity;
    }

    private ObjectNode createObject(final String fieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
            throws KLoadGenException {
        ObjectNode subEntity = JsonNodeFactory.instance.objectNode();
        if (null == subEntity) {
            throw new KLoadGenException("Something Odd just happened");
        }
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        while (!fieldExpMappingsQueue.isEmpty() && Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName)) {
            String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
            if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
                if (fieldValueMapping.getFieldType().endsWith("map")) {
                    fieldExpMappingsQueue.poll();
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    subEntity.putPOJO(fieldNameSubEntity, createBasicMap(fieldName, fieldValueMapping.getFieldType(),
                            calculateSize(fieldValueMapping.getFieldName(), fieldName),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList()));
                } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
                    fieldExpMappingsQueue.poll();
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    subEntity.putPOJO(fieldNameSubEntity, createSimpleMapArray(fieldName, fieldValueMapping.getFieldType(),
                            calculateSize(fieldValueMapping.getFieldName(), fieldName),
                            calculateMapSize(fieldValueMapping.getFieldName(), fieldNameSubEntity), fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList()));
                } else if (cleanUpPath(fieldValueMapping, "").contains("][]")) {
                    subEntity.putArray(fieldName).addAll(
                            createObjectMap(
                                    fieldName,
                                    calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                    fieldExpMappingsQueue));
                } else if (isBasicArray(fieldValueMapping.getFieldType())) {
                    fieldExpMappingsQueue.poll();
                    subEntity
                            .putArray(fieldName)
                            .addAll(createBasicArray(
                                    fieldName,
                                    fieldValueMapping.getFieldType(),
                                    calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                    fieldValueMapping.getValueLength(),
                                    fieldValueMapping.getFieldValuesList()));
                } else {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    subEntity.putArray(fieldNameSubEntity).addAll(
                            createObjectArray(
                                    fieldNameSubEntity,
                                    calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                    fieldExpMappingsQueue));
                }
            } else if (cleanFieldName.contains(".")) {
                String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                subEntity.set(fieldNameSubEntity, createObject(fieldNameSubEntity, fieldExpMappingsQueue));
            } else {
                fieldExpMappingsQueue.poll();
                subEntity.putPOJO(cleanFieldName,
                        mapper.convertValue(
                                randomToolJson.generateRandom(cleanFieldName,
                                        fieldValueMapping.getFieldType(),
                                        fieldValueMapping.getValueLength(),
                                        fieldValueMapping.getFieldValuesList()), JsonNode.class));
            }
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        }
        return subEntity;
    }

    private ArrayNode createObjectMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        ArrayNode objectArray = JsonNodeFactory.instance.arrayNode();

        for (int i = 0; i < calculateSize - 1; i++) {
            ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
            objectArray.add(JsonNodeFactory.instance.objectNode().set(String.valueOf(i), createObject(fieldName, temporalQueue)));
        }
        objectArray.add(JsonNodeFactory.instance.objectNode().set(String.valueOf(calculateSize), createObject(fieldName, fieldExpMappingsQueue)));
        return objectArray;
    }

    private ArrayNode createBasicArray(String fieldName, String fieldType, Integer calculateSize, Integer valueSize, List<String> fieldValuesList) {
        return mapper.convertValue(
                generateRandomList(fieldName,
                        fieldType,
                        calculateSize, valueSize,
                        fieldValuesList
                ), ArrayNode.class);
    }

    private boolean isBasicArray(String fieldType) {
        return fieldType.contains("-");
    }

    private List<ObjectNode> createObjectArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
            throws KLoadGenException {
        List<ObjectNode> objectArray = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize - 1; i++) {
            ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
            objectArray.add(createObject(fieldName, temporalQueue));
        }
        objectArray.add(createObject(fieldName, fieldExpMappingsQueue));
        return objectArray;
    }

    private Map createBasicMap(String fieldName, String fieldType, Integer mapSize, Integer fieldValueLength, List<String> fieldExpMappings)
            throws KLoadGenException {
        return generateRandomMap(fieldName, fieldType, mapSize, fieldValueLength, fieldExpMappings);
    }

    private Map createSimpleMapArray(String fieldName, String fieldType, Integer arraySize, Integer mapSize, Integer valueLength, List<String> fieldExpMappings)
            throws KLoadGenException {
        var result = new HashMap<String, Object>();
        while (mapSize > 0) {
            result.put((String) randomToolJson.generateRandom(fieldName, "string", valueLength, Collections.emptyList()),
                    generateRandomList(fieldName, fieldType, arraySize, valueLength, fieldExpMappings));
        }
        return result;
    }

}
