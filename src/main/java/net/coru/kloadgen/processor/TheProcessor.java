/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ObjectFactory;
import net.coru.kloadgen.processor.objectcreator.impl.AvroFactory;
import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class TheProcessor extends SchemaProcessorLib {

  private List<FieldValueMapping> fieldExprMappings;

  private ObjectFactory objectFactory;

  public void processSchema(Object schema, Object metadata, List<FieldValueMapping> fieldExprMappings) {
    this.objectFactory = new AvroFactory(schema, metadata);
    this.fieldExprMappings = fieldExprMappings;
  }

  public Object next() {
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;
      objectFactory.createRecord("root");
      while (!fieldExpMappingsQueue.isEmpty()) {
        int level = 0;
        String cleanPath = cleanUpPath(fieldValueMapping, "", level);
        String fieldName = getCleanMethodName(fieldValueMapping, "", level);
        String completeFieldName = getFullMethodName(fieldValueMapping, "", level);
        String completeTypeFilterChain = getTypeFilter(fieldName, cleanPath);

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
            && (generatedProperties == elapsedProperties && generatedProperties > 0) && fieldValueMapping.getAncestorRequired()) {
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

        if (fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        } else {
          String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);
          if (isTypeFilterMap(singleTypeFilter)) {
            this.objectFactory.createMap("root", fieldExpMappingsQueue, fieldName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                         completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll(
                                             "\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                         fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(), level, getMapAndArrayGenerationFunction(), true);
          } else if (isTypeFilterArray(singleTypeFilter)) {
            this.objectFactory.createArray("root", fieldExpMappingsQueue, fieldName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                           completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                           fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(), level, getMapAndArrayGenerationFunction(), true);
          } else if (isTypeFilterRecord(singleTypeFilter)) {
            Object objectRecord = createObject(fieldName, fieldExpMappingsQueue, level);
            this.objectFactory.assignObject("root", fieldName, objectRecord);
          } else {
            fieldExpMappingsQueue.remove();
            Object objectResult = this.objectFactory.createRepeatedObject("root", fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(),
                                                                      fieldValueMapping.getValueLength(),
                                                    fieldValueMapping.getFieldValuesList());
            this.objectFactory.assignObject("root", fieldName, objectResult);
          }
          fieldValueMapping = fieldExpMappingsQueue.peek();
        }
      }
    }
    return this.objectFactory.generateRecord();
  }

  private Object createObject(String rootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int level) {
    objectFactory.createRecord(rootFieldName);

    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;
    int levelCount = level + 1;

    Object objectRecord = new Object();

    while (!fieldExpMappingsQueue.isEmpty()
           && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + rootFieldName + "$")
               || fieldValueMapping.getFieldName().matches(rootFieldName + "\\..*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\[.*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\..*"))) {
      String cleanPath = cleanUpPath(fieldValueMapping, rootFieldName, levelCount);
      String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, rootFieldName, levelCount);
      String completeFieldName = getFullMethodName(fieldValueMapping, rootFieldName, levelCount);
      //String completeTypeFilterChain = cleanPath.replaceAll(fieldNameSubEntity, "");
      String completeTypeFilterChain = getTypeFilter(fieldNameSubEntity, cleanPath);

      generatedProperties++;

      if (objectFactory.isOptional(rootFieldName, fieldNameSubEntity) && fieldValueMapping.getFieldValuesList().contains("null")) {

        elapsedProperties++;
        FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (((fieldExpMappingsQueue.peek() != null && !Objects.requireNonNull(nextField).getFieldName().contains(rootFieldName))
             || fieldExpMappingsQueue.peek() == null)
            && actualField.getAncestorRequired()
            && (generatedProperties == elapsedProperties && generatedProperties > 0)) {

          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          if (fieldExpMappingsQueue.peek() == null) {
            fieldExpMappingsQueue.add(fieldValueMapping);
          }
        } else {
          fieldValueMapping = nextField;
        }

      } else {
        String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);

        if (isTypeFilterMap(singleTypeFilter)) {
          objectRecord = this.objectFactory.createMap(rootFieldName, fieldExpMappingsQueue, fieldNameSubEntity, completeFieldName,
                                                      calculateSizeFromTypeFilter(singleTypeFilter),
                                                      completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                                      fieldValueMapping.getValueLength(),
                                                      fieldValueMapping.getFieldValuesList(), levelCount, getMapAndArrayGenerationFunction(), true);

        } else if (isTypeFilterArray(singleTypeFilter)) {
          objectRecord = this.objectFactory.createArray(rootFieldName, fieldExpMappingsQueue, fieldNameSubEntity, completeFieldName,
                                                        calculateSizeFromTypeFilter(singleTypeFilter),
                                                        completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                                        fieldValueMapping.getValueLength(),
                                                        fieldValueMapping.getFieldValuesList(), levelCount, getMapAndArrayGenerationFunction(), true);
        } else if (isTypeFilterRecord(singleTypeFilter)) {
          objectRecord = createObject(fieldNameSubEntity, fieldExpMappingsQueue, levelCount);
          this.objectFactory.assignObject(rootFieldName, fieldNameSubEntity, objectRecord);
        } else {
          fieldExpMappingsQueue.remove();
          objectRecord = this.objectFactory.createRepeatedObject(rootFieldName, fieldNameSubEntity, fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(),
                                                                 fieldValueMapping.getFieldValuesList());
          objectRecord = this.objectFactory.assignObject(rootFieldName, fieldNameSubEntity, objectRecord);
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return objectRecord;
  }

  private BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> getMapAndArrayGenerationFunction() {
    BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generationFunction = (fieldExpMappingsQueue, generationFunctionPOJO) -> {

      Object returnObject = null;
      String singleTypeFilter = getFirstComplexType(generationFunctionPOJO.getCompleteTypeFilterChain());

      if (hasMapOrArrayTypeFilter(generationFunctionPOJO.getCompleteTypeFilterChain()) && (isTypeFilterMap(singleTypeFilter) || isTypeFilterArray(singleTypeFilter))) {
        returnObject = processComplexTypes(generationFunctionPOJO.getRootFieldName(), fieldExpMappingsQueue,
                                           generationFunctionPOJO.getFieldNameSubEntity(), generationFunctionPOJO.getCompleteFieldName(),
                                           generationFunctionPOJO.getCompleteTypeFilterChain(),
                                           generationFunctionPOJO.getValueType(),
                                           generationFunctionPOJO.getValueLength(),
                                           generationFunctionPOJO.getFieldValuesList(),
                                           generationFunctionPOJO.getLevel());
      } else if (isTypeFilterRecord(singleTypeFilter)) {
          returnObject = createObject(generationFunctionPOJO.getFieldNameSubEntity(), (ArrayDeque<FieldValueMapping>)fieldExpMappingsQueue,
                                      generationFunctionPOJO.getLevel());
      } else {
        fieldExpMappingsQueue.remove();
        returnObject = this.objectFactory.createRepeatedObject(generationFunctionPOJO.getRootFieldName(), generationFunctionPOJO.getFieldNameSubEntity(),
                                                               generationFunctionPOJO.getValueType()
            , generationFunctionPOJO.getValueLength(), generationFunctionPOJO.getFieldValuesList());
      }

      return returnObject;
    };

    return generationFunction;
  }

  private Object processComplexTypes(
      String objectName, ArrayDeque<?> fieldExpMappingsQueue, String fieldNameSubEntity, String completeFieldName, String completeTypeFilterChain,
      String valueType, Integer valueLength, List<String> fieldValuesList, final int level) {
    Object returnObject;
    String subfieldCleanName = fieldNameSubEntity;
    String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);

    if (isTypeFilterMap(singleTypeFilter)) {
      returnObject = this.objectFactory.createMap(objectName, fieldExpMappingsQueue, subfieldCleanName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                                  completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), valueType, valueLength, fieldValuesList,
                                                  level,
                                                  getMapAndArrayGenerationFunction(), false);
    } else {
      returnObject = this.objectFactory.createArray(objectName, fieldExpMappingsQueue, subfieldCleanName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                                    completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), valueType, valueLength,
                                                    fieldValuesList, level,
                                                    getMapAndArrayGenerationFunction(), false);
    }
    return returnObject;
  }

  private boolean isTypeFilterMap(String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[[1-9]*:]");
  }

  private boolean isTypeFilterArray(String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[\\d*]");
  }

  private Integer calculateSizeFromTypeFilter(String singleTypeFilter) {
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

  private boolean hasMapOrArrayTypeFilter(String typeFilter) {
    return typeFilter.matches("\\[.*].*");
  }

  private String getFirstComplexType(String completeTypeFilterChain) {
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

  private boolean isTypeFilterRecord(String singleTypeFilter) {
    return singleTypeFilter.startsWith(".");
  }


  /*private List<GenericRecord> createObjectArray(Schema subSchema, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, fieldName, temporalQueue));
    }
    objectArray.add(createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private Map<String, GenericRecord> createObjectMap(Schema subSchema, String fieldName, Integer mapSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    Map<String, GenericRecord> objectMap = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectMap.put(generateMapKey(), createObject(subSchema, fieldName, temporalQueue));
    }
    objectMap.put(generateMapKey(), createObject(subSchema, fieldName, fieldExpMappingsQueue));
    return objectMap;
  }*/

}
