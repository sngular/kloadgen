package net.coru.kloadgen.processor.objectcreator;

import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;

import java.util.ArrayDeque;
import java.util.function.BiFunction;

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

  boolean isOptional(String rootFieldName, String fieldName);
}
