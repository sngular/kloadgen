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

import lombok.SneakyThrows;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.ObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;

public class SchemaProcessor extends SchemaProcessorUtils {

  private List<FieldValueMapping> fieldExprMappings;

  private ObjectCreator objectCreator;

  public void processSchema(final SchemaTypeEnum schemaType, final Object schema, final Object metadata, final List<FieldValueMapping> fieldExprMappings) {
    this.objectCreator = ObjectCreatorFactory.getInstance(schemaType, schema, metadata);
    this.fieldExprMappings = fieldExprMappings;
  }

  public Object next() {
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;
      this.objectCreator.createRecord("root", "");
      while (!fieldExpMappingsQueue.isEmpty()) {
        final int level = 0;
        final String cleanPath = cleanUpPath(fieldValueMapping, level);
        final String fieldName = getCleanMethodName(fieldValueMapping, level);
        final String completeTypeFilterChain = getTypeFilter(fieldName, cleanPath);
        final String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);
        final String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
        final boolean lastTypeFilterOfLastElement = isLastTypeFilterOfLastElement(remainingFilterChain);

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName)) && generatedProperties == elapsedProperties
            && generatedProperties > 0 && fieldValueMapping.getAncestorRequired()) {
          fieldValueMapping.setRequired(true);
          final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          fieldExpMappingsQueueCopy.poll();
        } else {
          generatedProperties = 0;
          elapsedProperties = 0;
          fieldExpMappingsQueueCopy.poll();
        }
        generatedProperties++;

        final SchemaProcessorPOJO pojo = new SchemaProcessorPOJO("root", fieldExpMappingsQueue, fieldName, fieldValueMapping.getFieldName(), remainingFilterChain,
                                                                 calculateSizeFromTypeFilter(singleTypeFilter), fieldValueMapping.getFieldType(),
                                                                 fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(), fieldValueMapping.getConstraints(),
                                                                 level, lastTypeFilterOfLastElement);

        if (this.objectCreator.isOptional(pojo) && !Objects.requireNonNull(fieldValueMapping).getRequired() && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
        } else {

          if (isTypeFilterMap(singleTypeFilter)) {
            this.objectCreator.createMap(pojo, getMapAndArrayGenerationFunction(), false);
            if (lastTypeFilterOfLastElement) {
              fieldExpMappingsQueue.remove();
            }
          } else if (isTypeFilterArray(singleTypeFilter)) {
            this.objectCreator.createArray(pojo, getMapAndArrayGenerationFunction(), false);
            if (lastTypeFilterOfLastElement) {
              fieldExpMappingsQueue.remove();
            }
          } else if (isTypeFilterRecord(singleTypeFilter)) {
            createObject(fieldName, fieldValueMapping.getFieldName(), fieldExpMappingsQueue, level);
            this.objectCreator.assignRecord(pojo);
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

  private Object createObject(final String rootFieldName, final String completeRootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int levelCount) {
    final int level = levelCount + 1;
    this.objectCreator.createRecord(rootFieldName, getPathUpToFieldName(completeRootFieldName, level));

    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>();
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopyCopy = new ArrayDeque<>();
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    Object objectRecord = new Object();

    while (!fieldExpMappingsQueue.isEmpty() && isNewFieldSharingRootFieldName(level - 1, fieldValueMapping, rootFieldName)) {
      final String cleanPath = cleanUpPath(fieldValueMapping, level);
      final String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, level);
      final String completeTypeFilterChain = getTypeFilter(fieldNameSubEntity, cleanPath);
      final String singleTypeFilter = getFirstComplexType(completeTypeFilterChain);
      final String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
      final boolean lastTypeFilterOfLastElement = isLastTypeFilterOfLastElement(remainingFilterChain);
      generatedProperties++;

      final SchemaProcessorPOJO pojo = new SchemaProcessorPOJO(rootFieldName, fieldExpMappingsQueue, fieldNameSubEntity, fieldValueMapping.getFieldName(), remainingFilterChain,
                                                               calculateSizeFromTypeFilter(singleTypeFilter), fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(),
                                                               fieldValueMapping.getFieldValuesList(), fieldValueMapping.getConstraints(), level, lastTypeFilterOfLastElement);

      /*if (objectCreator.isOptional(pojo) && fieldValueMapping.getFieldValuesList().contains("null")) {

        elapsedProperties++;
        final FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        final FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if ((fieldExpMappingsQueue.peek() == null || !Objects.requireNonNull(nextField).getFieldName().contains(rootFieldName)) && actualField.getAncestorRequired() &&
            generatedProperties == elapsedProperties && generatedProperties > 0) {

          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          if (fieldExpMappingsQueue.peek() == null) {
            fieldExpMappingsQueue.add(fieldValueMapping);
          }
        } else {
          fieldValueMapping = nextField;
        }

      }*/

      if (this.objectCreator.isOptional(pojo)) {

        elapsedProperties++;
        final FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExpMappingsQueue);
        fieldExpMappingsQueue.remove();
        final FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (nextField == null && Objects.requireNonNull(actualField).getAncestorRequired() && generatedProperties == elapsedProperties && generatedProperties > 0) {

          if (hasTypeFilterInnerRecord(pojo.getCompleteTypeFilterChain())) {
            fieldValueMapping = null;
          } else {
            fieldValueMapping = makeFieldValueMappingRequired(actualField);
            fieldExpMappingsQueueCopyCopy = new ArrayDeque<>(fieldExpMappingsQueueCopy);
          }

        } else if (nextField == null && (!actualField.getRequired() || !actualField.getRequired() && !actualField.getAncestorRequired())) {
          fieldValueMapping = null;

        } else if (checkIfOptionalCollection(fieldValueMapping, cleanPath)) {
          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);

        } else if (!Objects.requireNonNull(nextField).getFieldName().contains(fieldNameSubEntity) && Objects.requireNonNull(actualField).getAncestorRequired()
                   && fieldExpMappingsQueue.peek() != null && generatedProperties == elapsedProperties && generatedProperties > 0) {

          if (fieldValueMapping.getFieldName().contains(".")) {
            final String ancestorName = fieldValueMapping.getFieldName().substring(0, fieldValueMapping.getFieldName().indexOf("."));
            if (Objects.requireNonNull(nextField).getFieldName().contains(ancestorName)) {
              fieldValueMapping = nextField;
            } else {
              if (hasTypeFilterInnerRecord(pojo.getCompleteTypeFilterChain())) {
                fieldValueMapping = nextField;
              } else {
                fieldValueMapping = makeFieldValueMappingRequired(actualField);
              }
            }
          } else {
            fieldValueMapping = makeFieldValueMappingRequired(actualField);
          }
        } else {
          fieldValueMapping = nextField;

        }
      } else {
        if (isTypeFilterMap(singleTypeFilter)) {
          objectRecord = this.objectCreator.createMap(pojo, getMapAndArrayGenerationFunction(), false);
          if (lastTypeFilterOfLastElement) {
            fieldExpMappingsQueue.remove();
          }
        } else if (isTypeFilterArray(singleTypeFilter)) {
          objectRecord = this.objectCreator.createArray(pojo, getMapAndArrayGenerationFunction(), false);
          if (lastTypeFilterOfLastElement) {
            fieldExpMappingsQueue.remove();
          }
        } else if (isTypeFilterRecord(singleTypeFilter)) {
          objectRecord = createObject(fieldNameSubEntity, fieldValueMapping.getFieldName(), fieldExpMappingsQueue, level);
          this.objectCreator.assignRecord(pojo);
        } else {
          fieldExpMappingsQueue.remove();
          objectRecord = this.objectCreator.createValueObject(pojo);
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return this.objectCreator.generateSubEntityRecord(objectRecord);
  }

  private BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> getMapAndArrayGenerationFunction() {
    final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generationFunction = (fieldExpMappingsQueue, schemaProcessorPOJO) -> {
      final Object returnObject;
      final String singleTypeFilter = getFirstComplexType(schemaProcessorPOJO.getCompleteTypeFilterChain());
      if (hasMapOrArrayTypeFilter(schemaProcessorPOJO.getCompleteTypeFilterChain()) && (isTypeFilterMap(singleTypeFilter) || isTypeFilterArray(singleTypeFilter))) {
        returnObject = processComplexTypes(schemaProcessorPOJO, singleTypeFilter, fieldExpMappingsQueue);
      } else if (isTypeFilterRecord(singleTypeFilter)) {
        returnObject = createObject(schemaProcessorPOJO.getFieldNameSubEntity(), schemaProcessorPOJO.getCompleteFieldName(), (ArrayDeque<FieldValueMapping>) fieldExpMappingsQueue,
                                    schemaProcessorPOJO.getLevel());
      } else {
        fieldExpMappingsQueue.remove();
        returnObject = this.objectCreator.createValueObject(schemaProcessorPOJO);
      }
      return returnObject;
    };
    return generationFunction;
  }

  @SneakyThrows
  private Object processComplexTypes(final SchemaProcessorPOJO pojo, final String singleTypeFilter, final ArrayDeque<?> fieldExpMappingsQueue) {
    final Object returnObject;
    final String remainingFilterChain = pojo.getCompleteTypeFilterChain().replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
    final boolean lastTypeFilterOfLastElement = isLastTypeFilterOfLastElement(remainingFilterChain);
    if (lastTypeFilterOfLastElement) {
      fieldExpMappingsQueue.remove();
    }
    SchemaProcessorPOJO newPojo = (SchemaProcessorPOJO) pojo.clone();
    newPojo.setFieldSize(calculateSizeFromTypeFilter(singleTypeFilter));
    newPojo.setLastFilterTypeOfLastElement(lastTypeFilterOfLastElement);
    newPojo.setCompleteTypeFilterChain(remainingFilterChain);
    if (isTypeFilterMap(singleTypeFilter)) {
      returnObject = this.objectCreator.createMap(newPojo, getMapAndArrayGenerationFunction(), true);
    } else {
      returnObject = this.objectCreator.createArray(newPojo, getMapAndArrayGenerationFunction(), true);
    }
    return returnObject;
  }

  private FieldValueMapping makeFieldValueMappingRequired(final FieldValueMapping fieldValueMapping) {
    fieldValueMapping.setRequired(true);
    final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
    temporalFieldValueList.remove("null");

    fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
    return fieldValueMapping;
  }

}
