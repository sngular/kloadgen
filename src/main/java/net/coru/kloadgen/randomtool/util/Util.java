package net.coru.kloadgen.randomtool.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.jmeter.threads.JMeterContextService;

public class Util {

  private Util() {

  }

  public static List<String> cleanFieldsName(List<String> fieldValuesList) {
    List<String> parameterList = new ArrayList<>(fieldValuesList);

    parameterList.replaceAll(fieldValue ->
        fieldValue.matches("\\$\\{\\w*}") ?
            JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);
    return parameterList;
  }

  public static Object castValue(Object value, String type) {
    Object castValue;
    switch (type) {
      case ValidType.INT:
        castValue = Integer.valueOf(value.toString());
        break;
      case ValidType.DOUBLE:
        castValue = Double.valueOf(value.toString());
        break;
      case ValidType.LONG:
        castValue = Long.valueOf(value.toString());
        break;
      case ValidType.FLOAT:
        castValue = Float.valueOf(value.toString());
        break;
      case ValidType.BOOLEAN:
        castValue = Boolean.valueOf(value.toString());
        break;
      default:
        castValue = value.toString();
        break;
    }
    return castValue;
  }
}
