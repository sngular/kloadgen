package net.coru.kloadgen.randomtool.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.util.Util;

public class StatelessGeneratorTool {

  private final Map<String, Object> context = new HashMap<>();

  private final RandomMap randomMap = new RandomMap();

  private final RandomArray randomArray = new RandomArray();

  private final RandomObject randomObject = new RandomObject();

  public Object generateObject(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = Util.cleanFieldsName(fieldValuesList);

    Object value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = randomObject.generateSeq(fieldName, fieldType, parameterList, context);
    }

    return value;
  }

  public Object generateMap(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {
    List<String> parameterList = Util.cleanFieldsName(fieldValuesList);
    return randomMap.generateMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());
  }

  public Object generateArray(String fieldName, String fieldType, Integer arraySize, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = Util.cleanFieldsName(fieldValuesList);

    Object value = randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    if ("seq".equals(fieldType)) {
      value = randomObject.generateSeq(fieldName, fieldType, parameterList, context);
    }

    return value;
  }
}
