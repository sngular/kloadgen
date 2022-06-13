package net.coru.kloadgen.processor.objectcreator;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import net.coru.kloadgen.model.ConstraintTypeEnum;

public interface ProcessorObjectCreator {

  String generateRandomString(Integer valueLength);

  boolean isOptionalField(Object field, boolean isRequired, String nameOfField, List<String> fieldValuesList);

  Object createBasicArrayMap(String fieldName, String fieldType, Integer arraySize, Integer valueLength, List<String> fieldValuesList);

  Object createBasicMapArray(String fieldType, Integer calculateSize, List<String> fieldValuesList, Integer valueLength, Integer arraySize, Map<ConstraintTypeEnum, String> constraints);

  Object createBasicMap(String fieldName, String fieldType, Integer arraySize, Integer fieldValueLength, List<String> fieldValuesList);

  Object createBasicArray(String fieldName, String fieldType, Integer calculateSize, Integer valueSize, List<String> fieldValuesList);

  Object createFinalField(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList);
}
