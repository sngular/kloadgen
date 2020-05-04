package net.coru.kloadgen.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatelessRandomTool {

  private Map<String, Object> context = new HashMap<>();

  public Object generateRandom(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList) {
    
    Object value = RandomTool.generateRandom(fieldType, valueLength, fieldValuesList);
    if("seq".equals(fieldType)) {
      value = RandomTool.generateSeq(fieldName, fieldType, fieldValuesList, context);
    }
    
    return value;
  }
}