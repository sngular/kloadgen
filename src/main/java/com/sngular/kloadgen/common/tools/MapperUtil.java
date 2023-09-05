/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.common.tools;

import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class MapperUtil {

  public static final String INTEGER = "integer";

  public static final String DOUBLE = "double";

  public static final String FLOAT = "float";

  public static final String NUMBER = "number";

  public static final String INT_64 = "int64";

  public static final String LONG = "long";

  public static final String BIG_DECIMAL = "bigDecimal";

  public static final String REF = "$ref";

  private static final String DIVISOR = "([./])";

  private MapperUtil() {}

  public static String getSimpleType(final JsonNode schema, final String prefix, final String suffix) {
    String type = schema.textValue();
    if (schema.has("type")) {
      type = schema.get("type").textValue();
      String format = null;
      if ("string".equalsIgnoreCase(type)) {
        type = "String";
      }
      if (schema.has("format")) {
        format = schema.get("format").textValue();
      }
      if (NUMBER.equalsIgnoreCase(type)) {
        if (FLOAT.equalsIgnoreCase(format)) {
          type = FLOAT;
        } else if (DOUBLE.equalsIgnoreCase(format)) {
          type = DOUBLE;
        } else {
          type = BIG_DECIMAL;
        }
      } else if (INTEGER.equalsIgnoreCase(type)) {
        if (INT_64.equalsIgnoreCase(format)) {
          type = LONG;
        } else {
          type = INTEGER;
        }
      }
    } else if (schema.has(REF)) {
      type = getRef(schema, prefix, suffix);
    }
    return type;
  }

  public static String getRef(final JsonNode schema, final String prefix, final String suffix) {
    return getPojoName(getRefClass(schema), prefix, suffix);
  }

  public static String getLongRefClass(final JsonNode schema) {
    final String[] pathObjectRef = getStrings(schema);
    return pathObjectRef[pathObjectRef.length - 2] + "/" + pathObjectRef[pathObjectRef.length - 1];
  }

  private static String[] getStrings(final JsonNode schema) {
    return splitName(schema.get(REF).textValue());
  }

  public static String getRefClass(final JsonNode schema) {
    final String[] pathObjectRef = getStrings(schema);
    return pathObjectRef[pathObjectRef.length - 1];
  }

  public static String getTypeMap(final JsonNode mapSchema, final String prefix, final String suffix) {
    var typeMap = "";
    final var mapNode = mapSchema.get("additionalProperties");
    final var mapValueType = mapNode.findPath("type");
    typeMap = getCollectionType(mapNode, mapValueType, prefix, suffix);
    return typeMap;
  }

  public static String getTypeArray(final JsonNode array, final String prefix, final String suffix) {
    var typeArray = "";
    final var arrayNode = array.get("items");
    final JsonNode mapValueType;
    if (arrayNode.has("type")) {
      mapValueType = arrayNode.get("type");
    } else {
      mapValueType = arrayNode.get(REF);
    }
    typeArray = getCollectionType(arrayNode, mapValueType, prefix, suffix);
    return typeArray;
  }

  private static String getCollectionType(final JsonNode mapNode, final JsonNode mapValueType, final String prefix, final String suffix) {
    var typeMap = mapValueType.textValue();
    if (!typeMap.contains("#")) {
      if ("string".equalsIgnoreCase(mapValueType.textValue())) {
        typeMap = "String";
      } else if (INTEGER.equalsIgnoreCase(mapValueType.textValue())) {
        typeMap = "Integer";
      } else {
        typeMap = mapValueType.textValue();
      }
    } else {
      final var valueSchema = mapNode.findPath(REF);
      if (Objects.nonNull(valueSchema)) {
        getRef(valueSchema, prefix, suffix);
      }
    }
    return typeMap;
  }

  public static String getPojoName(final String namePojo, final String prefix, final String suffix) {
    return StringUtils.defaultIfBlank(prefix, "")
           + StringUtils.capitalize(namePojo)
           + StringUtils.defaultIfBlank(suffix, "");
  }

  public static String[] splitName(final String name) {
    return name.split(DIVISOR);
  }

}
