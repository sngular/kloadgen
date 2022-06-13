package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.List;
import java.util.Map;

import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreator implements ProcessorObjectCreator {

  private StatelessGeneratorTool statelessGeneratorTool;

  public JsonObjectCreator() {
    this.statelessGeneratorTool = new StatelessGeneratorTool();
  }

  @Override
  public String generateRandomString(final Integer valueLength) {
    return statelessGeneratorTool.generateRandomString(valueLength);
  }

  @Override
  public boolean isOptionalField(final Object field, final boolean isRequired, final String nameOfField, final List<String> fieldValuesList) {
    return !isRequired && fieldValuesList.contains("null")
           && (!nameOfField.contains(".") || (nameOfField.contains(".") && nameOfField.contains("[")) || nameOfField.contains("["));
  }

  @Override
  public Object createBasicArrayMap(final String fieldName, final String fieldType, final Integer calculateSize, final Integer valueLength, final List<String> fieldValuesList) {
    return statelessGeneratorTool.generateArray(fieldName, fieldType, calculateSize, valueLength, fieldValuesList);
  }

  @Override
  public Object createBasicMapArray(final String fieldType, final Integer calculateSize, final List<String> fieldValuesList, Integer valueLength, Integer arraySize, Map<ConstraintTypeEnum, String> constraints) {
    return statelessGeneratorTool.generateMap(fieldType, calculateSize, fieldValuesList, calculateSize);
  }

  @Override
  public Object createBasicMap(final String fieldName, final String fieldType, final Integer arraySize, Integer fieldValueLength, final List<String> fieldValuesList) {
    return statelessGeneratorTool.generateMap(fieldType, arraySize, fieldValuesList, arraySize);
  }

  @Override
  public Object createBasicArray(final String fieldName, final String fieldType, final Integer calculateSize, final Integer valueSize, final List<String> fieldValuesList) {
    return statelessGeneratorTool.generateArray(fieldName, fieldType, calculateSize, valueSize, fieldValuesList);
  }

  @Override
  public Object createFinalField(final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList) {
    return statelessGeneratorTool.generateObject(fieldName,
                                              fieldType,
                                              valueLength,
                                              fieldValuesList);
  }
}
