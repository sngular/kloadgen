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
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreatorFactory;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class JsonSchemaProcessor {

  private static final ObjectMapper mapper = new ObjectMapper();

  private List<FieldValueMapping> fieldExprMappings;

  private ProcessorObjectCreator objectCreator;

  public void processSchema(List<FieldValueMapping> fieldExprMappings) {
    this.objectCreator = ProcessorObjectCreatorFactory.getInstance(SchemaTypeEnum.JSON, null, null);
    this.fieldExprMappings = fieldExprMappings;
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
            && (generatedProperties == elapsedProperties && generatedProperties > 0) && Objects.requireNonNull(fieldValueMapping).getAncestorRequired()) {
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());

        } else if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
                   && (generatedProperties == elapsedProperties && generatedProperties == 0)
                   && Objects.requireNonNull(fieldValueMapping).getAncestorRequired()
                   && checkIfNestedCollection(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
          fieldValueMapping.setRequired(true);
        } else {
          generatedProperties = 0;
          elapsedProperties = 0;
        }
        generatedProperties++;

        if (this.objectCreator.isOptionalField(null, fieldValueMapping.getRequired(), cleanUpPath(fieldValueMapping, ""), fieldValueMapping.getFieldValuesList())) {
          elapsedProperties++;
          fieldExpMappingsQueueCopy = fieldExpMappingsQueue.clone();
          fieldExpMappingsQueue.remove();
          FieldValueMapping fieldValueMappingCopy = fieldValueMapping;
          fieldValueMapping = fieldExpMappingsQueue.peek();

          if (fieldExpMappingsQueue.isEmpty() && elapsedProperties == generatedProperties && fieldValueMappingCopy.getAncestorRequired()) {
            fieldValueMappingCopy.setRequired(true);
            List<String> temporalFieldValueList = fieldValueMappingCopy.getFieldValuesList();
            temporalFieldValueList.remove("null");
            fieldValueMappingCopy.setFieldValuesList(temporalFieldValueList.toString());
            fieldExpMappingsQueue = fieldExpMappingsQueueCopy.clone();
            fieldValueMapping = fieldValueMappingCopy;

          } else if (fieldExpMappingsQueue != null && !fieldExpMappingsQueue.isEmpty()
                     && !getNameObjectCollection(fieldExpMappingsQueueCopy.peek().getFieldName()).contains(getNameObjectCollection(fieldExpMappingsQueue.peek().getFieldName()))
                     && elapsedProperties == generatedProperties && fieldValueMappingCopy.getAncestorRequired()) {
            fieldValueMapping = fieldExpMappingsQueueCopy.peek();
            fieldValueMapping.setRequired(true);
            fieldExpMappingsQueue = fieldExpMappingsQueueCopy.clone();
          } else {
            fieldExpMappingsQueueCopy = fieldExpMappingsQueue.clone();
          }
          fieldExpMappingsQueueCopy.poll();

        } else {

          String fieldNameProcessed = getFirstPartOfFieldValueMappingName(fieldValueMapping);

          if (fieldNameProcessed.contains("[")) {
            String completeFieldName = getNameObjectCollection(fieldValueMapping.getFieldName());
            if (completeFieldName.contains("].")) {
              if (fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName)) {
                generatedProperties = 0;
                elapsedProperties = 0;
              }
              operationsObjectCollections(completeFieldName, entity, fieldValueMapping, fieldName, fieldExpMappingsQueue);
            } else {
              if (fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName)) {
                generatedProperties = 0;
                elapsedProperties = 0;
              }
              operationsCollections(completeFieldName, entity, fieldValueMapping, fieldName, fieldExpMappingsQueue);
            }
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
            fieldExpMappingsQueueCopy.poll();
          } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {

            ObjectNode createdObject = createObject(fieldName, fieldExpMappingsQueue);
            if (!createdObject.isEmpty()) {
              entity.set(fieldName, createdObject);
            }
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
            fieldExpMappingsQueueCopy.poll();
          } else {

            entity.putPOJO(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                           mapper.convertValue(
                               this.objectCreator.createFinalField(fieldName,
                                                                   fieldValueMapping.getFieldType(),
                                                                   fieldValueMapping.getValueLength(),
                                                                   fieldValueMapping.getFieldValuesList()), JsonNode.class)
                           );
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
            fieldExpMappingsQueueCopy.poll();
          }
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
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>();
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopyCopy = new ArrayDeque<>();
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while ((!fieldExpMappingsQueue.isEmpty() && Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName))
           || !fieldExpMappingsQueueCopyCopy.isEmpty()) {

      fieldExpMappingsQueueCopyCopy.poll();
      fieldExpMappingsQueueCopy.poll();
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      generatedProperties++;

      if (checkIfObjectOptional(fieldValueMapping, cleanFieldName)) {

        elapsedProperties++;
        FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExpMappingsQueue);
        fieldExpMappingsQueue.remove();
        FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if ((nextField == null && Objects.requireNonNull(actualField).getAncestorRequired()
             && (generatedProperties == elapsedProperties && generatedProperties > 0))) {

          if (fieldValueMapping.getFieldName().split(cleanFieldName)[0].endsWith("[].") || fieldValueMapping.getFieldName().split(cleanFieldName)[0].endsWith("[:].")) {
            fieldValueMapping = null;
          } else {
            fieldValueMapping = actualField;
            fieldValueMapping.setRequired(true);
            List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
            temporalFieldValueList.remove("null");
            fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
            fieldExpMappingsQueueCopyCopy = new ArrayDeque<>(fieldExpMappingsQueueCopy);
          }

        } else if (nextField == null
                   && (!actualField.getRequired()
                       || (!actualField.getRequired() && !actualField.getAncestorRequired()))) {
          fieldValueMapping = null;

        } else if (checkIfOptionalCollection(fieldValueMapping, cleanFieldName)) {
          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);

        } else if ((!Objects.requireNonNull(nextField).getFieldName().contains(fieldName)
                    && Objects.requireNonNull(actualField).getAncestorRequired()
                    && fieldExpMappingsQueue.peek() != null
                    && (generatedProperties == elapsedProperties && generatedProperties > 0))) {

          if (fieldValueMapping.getFieldName().contains(".")) {
            String ancestorName = fieldValueMapping.getFieldName().substring(0, fieldValueMapping.getFieldName().indexOf("."));
            if (Objects.requireNonNull(nextField).getFieldName().contains(ancestorName)) {
              fieldValueMapping = nextField;
            } else {
              if (fieldValueMapping.getFieldName().split(cleanFieldName)[0].endsWith("[].") || fieldValueMapping.getFieldName().split(cleanFieldName)[0].endsWith("[:].")) {
                fieldValueMapping = nextField;
              } else {
                fieldValueMapping = actualField;
                fieldValueMapping.setRequired(true);
                List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
                temporalFieldValueList.remove("null");
                fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
              }
            }
          } else {
            fieldValueMapping = actualField;
            fieldValueMapping.setRequired(true);
            List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
            temporalFieldValueList.remove("null");
            fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          }
        } else {
          fieldValueMapping = nextField;

        }
      } else {

        if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {
          String completeFieldName = getNameObjectCollection(cleanFieldName);
          if (completeFieldName.contains("].")) {
            String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
            operationsObjectCollections(completeFieldName, subEntity, fieldValueMapping, fieldNameSubEntity, fieldExpMappingsQueue);
          } else {
            operationsCollections(completeFieldName, subEntity, fieldValueMapping, fieldName, fieldExpMappingsQueue);
          }

        } else if (cleanFieldName.contains(".")) {
          String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
          ObjectNode createdObject = createObject(fieldNameSubEntity, fieldExpMappingsQueue);
          if (!createdObject.isEmpty()) {
            subEntity.set(fieldNameSubEntity, createdObject);
          }
        } else {

          fieldExpMappingsQueue.poll();
          subEntity.putPOJO(cleanFieldName,
                            mapper.convertValue(
                                        this.objectCreator.createFinalField(cleanFieldName,
                                                                 Objects.requireNonNull(fieldValueMapping).getFieldType(),
                                                                 fieldValueMapping.getValueLength(),
                                                                 fieldValueMapping.getFieldValuesList()), JsonNode.class));
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      }
    }

    return subEntity;
  }

  private ObjectNode createObjectMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectArray = JsonNodeFactory.instance.objectNode();

    for (int i = 0; i < calculateSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.set(this.objectCreator.generateRandomString(i), createObject(fieldName, temporalQueue));
    }
    objectArray.set(this.objectCreator.generateRandomString(calculateSize), createObject(fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private ObjectNode createObjectMapMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectArray = JsonNodeFactory.instance.objectNode();

    for (int i = 0; i < calculateSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.set(this.objectCreator.generateRandomString(i), createObjectMap(fieldName, calculateSize, temporalQueue));
    }
    objectArray.set(this.objectCreator.generateRandomString(calculateSize), createObjectMap(fieldName, calculateSize, fieldExpMappingsQueue));
    return objectArray;
  }

  private ArrayNode createObjectArrayArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws KLoadGenException {

    List<ObjectNode> objectArray = new ArrayList<>(arraySize);
    ArrayNode subentity = new ArrayNode(JsonNodeFactory.instance);
    ArrayNode subentity2 = JsonNodeFactory.instance.arrayNode();

    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();

      List<ObjectNode> objectCreated = createObjectArray(fieldName, arraySize, temporalQueue);
      if (objectCreated != null && !objectCreated.isEmpty()) {
        objectArray.addAll(objectCreated);
        subentity.addAll(objectArray);
        subentity2.add(subentity);
        objectArray.removeAll(objectArray);
        subentity.removeAll();
      }

    }

    List<ObjectNode> objCreated = createObjectArray(fieldName, arraySize, fieldExpMappingsQueue);
    if (objCreated != null && !objCreated.isEmpty()) {
      objectArray.addAll(objCreated);
      subentity.addAll(objectArray);
      subentity2.add(subentity);
    }

    return subentity2;
  }

  private ObjectNode createObjectMapArray(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    ObjectNode objectArray = JsonNodeFactory.instance.objectNode();
    for (int i = 0; i < calculateSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.putArray(this.objectCreator.generateRandomString(i)).addAll(createObjectArray(fieldName, 2, temporalQueue));
    }
    objectArray.putArray(this.objectCreator.generateRandomString(calculateSize)).addAll(createObjectArray(fieldName, 2, fieldExpMappingsQueue));
    return objectArray;
  }

  private ArrayNode createObjectArrayMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectMap = JsonNodeFactory.instance.objectNode();
    ArrayNode array = JsonNodeFactory.instance.arrayNode(calculateSize);

    for (int i = 0; i < calculateSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectMap.setAll(createObjectMap(fieldName, calculateSize, temporalQueue));
      array.add(objectMap);
      objectMap = JsonNodeFactory.instance.objectNode();
    }

    objectMap.setAll(createObjectMap(fieldName, calculateSize, fieldExpMappingsQueue));
    array.add(objectMap);
    return array;
  }

  private List<ObjectNode> createObjectArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws KLoadGenException {
    List<ObjectNode> objectArray = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      ObjectNode objectNode = createObject(fieldName, temporalQueue);
      if (objectNode != null && !objectNode.isEmpty()) {
        objectArray.add(objectNode);
      }
    }
    ObjectNode objectNode = createObject(fieldName, fieldExpMappingsQueue);
    if (objectNode != null && !objectNode.isEmpty()) {
      objectArray.add(objectNode);
    }
    return objectArray;
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

    if (cleanPath.matches("^(\\d*:*]).*$")) {
      cleanPath = cleanPath.substring(cleanPath.indexOf(".") + 1);
    }
    return cleanPath;
  }

  private String getCleanMethodName(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains(".") ?
        pathToClean.indexOf(".") : pathToClean.contains("[") ? pathToClean.indexOf("[") : pathToClean.length();
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }

  private String getNameObjectCollection(String fieldName) {
    return fieldName.matches("[\\w\\d]*[\\[\\:*\\]]*\\[\\:*\\]\\.[\\w\\d]*.*") ?
        fieldName.substring(0, fieldName.indexOf(".") + 1) : fieldName;
  }

  private void operationsObjectCollections(
      String completeFieldName, ObjectNode entity, FieldValueMapping fieldValueMapping,
      String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    var finalFieldName = "";
    if (completeFieldName.contains("][")) {
      if (completeFieldName.contains("[:][:].")) {
        finalFieldName = completeFieldName.replace("[:][:].", "");
        entity.putPOJO(finalFieldName,
                       createObjectMapMap(fieldName,
                                                          calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                          fieldExpMappingsQueue));
      } else if (completeFieldName.contains("[][].")) {
        finalFieldName = completeFieldName.replace("[][].", "");
        entity.putArray(finalFieldName).addAll(
                      createObjectArrayArray(fieldName,
                                                                      calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                                      fieldExpMappingsQueue));
      } else if (completeFieldName.contains("[:][].")) {
        finalFieldName = completeFieldName.replace("[:][].", "");
        entity.putPOJO(finalFieldName,
                       createObjectMapArray(fieldName,
                                                            calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                            fieldExpMappingsQueue));
      } else {
        finalFieldName = completeFieldName.replace("[][:].", "");
        entity.set(finalFieldName,
                   createObjectArrayMap(completeFieldName,
                                                        calculateSize(fieldValueMapping.getFieldName(), finalFieldName),
                                                        fieldExpMappingsQueue));
      }
    } else {
      if (completeFieldName.contains("[:]")) {
        finalFieldName = completeFieldName.replace("[:].", "");
        entity.putPOJO(finalFieldName,
                       createObjectMap(fieldName,
                                                       calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                       fieldExpMappingsQueue));
      } else {
        finalFieldName = completeFieldName.replace("[].", "");
        entity.putArray(finalFieldName).addAll(
                        createObjectArray(fieldName,
                                                                 calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                                 fieldExpMappingsQueue));
      }
    }
  }

  private void operationsCollections(
      String completeFieldName, ObjectNode entity, FieldValueMapping fieldValueMapping,
      String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    var finalFieldName = "";
    if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("array-map")) {
      finalFieldName = completeFieldName.replace("[:][]", "");
      fieldExpMappingsQueue.poll();
      entity.putPOJO(finalFieldName,
                     this.objectCreator.createBasicArrayMap(finalFieldName, fieldValueMapping.getFieldType(),
                                                            calculateSize(fieldValueMapping.getFieldName(), finalFieldName),
                                                            fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList()));

    } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
      finalFieldName = completeFieldName.replace("[][:]", "");
      fieldExpMappingsQueue.poll();
      entity.putArray(finalFieldName).addAll(
          this.mapper.convertValue(
              this.objectCreator.createBasicMapArray(fieldValueMapping.getFieldType(),
                                                 calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                 fieldValueMapping.getFieldValuesList(), 0, 0, null), ArrayNode.class));
    } else if (completeFieldName.contains("[:]")) {
      finalFieldName = completeFieldName.replace(fieldValueMapping.getFieldType().endsWith("map-map") ? "[:][:]" : "[:]", "");
      fieldExpMappingsQueue.poll();
      entity.putPOJO(finalFieldName,
          this.objectCreator.createBasicMap(fieldValueMapping.getFieldName(),
                                            fieldValueMapping.getFieldType(),
                                            calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                            null,
                                            fieldValueMapping.getFieldValuesList()));
    } else {
      fieldExpMappingsQueue.poll();
      finalFieldName = completeFieldName.replace(fieldValueMapping.getFieldType().endsWith("array-array") ? "[][]" : "[]", "");
      entity.putArray(finalFieldName).addAll(
          this.mapper.convertValue(
          this.objectCreator.createBasicArray(finalFieldName,
                                              fieldValueMapping.getFieldType(),
                                              calculateSize(fieldValueMapping.getFieldName(), finalFieldName),
                                              fieldValueMapping.getValueLength(),
                                              fieldValueMapping.getFieldValuesList()), ArrayNode.class));
    }
  }

  public String getFirstPartOfFieldValueMappingName(FieldValueMapping fieldValueMapping) {
    if (fieldValueMapping.getFieldName().contains(".")) {
      return fieldValueMapping.getFieldName().substring(0, fieldValueMapping.getFieldName().indexOf("."));
    } else {
      return fieldValueMapping.getFieldName();
    }
  }

  public boolean checkIfNestedCollection(String fieldType) {
    return fieldType.contains("-array-map") || fieldType.contains("-map-array") || fieldType.contains("-array-array") || fieldType.contains("-map-map");
  }

  private boolean checkIfObjectOptional(FieldValueMapping field, String fieldName) {
    if (fieldName != null && !field.getAncestorRequired() && field.getFieldValuesList().contains("null") && (fieldName.contains("["))) {
      return true;
    }
    return fieldName != null ? this.objectCreator.isOptionalField(null, field.getRequired(), fieldName, field.getFieldValuesList()) : this.objectCreator.isOptionalField(null,
                                                                                                                                                                         field.getRequired(),
                                                                                                                                                                        cleanUpPath(field, ""),
                                                                                                                                                                         field.getFieldValuesList());

  }

  private boolean checkIfOptionalCollection(FieldValueMapping field, String nameOfField) {
    return !field.getRequired() && field.getFieldValuesList().contains("null") && (nameOfField.contains("[]") || nameOfField.contains("[:]"));
  }


}
