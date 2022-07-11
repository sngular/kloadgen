package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreatorFactory implements ObjectCreator {

  private static final StatelessGeneratorTool STATELESS_GENERATOR_TOOL = new StatelessGeneratorTool();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {
    return null;
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {
    return null;
  }

  @Override
  public Object createValueObject(final SchemaProcessorPOJO pojo) {return null;}

  @Override
  public Object assignRecord(final SchemaProcessorPOJO pojo) {
    return null;
  }

  @Override
  public void createRecord(final String objectName, final String completeFieldName) {
  }

  @Override
  public Object generateRecord() {
    return null;
  }

  @Override
  public Object generateSubEntityRecord(Object objectRecord) {
    return objectRecord;
  }

  @Override
  public boolean isOptional(final SchemaProcessorPOJO pojo) {
    return false;
  }

  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    return null;
  }
}
