package net.coru.kloadgen.processor.objectcreator;

import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;

import java.util.ArrayDeque;
import java.util.List;
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
      String fieldName,
      String completeFieldName,
      String valueType,
      Integer valueLength,
      List<String> fieldValuesList);

  Object assignObject(String targetObjectName, String fieldName, Object objectToAssign);

  Object assignRecord(String targetObjectName, String fieldName, String recordToAssign);

  Object createRecord(String objectName);

  Object generateRecord();

  boolean isOptional(String rootFieldName, String fieldName);
}
