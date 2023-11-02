/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.model.SchemaProcessorPOJO;
import com.sngular.kloadgen.processor.objectcreatorfactory.ObjectCreatorFactory;
import com.sngular.kloadgen.processor.objectcreatorfactory.ObjectCreatorFactoryHelper;
import com.sngular.kloadgen.processor.util.SchemaProcessorUtils;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;

public class SchemaProcessor {

  private List<FieldValueMapping> fieldExprMappings;

  private ObjectCreatorFactory objectCreatorFactory;

  public final void processSchema(
      final SchemaTypeEnum schemaType, final Object schema, final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata,
      final List<FieldValueMapping> fieldExprMappings) {
    this.objectCreatorFactory = ObjectCreatorFactoryHelper.getInstance(schemaType, schema, metadata);
    this.fieldExprMappings = fieldExprMappings;
  }

  public final Object next() {
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      final String rootFieldName = "root";
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = calculateFieldsToProcess();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      objectCreatorFactory.createRecord(rootFieldName, "");
      do {
        final int level = 0;
        final String cleanPath = SchemaProcessorUtils.cleanUpPath(fieldValueMapping, level);
        final String fieldName = SchemaProcessorUtils.getCleanMethodName(fieldValueMapping, level);
        final String completeTypeFilterChain = SchemaProcessorUtils.getTypeFilter(fieldName, cleanPath);
        final String singleTypeFilter = SchemaProcessorUtils.getFirstComplexType(completeTypeFilterChain);
        final String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
        final boolean lastTypeFilterOfLastElement = SchemaProcessorUtils.isLastTypeFilterOfLastElement(remainingFilterChain);
        final SchemaProcessorPOJO pojo = SchemaProcessorPOJO.builder().rootFieldName(rootFieldName).fieldExpMappingsQueue(fieldExpMappingsQueue).fieldNameSubEntity(fieldName)
                                                            .completeFieldName(fieldValueMapping.getFieldName()).completeTypeFilterChain(remainingFilterChain)
                                                            .fieldSize(SchemaProcessorUtils.calculateSizeFromTypeFilter(singleTypeFilter))
                                                            .valueType(fieldValueMapping.getFieldType()).valueLength(fieldValueMapping.getValueLength())
                                                            .fieldValuesList(fieldValueMapping.getFieldValuesList()).constraints(fieldValueMapping.getConstraints()).level(level)
                                                            .lastFilterTypeOfLastElement(lastTypeFilterOfLastElement).build();
        processSingleTypeFilterFromRecord(fieldExpMappingsQueue, fieldValueMapping, fieldName, singleTypeFilter, lastTypeFilterOfLastElement, pojo);
        fieldValueMapping = fieldExpMappingsQueue.peek();
      } while (!fieldExpMappingsQueue.isEmpty() && null != fieldValueMapping);
    }
    return this.objectCreatorFactory.generateRecord();
  }

  private Object createObject(final String rootFieldName, final String completeRootFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final int levelCount) {
    final int level = levelCount + 1;
    objectCreatorFactory.createRecord(rootFieldName, SchemaProcessorUtils.getPathUpToFieldName(completeRootFieldName, level));

    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Object objectRecord;
    do {
      final String cleanPath = SchemaProcessorUtils.cleanUpPath(fieldValueMapping, level);
      final String fieldNameSubEntity = SchemaProcessorUtils.getCleanMethodName(fieldValueMapping, level);
      final String completeTypeFilterChain = SchemaProcessorUtils.getTypeFilter(fieldNameSubEntity, cleanPath);
      final String singleTypeFilter = SchemaProcessorUtils.getFirstComplexType(completeTypeFilterChain);
      final String remainingFilterChain = completeTypeFilterChain.replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
      final boolean lastTypeFilterOfLastElement = SchemaProcessorUtils.isLastTypeFilterOfLastElement(remainingFilterChain);
      final SchemaProcessorPOJO pojo = SchemaProcessorPOJO.builder().rootFieldName(rootFieldName).fieldExpMappingsQueue(fieldExpMappingsQueue)
                                                          .fieldNameSubEntity(fieldNameSubEntity).completeFieldName(fieldValueMapping.getFieldName())
                                                          .completeTypeFilterChain(remainingFilterChain)
                                                          .fieldSize(SchemaProcessorUtils.calculateSizeFromTypeFilter(singleTypeFilter)).valueType(fieldValueMapping.getFieldType())
                                                          .valueLength(fieldValueMapping.getValueLength()).fieldValuesList(fieldValueMapping.getFieldValuesList())
                                                          .constraints(fieldValueMapping.getConstraints()).level(level).lastFilterTypeOfLastElement(lastTypeFilterOfLastElement)
                                                          .build();
      objectRecord = processSingleTypeFilterFromRecord(fieldExpMappingsQueue, fieldValueMapping, fieldNameSubEntity, singleTypeFilter, lastTypeFilterOfLastElement, pojo);
      fieldValueMapping = fieldExpMappingsQueue.peek();
    } while (!fieldExpMappingsQueue.isEmpty() && null != fieldValueMapping && SchemaProcessorUtils.isNewFieldSharingRootFieldName(level - 1, fieldValueMapping, rootFieldName));
    return this.objectCreatorFactory.generateSubEntityRecord(objectRecord);
  }

  private Object processSingleTypeFilterFromRecord(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final FieldValueMapping fieldValueMapping, final String fieldNameSubEntity, final String singleTypeFilter,
      final boolean lastTypeFilterOfLastElement, final SchemaProcessorPOJO pojo) {
    final Object objectRecord;
    if (SchemaProcessorUtils.isTypeFilterMap(singleTypeFilter)) {
      objectRecord = this.objectCreatorFactory.createMap(pojo, this::processTypeFilterAfterComplexType, false);
      removeFieldFromListIfNeededAfterProcessComplexType(fieldExpMappingsQueue, fieldValueMapping.getFieldValuesList(), lastTypeFilterOfLastElement);
    } else if (SchemaProcessorUtils.isTypeFilterArray(singleTypeFilter)) {
      objectRecord = this.objectCreatorFactory.createArray(pojo, this::processTypeFilterAfterComplexType, false);
      removeFieldFromListIfNeededAfterProcessComplexType(fieldExpMappingsQueue, fieldValueMapping.getFieldValuesList(), lastTypeFilterOfLastElement);
    } else if (SchemaProcessorUtils.isTypeFilterRecord(singleTypeFilter)) {
      createObject(fieldNameSubEntity, fieldValueMapping.getFieldName(), fieldExpMappingsQueue, pojo.getLevel());
      this.objectCreatorFactory.assignRecord(pojo);
      objectRecord = this.objectCreatorFactory.getRootNode(pojo.getRootFieldName());
    } else {
      fieldExpMappingsQueue.remove();
      objectRecord = this.objectCreatorFactory.createValueObject(pojo);
    }
    return objectRecord;
  }

  private void removeFieldFromListIfNeededAfterProcessComplexType(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final List<String> fieldValuesList, final boolean lastTypeFilterOfLastElement) {
    if (lastTypeFilterOfLastElement || fieldValuesList.contains("null")) {
      fieldExpMappingsQueue.remove();
    }
  }

  private Object processTypeFilterAfterComplexType(final SchemaProcessorPOJO pojo) {
    final Object returnObject;
    final String singleTypeFilter = SchemaProcessorUtils.getFirstComplexType(pojo.getCompleteTypeFilterChain());
    if (SchemaProcessorUtils.hasMapOrArrayTypeFilter(pojo.getCompleteTypeFilterChain())
        && (SchemaProcessorUtils.isTypeFilterMap(singleTypeFilter) || SchemaProcessorUtils.isTypeFilterArray(singleTypeFilter))) {
      returnObject = processNestedComplexTypes(pojo, singleTypeFilter);
    } else if (SchemaProcessorUtils.isTypeFilterRecord(singleTypeFilter)) {
      returnObject = createObject(pojo.getFieldNameSubEntity(), pojo.getCompleteFieldName(), pojo.getFieldExpMappingsQueue(), pojo.getLevel());
    } else {
      pojo.getFieldExpMappingsQueue().remove();
      returnObject = this.objectCreatorFactory.createValueObject(pojo);
    }
    return returnObject;
  }

  @SneakyThrows
  private Object processNestedComplexTypes(final SchemaProcessorPOJO pojo, final String singleTypeFilter) {
    final Object returnObject;
    final String remainingFilterChain = pojo.getCompleteTypeFilterChain().replaceFirst(singleTypeFilter.replaceAll("\\[", "\\\\["), "");
    final boolean lastTypeFilterOfLastElement = SchemaProcessorUtils.isLastTypeFilterOfLastElement(remainingFilterChain);
    removeFieldFromListIfNeededAfterProcessComplexType(pojo.getFieldExpMappingsQueue(), pojo.getFieldValuesList(), lastTypeFilterOfLastElement);
    final SchemaProcessorPOJO newPojo = SchemaProcessorPOJO.builder().rootFieldName(pojo.getRootFieldName()).fieldExpMappingsQueue(pojo.getFieldExpMappingsQueue())
                                                           .fieldNameSubEntity(pojo.getFieldNameSubEntity()).completeFieldName(pojo.getCompleteFieldName())
                                                           .completeTypeFilterChain(remainingFilterChain)
                                                           .fieldSize(SchemaProcessorUtils.calculateSizeFromTypeFilter(singleTypeFilter)).valueType(pojo.getValueType())
                                                           .valueLength(pojo.getValueLength()).fieldValuesList(pojo.getFieldValuesList()).constraints(pojo.getConstraints())
                                                           .level(pojo.getLevel()).lastFilterTypeOfLastElement(lastTypeFilterOfLastElement).build();
    if (SchemaProcessorUtils.isTypeFilterMap(singleTypeFilter)) {
      returnObject = this.objectCreatorFactory.createMap(newPojo, this::processTypeFilterAfterComplexType, true);
    } else {
      returnObject = this.objectCreatorFactory.createArray(newPojo, this::processTypeFilterAfterComplexType, true);
    }
    return returnObject;
  }

  private void makeFieldValueMappingRequiredAndNotNullable(final FieldValueMapping fieldValueMapping) {
    makeFieldValueMappingRequired(fieldValueMapping);
    final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
    temporalFieldValueList.remove("null");
    fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
  }

  private void makeFieldValueMappingRequired(final FieldValueMapping fieldValueMapping) {
    fieldValueMapping.setRequired(true);
  }

  private ArrayDeque<FieldValueMapping> calculateFieldsToProcess() {
    final ArrayDeque<FieldValueMapping> initialFieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
    return initialFieldExpMappingsQueue.stream().filter(fieldValueMapping -> shouldProcessField(fieldValueMapping, initialFieldExpMappingsQueue))
                                       .collect(Collectors.toCollection(ArrayDeque::new));
  }

  private boolean shouldProcessField(final FieldValueMapping fieldValueMapping, final ArrayDeque<FieldValueMapping> initialFieldExpMappingsQueue) {
    boolean shouldProcess = true;
    final String[] fields = fieldValueMapping.getFieldName().split("\\.");
    if (fieldValueMapping.getRequired()) {
      fieldValueMapping.getFieldValuesList().remove("null");
    } else {
      for (int level = 0; level < fields.length; level++) {
        shouldProcess = shouldProcessAccordingToSubField(fieldValueMapping, initialFieldExpMappingsQueue, level);
      }
    }
    return shouldProcess;
  }

  private boolean shouldProcessAccordingToSubField(final FieldValueMapping fieldValueMapping, final ArrayDeque<FieldValueMapping> initialFieldExpMappingsQueue, final int level) {
    boolean shouldProcess = false;
    final boolean lastLevel = SchemaProcessorUtils.isLastLevel(fieldValueMapping, level);
    if (!fieldValueMapping.getRequired()
        && this.objectCreatorFactory.isOptionalFieldAccordingToSchema(fieldValueMapping.getFieldName(),
                                                                      SchemaProcessorUtils.getConcreteLevelField(fieldValueMapping, level), level)) {
      if (lastLevel) {
        shouldProcess = shouldProcessLastLevelNotRequiredSubField(fieldValueMapping, initialFieldExpMappingsQueue, level);
      } else if (!fieldValueMapping.getAncestorRequired()) {
        makeFieldValueMappingRequired(fieldValueMapping);
      }
    }
    return shouldProcess;
  }

  private boolean shouldProcessLastLevelNotRequiredSubField(
      final FieldValueMapping fieldValueMapping, final ArrayDeque<FieldValueMapping> initialFieldExpMappingsQueue, final int level) {
    boolean shouldProcess = false;
    if (Objects.requireNonNull(fieldValueMapping).getAncestorRequired()) {
      if (SchemaProcessorUtils.checkIfOptionalCollection(fieldValueMapping, level)) {
        makeFieldValueMappingRequired(fieldValueMapping);
      } else {
        shouldProcess = searchFieldWithSharedPathAndMakeItProcessable(fieldValueMapping, initialFieldExpMappingsQueue, level);
      }
    }
    return shouldProcess;
  }

  private boolean searchFieldWithSharedPathAndMakeItProcessable(
      final FieldValueMapping fieldValueMapping, final ArrayDeque<FieldValueMapping> initialFieldExpMappingsQueue, final int level) {
    final boolean shouldProcess;
    boolean otherFieldIsRequired = false;
    final List<FieldValueMapping> listFieldsSharingPath = getFieldValueMappingsSharingLevel(fieldValueMapping, initialFieldExpMappingsQueue, level);
    final Iterator<FieldValueMapping> iterator = listFieldsSharingPath.iterator();
    while (iterator.hasNext() && !otherFieldIsRequired) {
      final FieldValueMapping field = iterator.next();
      otherFieldIsRequired = field.getRequired();
    }
    if (otherFieldIsRequired) {
      shouldProcess = false;
    } else {
      if (!listFieldsSharingPath.isEmpty()) {
        makeFieldValueMappingRequiredAndNotNullable(fetchFieldSharingPathToMakeItRequired(listFieldsSharingPath));
        shouldProcess = fieldValueMapping.getRequired();
      } else {
        shouldProcess = true;
      }
    }
    return shouldProcess;
  }

  private FieldValueMapping fetchFieldSharingPathToMakeItRequired(final List<FieldValueMapping> listFieldsSharingPath) {
    return listFieldsSharingPath.get(RandomUtils.nextInt(0, listFieldsSharingPath.size()));
  }

  private List<FieldValueMapping> getFieldValueMappingsSharingLevel(
      final FieldValueMapping fieldValueMapping, final ArrayDeque<FieldValueMapping> initialFieldExpMappingsQueue, final int level) {
    final String fieldPath = SchemaProcessorUtils.getPathUpToFieldName(fieldValueMapping.getFieldName(), level + 1);
    return initialFieldExpMappingsQueue.stream()
                                       .filter(fieldFromList -> SchemaProcessorUtils.getPathUpToFieldName(fieldFromList.getFieldName(), level + 1).equalsIgnoreCase(fieldPath))
                                       .collect(Collectors.toList());
  }
}
