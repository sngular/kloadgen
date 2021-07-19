package net.coru.kloadgen.randomtool.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.jmeter.threads.JMeterContextService;

public class Utils {

  private Utils() {

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
      case ValidTypes.INT:
        castValue = Integer.valueOf(value.toString());
        break;
      case ValidTypes.DOUBLE:
        castValue = Double.valueOf(value.toString());
        break;
      case ValidTypes.LONG:
        castValue = Long.valueOf(value.toString());
        break;
      case ValidTypes.FLOAT:
        castValue = Float.valueOf(value.toString());
        break;
      case ValidTypes.BOOLEAN:
        castValue = Boolean.valueOf(value.toString());
        break;
      default:
        castValue = value.toString();
        break;
    }
    return castValue;
  }
}
