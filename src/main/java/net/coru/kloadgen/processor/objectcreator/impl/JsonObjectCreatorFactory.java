package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;
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
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createValueObject(final SchemaProcessorPOJO pojo) {return null;}

  @Override
  public Object assignRecord(final String targetObjectName, final String fieldName, final String recordToAssign) {
    return null;
  }

  @Override
  public void createRecord(final String objectName) {
  }

  @Override
  public Object generateRecord() {
    return null;
  }

  @Override
  public Object generateSubentityRecord(Object objectRecord) {
    return objectRecord;
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    return false;
  }

  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    return null;
  }

  private String generateString(final Integer valueLength) {
    return null;
  }
}
