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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    FieldValueMapping initialFieldValueMapping = null;
    if (!fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
      initialFieldValueMapping = fieldValueMapping;
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
         // fieldExpMappingsQueueCopy.poll();

        } else if((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
        && (generatedProperties == elapsedProperties && generatedProperties == 0) && fieldValueMapping.getParentRequired()
        && (fieldValueMapping.getFieldType().contains("-array-map") || fieldValueMapping.getFieldType().contains("-map-array")
            || fieldValueMapping.getFieldType().contains("-array-array")   || fieldValueMapping.getFieldType().contains("-map-map"))){
          fieldValueMapping.setRequired(true);
        }
        else {
          generatedProperties = 0;
          elapsedProperties = 0;
         // fieldExpMappingsQueueCopy.poll();
        }
        generatedProperties++;

        //Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName);

        if (!fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")
            && (!cleanUpPath(fieldValueMapping, "").contains(".") ||
                (cleanUpPath(fieldValueMapping, "").contains(".") &&
                 cleanUpPath(fieldValueMapping, "").contains("[") ) ||
                cleanUpPath(fieldValueMapping, "").contains("["))){

          elapsedProperties++;
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
          fieldExpMappingsQueueCopy.poll();
        } else {


          String fieldNameProcessed = getFirstPartOfFieldValueMappingName(fieldValueMapping);


          if (fieldNameProcessed.contains("[")) {
            String completeFieldName = getNameObjectCollection(fieldValueMapping.getFieldName());

            if (completeFieldName.contains("].")) {
              operationsObjectCollections(completeFieldName,entity,fieldValueMapping,fieldName,fieldExpMappingsQueue);
            } else {
              operationsCollections(completeFieldName,entity,fieldValueMapping,fieldName,fieldExpMappingsQueue);
            }
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
            fieldExpMappingsQueueCopy.poll();
          } else if (cleanUpPath(fieldValueMapping, "").contains(".")) {

            entity.set(fieldName, createObject(fieldName, fieldExpMappingsQueue));
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
            fieldExpMappingsQueueCopy.poll();
          } else {

            entity.putPOJO(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                           mapper.convertValue(
                               statelessGeneratorTool.generateObject(fieldName,
                                                                     fieldValueMapping.getFieldType(),
                                                                     fieldValueMapping.getValueLength(),
                                                                     fieldValueMapping.getFieldValuesList()), JsonNode.class));
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
            fieldExpMappingsQueueCopy.poll();
          }
        }
      }
    }
    if(entity.isEmpty() && initialFieldValueMapping!=null){
      entity.set(getCleanMethodName(initialFieldValueMapping, ""),JsonNodeFactory.instance.objectNode());
    }
    return entity;
  }

  private boolean checkIfObjectOptional(FieldValueMapping field, String fieldName){

    if(fieldName!= null && !field.getParentRequired() && field.getFieldValuesList().contains("null") && (fieldName.contains("["))){
      return true;
    }

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
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>();
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopyCopy = new ArrayDeque<>();
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while((!fieldExpMappingsQueue.isEmpty() && Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName)) || !fieldExpMappingsQueueCopyCopy.isEmpty()) {
      fieldExpMappingsQueueCopyCopy.poll();
      fieldExpMappingsQueueCopy.poll();
      String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
      generatedProperties++;
      if (checkIfObjectOptional(fieldValueMapping, cleanFieldName)){
        elapsedProperties++;

        FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueueCopy =  new ArrayDeque<>(fieldExpMappingsQueue);
        fieldExpMappingsQueue.remove();
        FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if((nextField == null
            &&
            actualField.getParentRequired()
            &&
            (generatedProperties == elapsedProperties && generatedProperties>0)
        )){
          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          //REVISAR MUY FUERTE
          fieldExpMappingsQueueCopyCopy =  new ArrayDeque<>(fieldExpMappingsQueueCopy);
        }
        else if((nextField == null && (!actualField.getParentRequired() && !actualField.getRequired() ))){
          fieldValueMapping = nextField;
        }else if(nextField == null && !actualField.getRequired()){
          fieldValueMapping = nextField;
        }
        else if ((!Objects.requireNonNull(nextField).getFieldName().contains(fieldName)
            && actualField.getParentRequired()
            && fieldExpMappingsQueue.peek() != null
            && (generatedProperties == elapsedProperties && generatedProperties>0))
        ){
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

          String completeFieldName = getNameObjectCollection(cleanFieldName);

          if (completeFieldName.contains("].")) {
            String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
            operationsObjectCollections(completeFieldName,subEntity,fieldValueMapping,fieldNameSubEntity,fieldExpMappingsQueue);
          } else {
            operationsCollections(completeFieldName,subEntity,fieldValueMapping,fieldName,fieldExpMappingsQueue);
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

  private ObjectNode createObjectMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectArray =  JsonNodeFactory.instance.objectNode();

    for(int i=0; i<calculateSize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.set(statelessGeneratorTool.generateRandomString(i), createObject(fieldName, temporalQueue));
    }
    objectArray.set(statelessGeneratorTool.generateRandomString(calculateSize), createObject(fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private ObjectNode createObjectMapMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectArray =  JsonNodeFactory.instance.objectNode();

    for(int i=0; i<calculateSize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.set(statelessGeneratorTool.generateRandomString(i), createObjectMap(fieldName,calculateSize, temporalQueue));
    }
    objectArray.set(statelessGeneratorTool.generateRandomString(calculateSize), createObjectMap(fieldName,calculateSize, fieldExpMappingsQueue));
    return objectArray;
  }

  private ArrayNode createObjectArrayArray(String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue)
      throws KLoadGenException {
    //ObjectNode subentity = JsonNodeFactory.instance.objectNode();
    List<ObjectNode> objectArray = new ArrayList<>(arraySize);
    ArrayNode subentity = new ArrayNode(JsonNodeFactory.instance);
    ArrayNode subentity2 = JsonNodeFactory.instance.arrayNode();


    for(int i=0; i<arraySize -1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.addAll(createObjectArray(fieldName,2,temporalQueue)); //aqui meter calculateSize
      subentity.addAll(objectArray);
      subentity2.add(subentity);
      objectArray.removeAll(objectArray);
      subentity.removeAll();
    }
    objectArray.addAll(createObjectArray(fieldName,2,fieldExpMappingsQueue));
    subentity.addAll(objectArray);
    subentity2.add(subentity);
    return subentity2;
  }

  private ObjectNode createObjectMapArray(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    ObjectNode objectArray =  JsonNodeFactory.instance.objectNode();
    for(int i=0; i<calculateSize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.putArray(statelessGeneratorTool.generateRandomString(i)).addAll(createObjectArray(fieldName,2,temporalQueue));
    }
    objectArray.putArray(statelessGeneratorTool.generateRandomString(calculateSize)).addAll(createObjectArray(fieldName,2,fieldExpMappingsQueue));
    return objectArray;
  }
  private ArrayNode createObjectArrayMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectMap = JsonNodeFactory.instance.objectNode();
    ArrayNode array = JsonNodeFactory.instance.arrayNode(calculateSize);

    for(int i=0; i<calculateSize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectMap.setAll(createObjectMap(fieldName,2,temporalQueue));
      array.add(objectMap);
      objectMap = JsonNodeFactory.instance.objectNode();
    }

    objectMap.setAll(createObjectMap(fieldName,2,fieldExpMappingsQueue));
    array.add(objectMap);
    return array;
  }

  private ArrayNode createBasicArray(String fieldName, String fieldType, Integer calculateSize, Integer valueSize, List<String> fieldValuesList) {
    return mapper.convertValue(
        statelessGeneratorTool.generateArray(fieldName,fieldType,calculateSize,valueSize,fieldValuesList), ArrayNode.class);
  }

  private Object createBasicArrayMap(String fieldName, String fieldType, Integer calculateSize, Integer valueSize, List<String> fieldValuesList) {
    return statelessGeneratorTool.generateArray(fieldName,fieldType, calculateSize,valueSize,fieldValuesList);
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

  private ArrayNode createBasicMapArray(String fieldType, Integer arraySize, List<String> fieldExpMappings)
      throws KLoadGenException {
    return mapper.convertValue(statelessGeneratorTool.generateMap(fieldType, arraySize, fieldExpMappings, arraySize), ArrayNode.class);
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
    int endOfField = pathToClean.contains(".")?
        pathToClean.indexOf(".") : pathToClean.contains("[") ? pathToClean.indexOf("[") : pathToClean.length();
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }

  private String getNameObjectCollection(String fieldName){
    return fieldName.matches("[\\w\\d]*[\\[\\:*\\]]*\\[\\:*\\]\\.[\\w\\d]*.*") ?
        fieldName.substring(0,fieldName.indexOf(".") + 1) : fieldName;
  }

  private void operationsObjectCollections(String completeFieldName, ObjectNode entity,FieldValueMapping fieldValueMapping,
      String fieldName,ArrayDeque<FieldValueMapping> fieldExpMappingsQueue){
    if(completeFieldName.contains("][")){
      if(completeFieldName.contains("[:][:].")){
        completeFieldName =  completeFieldName.replace("[:][:]." , "");
        entity.putPOJO(completeFieldName.replace("[:][:]." , ""), createObjectMapMap(fieldName,
                                                                                     calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                                                     fieldExpMappingsQueue));
      }else if(completeFieldName.contains("[][].")){
        completeFieldName =  completeFieldName.replace("[][]." , "");
        entity.putArray(completeFieldName).addAll(createObjectArrayArray(fieldName,
                                                                      calculateSize(fieldValueMapping.getFieldName() , fieldName),
                                                                      fieldExpMappingsQueue));
      }else if(completeFieldName.contains("[:][].")){
        completeFieldName =  completeFieldName.replace("[:][]." , "");
        entity.putPOJO(completeFieldName,createObjectMapArray(fieldName,
                                                              calculateSize(fieldValueMapping.getFieldName(), fieldName),
                                                              fieldExpMappingsQueue));
      }else{
        completeFieldName =  completeFieldName.replace("[][:]." , "");
        entity.set(completeFieldName,createObjectArrayMap(completeFieldName,calculateSize(fieldValueMapping.getFieldName(), completeFieldName),fieldExpMappingsQueue));
      }
    } else {
      if(completeFieldName.contains("[:]")){
        completeFieldName =  completeFieldName.replace("[:]." , "");
        entity.putPOJO(completeFieldName, createObjectMap(fieldName,calculateSize(fieldValueMapping.getFieldName() , fieldName), fieldExpMappingsQueue));
      }else {
        completeFieldName =  completeFieldName.replace("[]." , "");
        entity.putArray(completeFieldName).addAll(createObjectArray(fieldName,calculateSize(fieldValueMapping.getFieldName() , fieldName),fieldExpMappingsQueue));
      }
    }
  }

  private void operationsCollections(String completeFieldName, ObjectNode entity,FieldValueMapping fieldValueMapping,
      String fieldName,ArrayDeque<FieldValueMapping> fieldExpMappingsQueue){

     if(Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("array-map")) {
      completeFieldName = completeFieldName.replace("[:][]" , "");
      fieldExpMappingsQueue.poll();
      entity.putPOJO(completeFieldName,createBasicArrayMap(completeFieldName,fieldValueMapping.getFieldType(),
                                                        calculateSize(fieldValueMapping.getFieldName() , completeFieldName),
                                                        fieldValueMapping.getValueLength(),fieldValueMapping.getFieldValuesList()));

    } else if(fieldValueMapping.getFieldType().endsWith("map-array")){
      completeFieldName = completeFieldName.replace("[][:]" , "");
      fieldExpMappingsQueue.poll();
      entity.putArray(completeFieldName).addAll(createBasicMapArray(fieldValueMapping.getFieldType() ,
                                                            calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                            fieldValueMapping.getFieldValuesList()));
    } else if(completeFieldName.contains("[:]")){
      completeFieldName =  completeFieldName.replace(fieldValueMapping.getFieldType().endsWith("map-map") ? "[:][:]" : "[:]", "");
      fieldExpMappingsQueue.poll();
      entity.putPOJO(completeFieldName, createBasicMap(fieldValueMapping.getFieldType() ,
                                                       calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                       fieldValueMapping.getFieldValuesList()));
    } else {
      fieldExpMappingsQueue.poll();
      completeFieldName =  completeFieldName.replace(fieldValueMapping.getFieldType().endsWith("array-array") ? "[][]": "[]" , "");
      entity.putArray(completeFieldName).addAll(createBasicArray(completeFieldName,
                                                                 fieldValueMapping.getFieldType() ,
                                                                 calculateSize(fieldValueMapping.getFieldName() , completeFieldName) ,
                                                                 fieldValueMapping.getValueLength() ,
                                                                 fieldValueMapping.getFieldValuesList()));
    }
  }


  public String getFirstPartOfFieldValueMappingName(FieldValueMapping fieldValueMapping){
    if(fieldValueMapping.getFieldName().contains(".")){
      return fieldValueMapping.getFieldName().substring(0,fieldValueMapping.getFieldName().indexOf("."));
    }else{
      return fieldValueMapping.getFieldName();
    }
  }

}
