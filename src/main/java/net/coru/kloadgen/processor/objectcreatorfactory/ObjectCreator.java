package net.coru.kloadgen.processor.objectcreatorfactory;

import java.util.ArrayDeque;
import java.util.function.BiFunction;

import net.coru.kloadgen.processor.model.SchemaProcessorPOJO;

public interface ObjectCreator {

  Object createMap(
      SchemaProcessorPOJO pojo, BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, boolean isInnerMap);

  Object createArray(
      SchemaProcessorPOJO pojo, BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, boolean isInnerArray);

  Object createValueObject(
      SchemaProcessorPOJO pojo);

  void assignRecord(SchemaProcessorPOJO pojo);

  void createRecord(String objectName, String completeFieldName);

  Object generateRecord();

  Object generateSubEntityRecord(Object objectRecord);

  boolean isOptionalFieldAccordingToSchema(final String completeFieldName, final String fieldName, final int level);
}
