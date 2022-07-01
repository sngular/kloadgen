package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.BiFunction;

import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreatorFactory implements ObjectCreator {

  private StatelessGeneratorTool statelessGeneratorTool;

  public JsonObjectCreatorFactory() {
    this.statelessGeneratorTool = new StatelessGeneratorTool();
  }

  @Override
  public String generateString(final Integer valueLength) {
    return null;
  }

  @Override
  public Object createMap(
      final String objectName, final ArrayDeque<?> fieldExpMappingsQueue, final String fieldName, final String completeFieldName, final Integer mapSize,
      final String completeTypeFilterChain, final String valueType,
      final Integer valueLength, final List<String> fieldValuesList, final int level, final BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createArray(
      final String objectName, final ArrayDeque<?> fieldExpMappingsQueue, final String fieldName, final String completeFieldName, final Integer arraySize,
      final String completeTypeFilterChain, final String valueType,
      final Integer valueLength, final List<String> fieldValuesList, final int level, final BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createRepeatedObject(
      final String fieldName, final String completeFieldName, final String valueType, final Integer valueLength, final List<String> fieldValuesList) {
    return null;
  }

  @Override
  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    return null;
  }

  @Override
  public Object assignRecord(final String targetObjectName, final String fieldName, final String recordToAssign) {
    return null;
  }

  @Override
  public Object createRecord(final String objectName) {
    return null;
  }

  @Override
  public Object generateRecord() {
    return null;
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    return false;
  }
}
