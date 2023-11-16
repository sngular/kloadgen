package com.sngular.kloadgen.common.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

public final class ApiTool {

  public static final String FORMAT = "format";
  public static final String ALL_OF = "allOf";
  public static final String ANY_OF = "anyOf";
  public static final String ONE_OF = "oneOf";
  public static final String COMPONENTS = "components";
  public static final String SCHEMAS = "schemas";

  public static final String MESSAGES = "messages";
  public static final String REQUIRED = "required";

  public static final String PARAMETERS = "parameters";

  public static final String RESPONSES = "responses";

  private ApiTool() {
  }

  public static JsonNode findNodeValue(final JsonNode node, final String valueName) {
    return node.findValue(valueName);
  }

  public static String getType(final JsonNode schema) {
    return hasType(schema) ? getNodeAsString(schema, "type") : "";
  }

  public static Iterator<Entry<String, JsonNode>> getProperties(final JsonNode schema) {
    return getNode(schema, "properties").fields();
  }

  public static String getRefValue(final JsonNode schema) {
    return getNode(schema, "$ref").textValue();
  }

  public static JsonNode getAdditionalProperties(final JsonNode schema) {
    return getNode(schema, "additionalProperties");
  }

  public static String getFormat(final JsonNode schema) {
    return getNodeAsString(schema, FORMAT);
  }

  public static JsonNode getAllOf(final JsonNode schema) {
    return getNode(schema, ALL_OF);
  }

  public static JsonNode getAnyOf(final JsonNode schema) {
    return getNode(schema, ANY_OF);
  }

  public static JsonNode getOneOf(final JsonNode schema) {
    return getNode(schema, ONE_OF);
  }

  public static JsonNode getNode(final JsonNode schema, final String nodeName) {
    return schema.get(nodeName);
  }

  public static String getNodeAsString(final JsonNode schema, final String nodeName) {
    return hasNode(schema, nodeName) ? getNode(schema, nodeName).textValue() : null;
  }

  public static boolean getNodeAsBoolean(final JsonNode schema, final String nodeName) {
    return hasNode(schema, nodeName) && getNode(schema, nodeName).booleanValue();
  }

  public static Iterator<Entry<String, JsonNode>> getFieldIterator(final JsonNode schema) {
    return Objects.nonNull(schema) ? schema.fields() : IteratorUtils.emptyIterator();
  }

  public static String getName(final JsonNode node) {
    return hasNode(node, "name") ? getNodeAsString(node, "name") : node.textValue();
  }

  public static List<String> getEnumValues(final JsonNode schema) {
    return new ArrayList<>(CollectionUtils.collect(
      IteratorUtils.toList(schema.get("enum").elements()),
      getTextValue()));
  }

  public static JsonNode getItems(final JsonNode schema) {
    return getNode(schema, "items");
  }

  public static Map<String, JsonNode> getComponentSchemas(final JsonNode openApi) {
    return getSchemas(openApi, SCHEMAS);
  }

  public static Map<String, JsonNode> getComponentMessages(final JsonNode openApi) {
    return getSchemas(openApi, MESSAGES);
  }

  public static Map<String, JsonNode> getParameterSchemas(final JsonNode openApi) {
    return getSchemas(openApi, PARAMETERS);
  }

  public static Map<String, JsonNode> getResponseSchemas(final JsonNode openApi) {

    return getSchemas(openApi, RESPONSES);
  }

  public static Map<String, JsonNode> getSchemas(final JsonNode openApi, final String schemaType) {
    final var schemasMap = new HashMap<String, JsonNode>();

    if (hasNode(openApi, COMPONENTS)) {
      final var components = getNode(openApi, COMPONENTS);
      if (hasNode(components, schemaType)) {
        final var schemas = getNode(components, schemaType);
        final var schemasIt = schemas.fieldNames();
        schemasIt.forEachRemaining(name -> schemasMap.put(schemaType + "/" + name, getNode(schemas, name)));
      }
    }

    return schemasMap;
  }

  public static Map<String, JsonNode> getComponentSecuritySchemes(final JsonNode openApi) {
    final var schemasMap = new HashMap<String, JsonNode>();

    if (hasNode(openApi, COMPONENTS)) {
      final var components = getNode(openApi, COMPONENTS);
      if (hasNode(components, "securitySchemes")) {
        getNode(components, "securitySchemes").fields().forEachRemaining(schema -> schemasMap.put(schema.getKey(), schema.getValue()));
      }
    }

    return schemasMap;
  }

  public static String getNumberType(final JsonNode schema) {
    final String type;
    if (hasType(schema)) {
      type = switch (getType(schema)) {
        case TypeConstants.DOUBLE -> TypeConstants.DOUBLE;
        case TypeConstants.FLOAT -> TypeConstants.FLOAT;
        case TypeConstants.NUMBER -> TypeConstants.NUMBER;
        case TypeConstants.INT_64 -> TypeConstants.INT_64;
        case TypeConstants.INT_32 -> TypeConstants.INT_32;
        default -> TypeConstants.INTEGER;
      };
    } else {
      type = TypeConstants.INTEGER;
    }
    return type;
  }

  public static boolean hasItems(final JsonNode schema) {
    return hasNode(schema, "items");
  }

  public static boolean hasNode(final JsonNode schema, final String nodeName) {
    return Objects.nonNull(schema) && schema.has(nodeName);
  }

  public static boolean hasField(final JsonNode schema, final String... fieldNameArray) {
    final var nodeNamesList = Arrays.asList(fieldNameArray);
    return StringUtils.isNotEmpty(IteratorUtils.find(schema.fieldNames(), nodeNamesList::contains));
  }

  public static boolean hasRequired(final JsonNode schema) {
    return hasNode(schema, REQUIRED);
  }

  public static boolean hasType(final JsonNode schema) {
    return hasNode(schema, "type");
  }

  public static boolean hasRef(final JsonNode schema) {
    return hasNode(schema, "$ref");
  }

  public static boolean hasProperties(final JsonNode schema) {
    return hasNode(schema, "properties");
  }

  public static boolean hasContent(final JsonNode schema) {
    return hasNode(schema, "content");
  }

  public static boolean hasAdditionalProperties(final JsonNode schema) {
    return hasNode(schema, "additionalProperties");
  }

  public static boolean hasFormat(final JsonNode schema) {
    return hasNode(schema, "format");
  }

  public static boolean isObject(final JsonNode schema) {
    return hasType(schema) && TypeConstants.OBJECT.equalsIgnoreCase(getType(schema));
  }

  public static boolean isArray(final JsonNode schema) {
    return hasType(schema) && TypeConstants.ARRAY.equalsIgnoreCase(getType(schema));
  }

  public static boolean isComposed(final JsonNode schema) {
    return ApiTool.hasField(schema, ANY_OF, ALL_OF, ONE_OF);
  }

  public static boolean isString(final JsonNode schema) {
    return hasType(schema) && TypeConstants.STRING.equalsIgnoreCase(getType(schema));
  }

  public static boolean isBoolean(final JsonNode schema) {
    return hasType(schema) && TypeConstants.BOOLEAN.equalsIgnoreCase(getType(schema));
  }

  public static boolean isNumber(final JsonNode schema) {
    return hasType(schema)
           && (TypeConstants.INTEGER.equalsIgnoreCase(getType(schema))
            || TypeConstants.NUMBER.equalsIgnoreCase(getType(schema))
            || TypeConstants.INT_64.equalsIgnoreCase(getType(schema))
            || TypeConstants.INT_32.equalsIgnoreCase(getType(schema)));
  }

  public static boolean isEnum(final JsonNode schema) {
    return schema.has("enum");
  }

  public static boolean isAllOf(final JsonNode schema) {
    return hasNode(schema, ALL_OF);
  }

  public static boolean isAnyOf(final JsonNode schema) {
    return hasNode(schema, ANY_OF);
  }

  public static boolean isOneOf(final JsonNode schema) {
    return hasNode(schema, ONE_OF);
  }

  public static boolean isDateTime(final JsonNode schema) {
    final boolean isDateTime;
    if (hasType(schema) && TypeConstants.STRING.equalsIgnoreCase(getType(schema))) {
      if (hasNode(schema, FORMAT)) {
        isDateTime = "date".equalsIgnoreCase(getNode(schema, FORMAT).textValue())
                     || "date-time".equalsIgnoreCase(getNode(schema, FORMAT).textValue());
      } else {
        isDateTime = false;
      }
    } else {
      isDateTime = false;
    }
    return isDateTime;
  }

  public static boolean checkIfRequired(final JsonNode schema, final String fieldName) {
    boolean isRequired = false;
    if (hasNode(schema, REQUIRED)) {
      final var fieldIt = getNode(schema, REQUIRED).elements();
      while (fieldIt.hasNext() && !isRequired) {
        isRequired = fieldName.equalsIgnoreCase(fieldIt.next().textValue());
      }
    }
    return isRequired;
  }

  private static Transformer<JsonNode, String> getTextValue() {
    return JsonNode::asText;
  }

}
