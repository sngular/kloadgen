/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.jmeter.threads.JMeterContextService;

public class Utils {

  private Utils() {

  }

  public static List<String> replaceValuesContext(List<String> fieldValuesList) {
    List<String> parameterList = new ArrayList<>(fieldValuesList);

    parameterList.replaceAll(fieldValue ->
        fieldValue.matches("\\$\\{\\w*}") ?
            JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);
    return parameterList;
  }

  public static Object castValue(Object value, String type) {
    Object castValue;
    switch (type) {
      case ValidTypeConstants.INT:
        castValue = Integer.valueOf(value.toString());
        break;
      case ValidTypeConstants.DOUBLE:
        castValue = Double.valueOf(value.toString());
        break;
      case ValidTypeConstants.LONG:
        castValue = Long.valueOf(value.toString());
        break;
      case ValidTypeConstants.FLOAT:
        castValue = Float.valueOf(value.toString());
        break;
      case ValidTypeConstants.BOOLEAN:
        castValue = Boolean.valueOf(value.toString());
        break;
      default:
        castValue = value.toString();
        break;
    }
    return castValue;
  }
}
