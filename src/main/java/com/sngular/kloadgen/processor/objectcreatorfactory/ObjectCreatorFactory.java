package com.sngular.kloadgen.processor.objectcreatorfactory;

import java.util.function.Function;

import com.sngular.kloadgen.processor.model.SchemaProcessorPOJO;

public interface ObjectCreatorFactory {

  Object createMap(
      SchemaProcessorPOJO pojo, Function<SchemaProcessorPOJO, Object> generateFunction, boolean isInnerMap);

  Object createArray(
      SchemaProcessorPOJO pojo, Function<SchemaProcessorPOJO, Object> generateFunction, boolean isInnerArray);

  Object createValueObject(
      SchemaProcessorPOJO pojo);

  void assignRecord(SchemaProcessorPOJO pojo);

  void createRecord(String objectName, String completeFieldName);

  Object generateRecord();

  Object generateSubEntityRecord(Object objectRecord);

  boolean isOptionalFieldAccordingToSchema(final String completeFieldName, final String fieldName, final int level);

  Object getRootNode(final String rootNode);
}
