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

        //Objects.requireNonNull(fieldValueMapping).getFieldName().contains(fieldName);

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

            String nombreCompleto = fieldValueMapping.getFieldName().matches("[\\w\\d]*[\\[\\:*\\]]*\\[\\:*\\]\\.[\\w\\d]*.*") ?
            fieldValueMapping.getFieldName().substring(0,fieldValueMapping.getFieldName().indexOf(".") + 1) : fieldValueMapping.getFieldName();

            //comprobamso si es array/map y objeto, sellamara en todos lo0s casos a createObject
            if (nombreCompleto.contains("].")) {
              if(nombreCompleto.contains("][")){
                //map-map, array-array y combinaciones (4) con objetos
                System.out.println("HAY MAPA/ARRAY CON OBJETO");
                if(nombreCompleto.contains("[:][:].")){
                   nombreCompleto =  nombreCompleto.replace("[:][:]." , "");
                  entity.putPOJO(nombreCompleto,
                                 createObjectMapMap(
                                     fieldName,
                                     calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                     fieldExpMappingsQueue));
                  fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                }



              } else {
                //simple map o array con objeto
                if(nombreCompleto.contains("[:]")){
                 nombreCompleto =  nombreCompleto.replace("[:]." , "");
                  entity.putPOJO(nombreCompleto,
                      createObjectMap(
                          fieldName,
                          calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                          fieldExpMappingsQueue));
                  fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                }else {
                  nombreCompleto =  nombreCompleto.replace("[]." , "");
                  entity.putArray(nombreCompleto).addAll(
                      createObjectArray(
                          fieldName ,
                          calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                          fieldExpMappingsQueue));
                  fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                }

              }

            } else {
              //array/map simple

              if(Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map-map")) {
                nombreCompleto = nombreCompleto.replace("[:][:]" , "");
                fieldExpMappingsQueue.poll();

                entity.putPOJO(nombreCompleto , createBasicMap(fieldValueMapping.getFieldType() ,
                                                               calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                               fieldValueMapping.getFieldValuesList()));

                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);

              } else if(Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("array-map")) {

                nombreCompleto = nombreCompleto.replace("[:][]" , "");
                fieldExpMappingsQueue.poll();

             /*   ObjectNode objectArray =  JsonNodeFactory.instance.objectNode();
                objectArray.set("PEPE",new ObjectMapper().convertValue(createBasicMap(fieldValueMapping.getFieldType() ,
                                                                                      calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                                                      fieldValueMapping.getFieldValuesList()),JsonNode.class));*/

                entity.putPOJO(nombreCompleto,createBasicArray(nombreCompleto,
                                                               fieldValueMapping.getFieldType() ,
                                                               calculateSize(fieldValueMapping.getFieldName() , nombreCompleto) ,
                                                               fieldValueMapping.getValueLength() ,
                                                               fieldValueMapping.getFieldValuesList()));

                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);

              } else if(Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map-array")){
                nombreCompleto = nombreCompleto.replace("[][:]" , "");
                fieldExpMappingsQueue.poll();

                ObjectNode objectArray =  JsonNodeFactory.instance.objectNode();
                objectArray.set("PEPE",new ObjectMapper().convertValue(createBasicMap(fieldValueMapping.getFieldType() ,
                                                                                      calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                                                      fieldValueMapping.getFieldValuesList()),JsonNode.class));

              entity.putArray(nombreCompleto).add(objectArray);

                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              } else if(nombreCompleto.contains("[:]")){
                nombreCompleto =  nombreCompleto.replace("[:]" , "");
                fieldExpMappingsQueue.poll();
                entity.putPOJO(nombreCompleto , createBasicMap(fieldValueMapping.getFieldType() ,
                                                                 calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                                 fieldValueMapping.getFieldValuesList()));
                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              } else {
                fieldExpMappingsQueue.poll();
                nombreCompleto =  nombreCompleto.replace("[]" , "");
                entity.putArray(nombreCompleto).addAll(createBasicArray(nombreCompleto,
                                                  fieldValueMapping.getFieldType() ,
                                                  calculateSize(fieldValueMapping.getFieldName() , nombreCompleto) ,
                                                  fieldValueMapping.getValueLength() ,
                                                  fieldValueMapping.getFieldValuesList()));
                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              }
            }

             /* if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map")) {
                String fieldCleanUpPath = cleanUpPath(fieldValueMapping , "").replace("[:]" , "");
                fieldExpMappingsQueue.poll();
                entity.putPOJO(fieldCleanUpPath , createBasicMap(fieldValueMapping.getFieldType() ,
                                                                 calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                                 fieldValueMapping.getFieldValuesList()));
                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              } else if (fieldValueMapping.getName().contains("[:].")) {

              } else if (fieldValueMapping.getFieldType().endsWith("map-array")) {
                fieldExpMappingsQueue.poll();
                entity.putPOJO(fieldName , createObjectMapArray(fieldValueMapping.getFieldType() ,
                                                                calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                                fieldValueMapping.getValueLength() ,
                                                                fieldValueMapping.getFieldValuesList()));
              } else if (cleanUpPath(fieldValueMapping , "").contains("][]")) {
                entity.putArray(fieldName).addAll(
                    createObjectMap(
                        fieldName ,
                        calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                        fieldExpMappingsQueue));
                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              } else if (isBasicArray(fieldValueMapping.getFieldType())) {
                fieldExpMappingsQueue.poll();
                entity
                    .putArray(fieldName)
                    .addAll(createBasicArray(
                        fieldName ,
                        fieldValueMapping.getFieldType() ,
                        calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                        fieldValueMapping.getValueLength() ,
                        fieldValueMapping.getFieldValuesList()));
                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              } else {
                entity.putArray(fieldName).addAll(
                    createObjectArray(
                        fieldName ,
                        calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                        fieldExpMappingsQueue));
                fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
              }
            */
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
          //fieldExpMappingsQueue.remove();
          fieldValueMapping = nextField;
        }
      } else {
        if (cleanFieldName.matches("[\\w\\d]+\\[.*")) {


          String nombreCompleto = cleanFieldName.matches("[\\w\\d]*[\\[\\:*\\]]*\\[\\:*\\]\\.[\\w\\d]*.*") ?
              cleanFieldName.substring(0,cleanFieldName.indexOf(".") + 1) : cleanFieldName;


          //comprobamso si es array/map y objeto, sellamara en todos lo0s casos a createObject
            if (nombreCompleto.contains("].")) {
              if(nombreCompleto.contains("][")){
                //map-map, array-array y combinaciones (4) con objetos
                System.out.println("HAY MAPA/ARRAY CON OBJETO");
              } else {
                //simple map o array con objeto
                if(nombreCompleto.contains("[:]")){
                  nombreCompleto.replace("[:]." , "");
                  subEntity.putPOJO(nombreCompleto,
                      createObjectMap(
                          fieldName,
                          calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                          fieldExpMappingsQueue));

                }else {
                  subEntity.putArray(fieldName).addAll(
                      createObjectArray(
                          fieldName ,
                          calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                          fieldExpMappingsQueue));

                }

              }

            } else {
              if(nombreCompleto.contains("[:]")){
                nombreCompleto.replace("[:]" , "");
                fieldExpMappingsQueue.poll();
                subEntity.putPOJO(nombreCompleto , createBasicMap(fieldValueMapping.getFieldType() ,
                                                                 calculateSize(fieldValueMapping.getFieldName() , fieldName) ,
                                                                 fieldValueMapping.getFieldValuesList()));

              } else {
                fieldExpMappingsQueue.poll();
                nombreCompleto = nombreCompleto.replace("[]" , "");
                subEntity.putArray(nombreCompleto).addAll(createBasicArray(nombreCompleto,
                                                                   fieldValueMapping.getFieldType() ,
                                                                   calculateSize(fieldValueMapping.getFieldName() , nombreCompleto) ,
                                                                   fieldValueMapping.getValueLength() ,
                                                                   fieldValueMapping.getFieldValuesList()));

              }
            }


         /* if (Objects.requireNonNull(fieldValueMapping).getFieldType().endsWith("map")) {
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
            String fieldNameSubEntity = getCleanMethodNameMap(fieldValueMapping, fieldName);
            subEntity
                    .putArray(fieldNameSubEntity)
                    .addAll(createBasicArray(
                        fieldNameSubEntity,
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
          }*/
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
      objectArray.set(String.valueOf(i), createObject(fieldName, temporalQueue));
    }
    objectArray.set(String.valueOf(calculateSize), createObject(fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private ObjectNode createObjectMapMap(String fieldName, Integer calculateSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    ObjectNode objectArray =  JsonNodeFactory.instance.objectNode();

    for(int i=0; i<calculateSize-1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.set(String.valueOf(i), createObjectMap(fieldName,calculateSize, temporalQueue));
    }
    objectArray.set(String.valueOf(calculateSize), createObjectMap(fieldName,calculateSize, fieldExpMappingsQueue));
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

  private String getCleanMethodNameMap(FieldValueMapping fieldValueMapping, String fieldName) {
    String pathToClean = cleanUpPath(fieldValueMapping, fieldName);
    int endOfField = pathToClean.contains("[")?
        pathToClean.indexOf("[") : 0;
    return pathToClean.substring(0, endOfField).replaceAll("\\[[0-9]*]", "");
  }


}
