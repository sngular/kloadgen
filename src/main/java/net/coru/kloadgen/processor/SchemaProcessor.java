/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.ObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;

public class SchemaProcessor extends SchemaProcessorLib {

  private List<FieldValueMapping> fieldExprMappings;

  private ObjectCreator objectCreator;

  public void processSchema(SchemaTypeEnum schemaType, Object schema, Object metadata, List<FieldValueMapping> fieldExprMappings) {
    this.objectCreator = ObjectCreatorFactory.getInstance(schemaType, schema, metadata);
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
      objectCreator.createRecord("root");
      while (!fieldExpMappingsQueue.isEmpty()) {
        int level = 0;
        String cleanPath = cleanUpPath(fieldValueMapping, level);
        String fieldName = getCleanMethodName(fieldValueMapping, level);
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

        if (this.objectCreator.isOptional("root", fieldName) && !Objects.requireNonNull(fieldValueMapping).getRequired() && fieldValueMapping.getFieldValuesList().contains(
            "null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
        } else {

          String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);
          String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
          boolean lastTypeFilterOfLastElement = isLastTypeFilterOfLastElement(remainingFilterChain);
          SchemaProcessorPOJO pojo = new SchemaProcessorPOJO("root", fieldExpMappingsQueue, fieldName, fieldValueMapping.getFieldName(), remainingFilterChain,
                                                             calculateSizeFromTypeFilter(singleTypeFilter), fieldValueMapping.getFieldType(),
                                                             fieldValueMapping.getValueLength(),
                                                             fieldValueMapping.getFieldValuesList(), fieldValueMapping.getConstraints(), level,
                                                             lastTypeFilterOfLastElement);

          if (isTypeFilterMap(singleTypeFilter)) {
            this.objectCreator.createMap(pojo, getMapAndArrayGenerationFunction(), true);
            if (lastTypeFilterOfLastElement) {
              fieldExpMappingsQueue.remove();
            }
          } else if (isTypeFilterArray(singleTypeFilter)) {
            this.objectCreator.createArray(pojo, getMapAndArrayGenerationFunction(), true);
            if (lastTypeFilterOfLastElement) {
              fieldExpMappingsQueue.remove();
            }
          } else if (isTypeFilterRecord(singleTypeFilter)) {
            createObject(fieldName, fieldExpMappingsQueue, level);
            this.objectCreator.assignRecord("root", fieldName, fieldName);
          } else {
            fieldExpMappingsQueue.remove();
            this.objectCreator.createValueObject(pojo);
          }
        }
        fieldValueMapping = fieldExpMappingsQueue.peek();
      }
    }
    return this.objectCreator.generateRecord();
  }

  private Object createObject(String rootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int levelCount) {
    objectCreator.createRecord(rootFieldName);

    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;
    int level = levelCount + 1;

    Object objectRecord = new Object();

    while (!fieldExpMappingsQueue.isEmpty() && isNewFieldSharingRootFieldName(level - 1, fieldValueMapping, rootFieldName)) {
      String cleanPath = cleanUpPath(fieldValueMapping, level);
      String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, level);
      String completeTypeFilterChain = getTypeFilter(fieldNameSubEntity, cleanPath);

      generatedProperties++;

      if (objectCreator.isOptional(rootFieldName, fieldNameSubEntity) && fieldValueMapping.getFieldValuesList().contains("null")) {

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
        String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
        boolean lastTypeFilterOfLastElement = isLastTypeFilterOfLastElement(remainingFilterChain);

        SchemaProcessorPOJO pojo = new SchemaProcessorPOJO(rootFieldName, fieldExpMappingsQueue, fieldNameSubEntity, fieldValueMapping.getFieldName(),
                                                           remainingFilterChain,
                                                           calculateSizeFromTypeFilter(singleTypeFilter),
                                                           fieldValueMapping.getFieldType(),
                                                           fieldValueMapping.getValueLength(),
                                                           fieldValueMapping.getFieldValuesList(), fieldValueMapping.getConstraints(), level,
                                                           lastTypeFilterOfLastElement);
        if (isTypeFilterMap(singleTypeFilter)) {
          objectRecord = this.objectCreator.createMap(pojo, getMapAndArrayGenerationFunction(), true);
          if (lastTypeFilterOfLastElement) {
            fieldExpMappingsQueue.remove();
          }
        } else if (isTypeFilterArray(singleTypeFilter)) {
          objectRecord = this.objectCreator.createArray(pojo, getMapAndArrayGenerationFunction(), true);
          if (lastTypeFilterOfLastElement) {
            fieldExpMappingsQueue.remove();
          }
        } else if (isTypeFilterRecord(singleTypeFilter)) {
          objectRecord = createObject(fieldNameSubEntity, fieldExpMappingsQueue, level);
          this.objectCreator.assignRecord(rootFieldName, fieldNameSubEntity, fieldNameSubEntity);
        } else {
          fieldExpMappingsQueue.remove();
          objectRecord = this.objectCreator.createValueObject(pojo);
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return this.objectCreator.generateSubentityRecord(objectRecord);
  }

  private BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> getMapAndArrayGenerationFunction() {
    BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generationFunction = (fieldExpMappingsQueue, schemaProcessorPOJO) -> {

      Object returnObject;
      String singleTypeFilter = getFirstComplexType(schemaProcessorPOJO.getCompleteTypeFilterChain());

      if (hasMapOrArrayTypeFilter(schemaProcessorPOJO.getCompleteTypeFilterChain()) && (isTypeFilterMap(singleTypeFilter) || isTypeFilterArray(singleTypeFilter))) {
        returnObject = processComplexTypes(schemaProcessorPOJO.getRootFieldName(), fieldExpMappingsQueue,
                                           schemaProcessorPOJO.getFieldNameSubEntity(), schemaProcessorPOJO.getCompleteFieldName(),
                                           schemaProcessorPOJO.getCompleteTypeFilterChain(),
                                           schemaProcessorPOJO.getValueType(),
                                           schemaProcessorPOJO.getValueLength(),
                                           schemaProcessorPOJO.getFieldValuesList(),
                                           schemaProcessorPOJO.getConstraints(),
                                           schemaProcessorPOJO.getLevel());
      } else if (isTypeFilterRecord(singleTypeFilter)) {

        returnObject = createObject(schemaProcessorPOJO.getFieldNameSubEntity(), (ArrayDeque<FieldValueMapping>) fieldExpMappingsQueue,
                                    schemaProcessorPOJO.getLevel());
      } else {
        fieldExpMappingsQueue.remove();
        returnObject = this.objectCreator.createValueObject(schemaProcessorPOJO);
      }

      return returnObject;
    };

    return generationFunction;
  }

  private Object processComplexTypes(
      String objectName, ArrayDeque<?> fieldExpMappingsQueue, String fieldNameSubEntity, String completeFieldName, String completeTypeFilterChain,
      String valueType, Integer valueLength, List<String> fieldValuesList, final Map<ConstraintTypeEnum, String> constraints, final int level) {
    Object returnObject;
    String subfieldCleanName = fieldNameSubEntity;
    String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);

    String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
    boolean lastTypeFilterOfLastElement = isLastTypeFilterOfLastElement(remainingFilterChain);
    int size = calculateSizeFromTypeFilter(singleTypeFilter);
    if (lastTypeFilterOfLastElement) {
      fieldExpMappingsQueue.remove();
    }
    SchemaProcessorPOJO pojo = new SchemaProcessorPOJO(objectName, fieldExpMappingsQueue, subfieldCleanName, completeFieldName,
                                                       completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), ""),
                                                       size, valueType, valueLength,
                                                       fieldValuesList, constraints,
                                                       level, lastTypeFilterOfLastElement);
    if (isTypeFilterMap(singleTypeFilter)) {
      returnObject = this.objectCreator.createMap(pojo, getMapAndArrayGenerationFunction(), false);
    } else {
      returnObject = this.objectCreator.createArray(pojo, getMapAndArrayGenerationFunction(), false);
    }
    return returnObject;
  }
}
