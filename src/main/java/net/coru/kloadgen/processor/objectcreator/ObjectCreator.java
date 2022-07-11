package net.coru.kloadgen.processor.objectcreator;

import java.util.ArrayDeque;
import java.util.function.BiFunction;

import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;

public interface ObjectCreator {

  Object createMap(
      SchemaProcessorPOJO pojo, BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, boolean isInnerMap);

  Object createArray(
      SchemaProcessorPOJO pojo, BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, boolean isInnerArray);

  Object createValueObject(
      SchemaProcessorPOJO pojo);

  Object assignRecord(SchemaProcessorPOJO pojo);

  void createRecord(String objectName, String completeFieldName);

  Object generateRecord();

  Object generateSubEntityRecord(Object objectRecord);

  boolean isOptional(SchemaProcessorPOJO pojo);
}
