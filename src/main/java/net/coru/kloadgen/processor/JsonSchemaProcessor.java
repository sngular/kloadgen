/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class JsonSchemaProcessor {

  private static final ObjectMapper mapper = new ObjectMapper();

  private List<FieldValueMapping> fieldExprMappings;

  private StatelessGeneratorTool statelessGeneratorTool;

  public void processSchema(List<FieldValueMapping> fieldExprMappings) {
    this.fieldExprMappings = fieldExprMappings;
    statelessGeneratorTool = new StatelessGeneratorTool();
  }

  @SneakyThrows
  public ObjectNode next() {
    ObjectNode entity = JsonNodeFactory.instance.objectNode();
    if (!fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;

      while (!fieldExpMappingsQueue.isEmpty()) {
        String fieldName = getCleanMethodName(fieldValueMapping, "");

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
                && (generatedProperties == elapsedProperties && generatedProperties > 0) && fieldValueMapping.getParentRequired()){
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          fieldExpMappingsQueueCopy.poll();

        } else {
         generatedProperties = 0;
         elapsedProperties = 0;
         fieldExpMappingsQueueCopy.poll();
        }
         generatedProperties++;

        if (!fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")
        && (!cleanUpPath(fieldValueMapping, "").contains(".") ||
                (cleanUpPath(fieldValueMapping, "").contains(".") &&
                cleanUpPath(fieldValueMapping, "").contains("[") ) ||
                cleanUpPath(fieldValueMapping, "").contains("["))){

          elapsedProperties++;
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        } else {
          if (cleanUpPath(fieldValueMapping, "").contains("[")) {
            if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map")) {
              String fieldCleanUpPath = cleanUpPath(fieldValueMapping, fieldName).replace("[]","");
              fieldExpMappingsQueue.poll();
              entity.putPOJO(fieldCleanUpPath, createBasicMap(fieldValueMapping.getFieldType(),
                      calculateSize(fieldValueMapping.getFieldName(), fieldName),
                      fieldValueMapping.getFieldValuesList()));
            } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
              fieldExpMappingsQueue.poll();
              entity.putPOJO(fieldName, createObjectMapArray(fieldValueMapping.getFieldType(),
                      calculateSize(fieldValueMapping.getFieldName(), fieldName),
                      fieldValueMapping.getValueLength(),
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
                            statelessGeneratorTool.generateObject(fieldName,
                                    fieldValueMapping.getFieldType(),
                                    fieldValueMapping.getValueLength(),
                                    fieldValueMapping.getFieldValuesList()), JsonNode.class));
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
          }
        }
      }
    }
    return entity;
  }

  private boolean checkIfObjectOptional(FieldValueMapping field, String fieldName){
    if (fieldName != null){
      return !field.getRequired() && field.getFieldValuesList().contains("null")
              && (!fieldName.contains(".") ||
              (fieldName.contains(".") && fieldName.contains("[")) ||
              fieldName.contains("["));
    }
    return !field.getRequired() && field.getFieldValuesList().contains("null")
            && (!cleanUpPath(field, "").contains(".") ||
            (cleanUpPath(field, "").contains(".") && cleanUpPath(field, "").contains("[")) ||
            cleanUpPath(field, "").contains("["));
  }

  private ObjectNode createObject(final String fieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
          throws KLoadGenException {
    ObjectNode subEntity = JsonNodeFactory.instance.objectNode();
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while(!fieldExpMappingsQueue.isEmpty() && Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName)) {
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      generatedProperties++;
      if (checkIfObjectOptional(fieldValueMapping, cleanFieldName)){
        elapsedProperties++;

        FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (!Objects.requireNonNull(nextField).getFieldName().contains(fieldName)
                && actualField.getParentRequired()
                && fieldExpMappingsQueue.peek() != null
                && (generatedProperties == elapsedProperties && generatedProperties>0)){
          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
        } else{
          fieldValueMapping = nextField;
        }
      } else {
        if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
          if (fieldValueMapping.getFieldType().endsWith("map")) {
            fieldExpMappingsQueue.poll();
            String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
            subEntity.putPOJO(fieldNameSubEntity, createBasicMap(fieldValueMapping.getFieldType(),
                    calculateSize(fieldValueMapping.getFieldName(), fieldName),
                    fieldValueMapping.getFieldValuesList()));
          } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
            fieldExpMappingsQueue.poll();
            String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
            subEntity.putPOJO(fieldNameSubEntity, createObjectMapArray(fieldValueMapping.getFieldType(),
                    calculateSize(fieldValueMapping.getFieldName(), fieldName),
                    fieldValueMapping.getValueLength(),
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
                          statelessGeneratorTool.generateObject(cleanFieldName,
                                  fieldValueMapping.getFieldType(),
                                  fieldValueMapping.getValueLength(),
                                  fieldValueMapping.getFieldValuesList()), JsonNode.class));
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return subEntity;
  }

  private ArrayNode createObjectMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
   ArrayNode objectArray = JsonNodeFactory.instance.arrayNode();

    for(int i=0; i<calculateSize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(JsonNodeFactory.instance.objectNode().set(String.valueOf(i), createObject(fieldName, temporalQueue)));
    }
    objectArray.add(JsonNodeFactory.instance.objectNode().set(String.valueOf(calculateSize), createObject(fieldName, fieldExpMappingsQueue)));
    return objectArray;
  }

  private ArrayNode createBasicArray(String fieldName, String fieldType, Integer calculateSize, Integer valueSize, List<String> fieldValuesList) {
    return mapper.convertValue(
              statelessGeneratorTool.generateArray(fieldName,
                      fieldType,
                      calculateSize,
                      valueSize,
                      fieldValuesList), ArrayNode.class);
  }

  private boolean isBasicArray(String fieldType) {
    return fieldType.contains("-");
  }

  private List<ObjectNode> createObjectArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws KLoadGenException {
    List<ObjectNode> objectArray = new ArrayList<>(arraySize);
    for(int i=0; i<arraySize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(fieldName, temporalQueue));
    }
    objectArray.add(createObject(fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private Object createBasicMap(String fieldType, Integer arraySize, List<String> fieldExpMappings)
  throws KLoadGenException {
    return statelessGeneratorTool.generateMap(fieldType, arraySize, fieldExpMappings, arraySize);
  }

  private Object createObjectMapArray(String fieldType, Integer arraySize, Integer mapSize, List<String> fieldExpMappings)
      throws KLoadGenException {
    return statelessGeneratorTool.generateMap(fieldType, mapSize, fieldExpMappings, arraySize);
  }

  private Integer calculateSize(String fieldName, String methodName) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String tempString = fieldName.substring(
        fieldName.lastIndexOf(methodName));
    String arrayLengthStr = StringUtils.substringBetween(tempString, "[", "]");
    if (StringUtils.isNotEmpty(arrayLengthStr) && StringUtils.isNumeric(arrayLengthStr)) {
      arrayLength = Integer.parseInt(arrayLengthStr);
    }
    return arrayLength;
  }

  private FieldValueMapping getSafeGetElement(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  private String cleanUpPath(FieldValueMapping fieldValueMapping, String fieldName) {
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

  private String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains(".")?
        pathToClean.indexOf(".") : pathToClean.contains("[") ? pathToClean.indexOf("[") : pathToClean.length();
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }

  private String getCleanMethodNameMap(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains("[")?
        pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }
}
