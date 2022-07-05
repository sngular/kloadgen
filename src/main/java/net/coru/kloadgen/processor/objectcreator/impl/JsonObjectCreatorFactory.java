package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.BiFunction;

import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreatorFactory implements ObjectCreator {

  private StatelessGeneratorTool statelessGeneratorTool;

  public JsonObjectCreatorFactory() {
    this.statelessGeneratorTool = new StatelessGeneratorTool();
  }

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createValueObject(
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

  private String generateString(final Integer valueLength) {
    return null;
  }
}
