/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.impl.AvroObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;

public class SchemaProcessor extends SchemaProcessorLib {

  private List<FieldValueMapping> fieldExprMappings;

  private ObjectCreator objectCreator;

  public void processSchema(Object schema, Object metadata, List<FieldValueMapping> fieldExprMappings) {
    this.objectCreator = new AvroObjectCreatorFactory(schema, metadata);
    this.fieldExprMappings = fieldExprMappings;
  }

  public Object next() {
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      /*int generatedProperties = 0;
      int elapsedProperties = 0;*/
      objectCreator.createRecord("root");
      while (!fieldExpMappingsQueue.isEmpty()) {
        int level = 0;
        String cleanPath = cleanUpPath(fieldValueMapping, "", level);
        String fieldName = getCleanMethodName(fieldValueMapping, "", level);
        String completeFieldName = getFullMethodName(fieldValueMapping, "", level);
        String completeTypeFilterChain = getTypeFilter(fieldName, cleanPath);

        /*if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
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
        } else {*/
          String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);
          if (isTypeFilterMap(singleTypeFilter)) {
            this.objectCreator.createMap("root", fieldExpMappingsQueue, fieldName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                         completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll(
                                             "\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                         fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(), level, getMapAndArrayGenerationFunction(), true);
          } else if (isTypeFilterArray(singleTypeFilter)) {
            this.objectCreator.createArray("root", fieldExpMappingsQueue, fieldName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                           completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                           fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(), level, getMapAndArrayGenerationFunction(), true);
          } else if (isTypeFilterRecord(singleTypeFilter)) {
            createObject(fieldName, fieldExpMappingsQueue, level);
            this.objectCreator.assignRecord("root", fieldName, fieldName);
          } else {
            fieldExpMappingsQueue.remove();
            Object objectResult = this.objectCreator.createRepeatedObject(fieldName, fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(),
                                                                          fieldValueMapping.getValueLength(),
                                                                          fieldValueMapping.getFieldValuesList());
            this.objectCreator.assignObject("root", fieldName, objectResult);
          }
          fieldValueMapping = fieldExpMappingsQueue.peek();
        //}
      }
    }
    return this.objectCreator.generateRecord();
  }

  private Object createObject(String rootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int level) {
    objectCreator.createRecord(rootFieldName);

    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    /*int generatedProperties = 0;
    int elapsedProperties = 0;*/
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
      String completeTypeFilterChain = getTypeFilter(fieldNameSubEntity, cleanPath);

      //generatedProperties++;

      /*if (objectCreator.isOptional(rootFieldName, fieldNameSubEntity) && fieldValueMapping.getFieldValuesList().contains("null")) {

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

      } else {*/
        String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);

        if (isTypeFilterMap(singleTypeFilter)) {
          objectRecord = this.objectCreator.createMap(rootFieldName, fieldExpMappingsQueue, fieldNameSubEntity, completeFieldName,
                                                      calculateSizeFromTypeFilter(singleTypeFilter),
                                                      completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                                      fieldValueMapping.getValueLength(),
                                                      fieldValueMapping.getFieldValuesList(), levelCount, getMapAndArrayGenerationFunction(), true);

        } else if (isTypeFilterArray(singleTypeFilter)) {
          objectRecord = this.objectCreator.createArray(rootFieldName, fieldExpMappingsQueue, fieldNameSubEntity, completeFieldName,
                                                        calculateSizeFromTypeFilter(singleTypeFilter),
                                                        completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), fieldValueMapping.getFieldType(),
                                                        fieldValueMapping.getValueLength(),
                                                        fieldValueMapping.getFieldValuesList(), levelCount, getMapAndArrayGenerationFunction(), true);
        } else if (isTypeFilterRecord(singleTypeFilter)) {
          objectRecord = createObject(fieldNameSubEntity, fieldExpMappingsQueue, levelCount);
          this.objectCreator.assignRecord(rootFieldName, fieldNameSubEntity, fieldNameSubEntity);
        } else {
          fieldExpMappingsQueue.remove();
          objectRecord = this.objectCreator.createRepeatedObject(fieldNameSubEntity, fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(),
                                                                 fieldValueMapping.getValueLength(),
                                                                 fieldValueMapping.getFieldValuesList());
          objectRecord = this.objectCreator.assignObject(rootFieldName, fieldNameSubEntity, objectRecord);
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      //}
    }
    return objectRecord;
  }

  private BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> getMapAndArrayGenerationFunction() {
    BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generationFunction = (fieldExpMappingsQueue, generationFunctionPOJO) -> {

      Object returnObject;
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
        returnObject = this.objectCreator.createRepeatedObject(generationFunctionPOJO.getFieldNameSubEntity(), generationFunctionPOJO.getCompleteFieldName(),
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
      returnObject = this.objectCreator.createMap(objectName, fieldExpMappingsQueue, subfieldCleanName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                                  completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), valueType, valueLength, fieldValuesList,
                                                  level,
                                                  getMapAndArrayGenerationFunction(), false);
    } else {
      returnObject = this.objectCreator.createArray(objectName, fieldExpMappingsQueue, subfieldCleanName, completeFieldName, calculateSizeFromTypeFilter(singleTypeFilter),
                                                    completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""), valueType, valueLength,
                                                    fieldValuesList, level,
                                                    getMapAndArrayGenerationFunction(), false);
    }
    return returnObject;
  }
}
