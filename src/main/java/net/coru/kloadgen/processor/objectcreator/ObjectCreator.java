package net.coru.kloadgen.processor.objectcreator;

import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.BiFunction;

public interface ObjectCreator {

  String generateString(Integer valueLength);

  Object createMap(
    String objectName,
    ArrayDeque<?> fieldExpMappingsQueue,
    String fieldName,
    String completeFieldName,
    Integer mapSize,
    String completeTypeFilterChain,
    String valueType,
    Integer valueLength,
    List<String> fieldValuesList,
    int level,
    BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
    boolean returnCompleteEntry);

  Object createArray(
    String objectName,
    ArrayDeque<?> fieldExpMappingsQueue,
    String fieldName,
    String completeFieldName,
    Integer arraySize,
    String completeTypeFilterChain,
    String valueType,
    Integer valueLength,
    List<String> fieldValuesList,
    int level,
    BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
    boolean returnCompleteEntry);

  Object createRepeatedObject(
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
