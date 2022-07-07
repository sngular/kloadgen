package net.coru.kloadgen.processor.objectcreator;

import java.util.ArrayDeque;
import java.util.function.BiFunction;

import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;

public interface ObjectCreator {

  Object createMap(
      SchemaProcessorPOJO pojo,
      BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      boolean returnCompleteEntry);

  Object createArray(
      SchemaProcessorPOJO pojo,
      BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      boolean returnCompleteEntry);

  Object createValueObject(
      SchemaProcessorPOJO pojo);

  Object assignRecord(String targetObjectName, String fieldName, String recordToAssign);

  void createRecord(String objectName);

  Object generateRecord();

  Object generateSubentityRecord(Object objectRecord);

  boolean isOptional(String rootFieldName, String fieldName);
}
