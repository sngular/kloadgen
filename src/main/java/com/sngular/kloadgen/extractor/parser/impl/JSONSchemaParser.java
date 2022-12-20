/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor.parser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.parser.SchemaParser;
import com.sngular.kloadgen.model.json.ArrayField;
import com.sngular.kloadgen.model.json.BooleanField;
import com.sngular.kloadgen.model.json.DateField;
import com.sngular.kloadgen.model.json.EnumField;
import com.sngular.kloadgen.model.json.Field;
import com.sngular.kloadgen.model.json.IntegerField;
import com.sngular.kloadgen.model.json.MapField;
import com.sngular.kloadgen.model.json.NumberField;
import com.sngular.kloadgen.model.json.ObjectField;
import com.sngular.kloadgen.model.json.Schema;
import com.sngular.kloadgen.model.json.StringField;
import com.sngular.kloadgen.model.json.UUIDField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class JSONSchemaParser implements SchemaParser {

  public static final String REQUIRED = "required";
  public static final String PROPERTIES = "properties";
  public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
  public static final String DEFINITIONS = "definitions";
  public static final String ID = "$id";
  public static final String SCHEMA = "$schema";
  public static final String REF = "$ref";

  public static final String TYPE = "type";
  public static final String TYPE_OBJECT = "object";
  public static final String TYPE_INTEGER = "integer";
  public static final String TYPE_NUMBER = "number";
  public static final String TYPE_ARRAY = "array";
  public static final String TYPE_BOOLEAN = "boolean";
  public static final String NULL = "null";

  public static final String ANY_OF = "anyOf";
  public static final String ALL_OF = "allOf";
  public static final String ONE_OF = "oneOf";

  public static final String ITEMS = "items";
  public static final String UNIQUE_ITEMS = "uniqueItems";
  public static final String MIN_ITEMS = "minItems";
  public static final String ZERO = "0";
  public static final String INTERNAL_MAP_FIELD = "internalMapField";

  public static final String HASHTAG = "#";
  public static final String ENUM = "enum";
  public static final String PATTERN = "pattern";
  public static final String MIN_LENGTH = "minLength";
  public static final String MAX_LENGTH = "maxLength";
  public static final String FORMAT = "format";
  public static final String DATE_TIME = "date-time";
  public static final String TIME = "time";
  public static final String DATE = "date";
  public static final String UUID = "uuid";

  public static final String MAXIMUM = "maximum";
  public static final String MINIMUM = "minimum";
  public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
  public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
  public static final String MULTIPLE_OF = "multipleOf";

  public static final String SEPARATOR_DOT = ".";
  public static final String SEPARATOR_SLASH = "/";

  public static final String FALSE = "false";

  public static final String ERROR_WRONG_JSON_SCHEMA = "Wrong Json Schema";
  public static final String ERROR_WRONG_JSON_SCHEMA_MISSING_DEFINITION = "Wrong Json Schema, Missing definition";
  public static final String ERROR_NOT_TYPE_OBJECT_FOUND = "Not Type Object found";
  public static final String ERROR_REFERENCE_NOT_SUPPORTED = "Reference not Supported: %s";
  public static final String ERROR_NOT_SUPPORTED_FILE = "Not supported file";
  public static final String ERROR_INCORRECT_TYPE_IN_COMBINATION = "Incorrect type in combination";
  public static final String ERROR_INCORRECT_COMBINATION_TYPES_AND_PROPERTIES_MIXED = "Incorrect combination, types and properties mixed";

  private static final Set<String> CYCLING_SET = new HashSet<>();

  private final ObjectMapper mapper = new ObjectMapper();

  private final Map<String, Field> definitionsMap = new HashMap<>();

  @Override
  public final Schema parse(final String jsonSchema) {
    definitionsMap.clear();
    final Schema schema;
    try {
      schema = parse(mapper.readTree(jsonSchema));
    } catch (final IOException e) {
      throw new KLoadGenException(ERROR_WRONG_JSON_SCHEMA, e);
    }
    return schema;
  }

  @Override
  public final Schema parse(final JsonNode jsonNode) {

    
    definitionsMap.clear();
    final List<Field> fields = new ArrayList<>();
    final Schema schema;

    final JsonNode definitions = jsonNode.path(DEFINITIONS);
    processDefinitions(definitions);

    final JsonNode schemaId = jsonNode.path(ID);
    final JsonNode schemaName = jsonNode.path(SCHEMA);
    final JsonNode requiredList = jsonNode.path(REQUIRED);
    final JsonNode type = jsonNode.path(TYPE);
    final String schemaType = getSafeType(jsonNode).toLowerCase();

    final List<String> requiredFields = new ArrayList<>();
    requiredList.elements().forEachRemaining(elm -> requiredFields.add(elm.textValue()));

    jsonNode.path(PROPERTIES).fields().forEachRemaining(field -> field.getValue().path(REQUIRED).elements()
                                                                          .forEachRemaining(elm -> requiredFields.add(field.getKey() + SEPARATOR_DOT + elm.textValue())));

    CollectionUtils.collect(jsonNode.path(PROPERTIES).fieldNames(),
                            fieldName -> buildProperty(fieldName, jsonNode.path(PROPERTIES).get(fieldName), requiredFields.contains(fieldName),
                                                       schemaType.equals(TYPE_OBJECT)), fields);

    schema = Schema.builder()
                   .id(schemaId.asText())
                   .name(schemaName.asText())
                   .requiredFields(requiredFields)
                   .type(type.asText())
                   .properties(fields)
                   .descriptions(definitionsMap.values())
                   .build();

    return schema;
  }

  private void processDefinitions(final JsonNode definitions) {
    for (Iterator<Entry<String, JsonNode>> it = definitions.fields(); it.hasNext();) {
      final Entry<String, JsonNode> definitionNode = it.next();
      if (!isRefNode(definitionNode.getValue())) {
        definitionsMap.putIfAbsent(definitionNode.getKey(), buildDefinition(definitionNode.getKey(), definitionNode.getValue(), definitions));
      } else if (isRefNodeSupported(definitionNode.getValue())) {
        final String referenceName = extractRefName(definitionNode.getValue());
        if (definitionsMap.containsKey(referenceName)) {
          definitionsMap.put(definitionNode.getKey(), buildDefinition(definitionNode.getKey(), definitionNode.getValue(), definitions));
        } else {
          if (!isRefNode(definitions.path(referenceName))) {
            if (CYCLING_SET.add(referenceName)) {
              definitionsMap.put(definitionNode.getKey(), buildDefinition(definitionNode.getKey(), definitions.path(referenceName), definitions));
              CYCLING_SET.remove(referenceName);
            } else {
              throw new KLoadGenException(ERROR_WRONG_JSON_SCHEMA_MISSING_DEFINITION);
            }
          } else {
            throw new KLoadGenException(ERROR_WRONG_JSON_SCHEMA_MISSING_DEFINITION);
          }
        }
      }
    }
  }

  private Field buildDefinition(final String fieldName, final JsonNode jsonNode, final JsonNode definitions) {
    return buildDefinition(fieldName, jsonNode, definitions, null, null);
  }

  private Field buildDefinition(final String fieldName, final JsonNode jsonNode, final JsonNode definitions, final Boolean required, final Boolean isParentObject) {
    final Field result;
    if (isAnyType(jsonNode)) {
      final String nodeType = getSafeType(jsonNode);
      if (Objects.isNull(nodeType)) {
        throw new KLoadGenException(ERROR_NOT_TYPE_OBJECT_FOUND);
      }
      switch (nodeType) {
        case TYPE_INTEGER:
          result = IntegerField.builder().name(fieldName).build();
          break;
        case TYPE_NUMBER:
          result = buildNumberField(fieldName, jsonNode);
          break;
        case TYPE_ARRAY:
          result = buildDefinitionArrayField(fieldName, jsonNode, definitions, checkRequiredCollection(isParentObject, required));
          break;
        case TYPE_OBJECT:
          result = buildDefinitionObjectField(fieldName, jsonNode, definitions, required, isParentObject);
          break;
        case TYPE_BOOLEAN:
          result = buildBooleanField(fieldName);
          break;
        default:
          result = buildStringField(fieldName, jsonNode);
          break;
      }
    } else if (isRefNode(jsonNode)) {
      final String referenceName = extractRefName(jsonNode);
      if (definitionsMap.containsKey(referenceName)) {
        result = definitionsMap.get(referenceName);
      } else {
        if (CYCLING_SET.add(referenceName)) {
          result = extractDefinition(referenceName, definitions, required, isParentObject);
          CYCLING_SET.remove(referenceName);
        } else {
          result = null;
        }
      }
    } else if (isCombine(jsonNode)) {
      if (Objects.nonNull(jsonNode.get(ANY_OF))) {
        result = chooseAnyOfDefinition(fieldName, jsonNode, ANY_OF, definitions);
      } else if (Objects.nonNull(jsonNode.get(ALL_OF))) {
        result = chooseAnyOfDefinition(fieldName, jsonNode, ALL_OF, definitions);
      } else {
        result = chooseAnyOfDefinition(fieldName, jsonNode, ONE_OF, definitions);
      }
    } else {
      result = buildDefinitionObjectField(fieldName, jsonNode, definitions);
    }
    return result;
  }

  private boolean isCombine(final JsonNode jsonNode) {
    return Objects.nonNull(jsonNode.get(ANY_OF))
           || Objects.nonNull(jsonNode.get(ALL_OF))
           || Objects.nonNull(jsonNode.get(ONE_OF));
  }

  private String getSafeType(final JsonNode jsonNode) {
    String nodeType = null;
    if (Objects.nonNull(jsonNode.findPath(TYPE))) {
      if (jsonNode.findPath(TYPE).isArray()) {
        nodeType = getNonNUll(jsonNode.findPath(TYPE).elements());
      } else {
        nodeType = jsonNode.findPath(TYPE).textValue().toLowerCase();
      }
    }
    return nodeType;
  }

  private String getNonNUll(final Iterator<JsonNode> typeIt) {
    String type = null;
    while (typeIt.hasNext() && Objects.isNull(type)) {
      type = typeIt.next().asText();
      if (NULL.equalsIgnoreCase(type)) {
        type = null;
      }
    }
    return type;
  }

  private Field extractDefinition(final String referenceName, final JsonNode definitions, final Boolean required, final Boolean isParentObject) {
    final JsonNode field = definitions.path(referenceName);
    Field definition = null;
    if (Objects.nonNull(field)) {
      definition = buildDefinition(referenceName, field, definitions, required, isParentObject);
      definitionsMap.put(referenceName, definition);
    }
    return definition;
  }

  private Field chooseAnyOfDefinition(final String fieldName, final JsonNode jsonNode, final String type, final JsonNode definitions) {
    final List<JsonNode> options = IteratorUtils.toList(jsonNode.get(type).elements());
    final int optionsNumber = options.size();
    final Field resultObject;
    switch (type) {
      case ANY_OF:
      case ONE_OF:
        resultObject = buildDefinition(fieldName, jsonNode.path(type).get(RandomUtils.nextInt(0, optionsNumber)), definitions);
        break;
      default:
        resultObject = buildDefinition(fieldName, jsonNode.path(type), definitions);
        break;
    }
    return resultObject;
  }

  private Field buildDefinitionArrayField(final String fieldName, final JsonNode jsonNode, final JsonNode definitions, final Boolean required) {
    return buildArrayField(fieldName, jsonNode, buildDefinition(null, jsonNode.path(ITEMS), definitions,
                                                                !StringUtils.isBlank(jsonNode.path(MIN_ITEMS).asText()) && !jsonNode.path(MIN_ITEMS).asText().equals(ZERO), null),
                           required);
  }

  private Field buildDefinitionObjectField(final String fieldName, final JsonNode jsonNode, final JsonNode definitions) {
    return buildDefinitionObjectField(fieldName, jsonNode, definitions, null, null);
  }

  private Field buildDefinitionObjectField(final String fieldName, final JsonNode jsonNode, final JsonNode definitions, final Boolean required, final Boolean isParentObject) {
    final Field returnField;
    final List<Field> properties = new ArrayList<>();
    if (Objects.nonNull(jsonNode.get(PROPERTIES))) {
      final JsonNode requiredList = jsonNode.path(REQUIRED);
      final List<String> strRequired = new ArrayList<>();
      requiredList.elements().forEachRemaining(elm -> strRequired.add(elm.textValue()));
      CollectionUtils.filter(strRequired, StringUtils::isNotEmpty);
      CollectionUtils.collect(jsonNode.path(PROPERTIES).fields(),
                              field -> buildDefinition(field.getKey(), field.getValue(), definitions, strRequired.contains(field.getKey()), true), properties);
      if (required != null) {
        returnField = ObjectField.builder().name(fieldName).properties(properties).required(strRequired).isFieldRequired(required).build();
      } else {
        returnField = ObjectField.builder().name(fieldName).properties(properties).required(strRequired).build();
      }
    } else if (!jsonNode.path(ADDITIONAL_PROPERTIES).isNull() && !jsonNode.path(ADDITIONAL_PROPERTIES).isEmpty()) {
      final Field internalMapField = buildDefinition(INTERNAL_MAP_FIELD, jsonNode.path(ADDITIONAL_PROPERTIES), definitions, required, null);
      returnField = MapField.builder().name(fieldName).mapType(internalMapField).isFieldRequired(checkRequiredCollection(isParentObject, required)).build();
    } else if (Objects.nonNull(jsonNode.get(REF))) {
      final String referenceName = extractRefName(jsonNode);
      if (definitionsMap.containsKey(referenceName)) {
        returnField = definitionsMap.get(referenceName).cloneField(fieldName);
      } else if (CYCLING_SET.add(referenceName)) {
        returnField = extractDefinition(referenceName, definitions, required, isParentObject);
      } else {
        returnField = null;
      }
    } else {
      final List<Field> fieldList = new ArrayList<>();
      jsonNode.fields()
              .forEachRemaining(property -> fieldList.add(buildProperty(property.getKey(), property.getValue())));
      returnField = ObjectField.builder().name(fieldName).properties(fieldList).build();
    }
    return returnField;
  }

  private boolean isRefNodeSupported(final JsonNode jsonNode) {
    final String reference = jsonNode.get(REF).asText();
    return reference.startsWith(HASHTAG);
  }

  private boolean isRefNode(final JsonNode jsonNode) {
    return Objects.nonNull(jsonNode.get(REF));
  }

  private String extractRefName(final JsonNode jsonNode) {
    final String reference = jsonNode.get(REF).asText();
    return extractRefName(reference);
  }

  private String extractRefName(final String jsonNodeName) {
    return jsonNodeName.substring(jsonNodeName.lastIndexOf(SEPARATOR_SLASH) + 1);
  }

  private Field buildProperty(final String fieldName, final JsonNode jsonNode) {
    return buildProperty(fieldName, jsonNode, null, null);
  }

  private Field buildProperty(final String fieldName, final JsonNode jsonNode, final Boolean required) {
    return buildProperty(fieldName, jsonNode, required, null);
  }

  private Field buildProperty(final String fieldName, final JsonNode jsonNode, final Boolean required, final Boolean isParentObject) {
    final Field result;
    if (isRefNode(jsonNode)) {
      if (isRefNodeSupported(jsonNode)) {
        final String referenceName = extractRefName(jsonNode);
        if (TYPE_ARRAY.equalsIgnoreCase(jsonNode.findPath(TYPE).textValue())) {
          result = buildArrayField(fieldName, jsonNode, definitionsMap.get(referenceName).cloneField(null), checkRequiredCollection(isParentObject, required));
        } else {
          result = propagateRequired(definitionsMap.get(referenceName).cloneField(fieldName), required, isParentObject);
        }
      } else {
        throw new KLoadGenException(String.format(ERROR_REFERENCE_NOT_SUPPORTED, extractRefName(jsonNode)));
      }
    } else if (isAnyType(jsonNode)) {
      result = buildField(fieldName, jsonNode, required, isParentObject);
    } else if (isCombine(jsonNode)) {
      if (Objects.nonNull(jsonNode.get(ANY_OF))) {
        result = chooseAnyOf(fieldName, jsonNode, ANY_OF);
      } else if (Objects.nonNull(jsonNode.get(ALL_OF))) {
        result = chooseAnyOf(fieldName, jsonNode, ALL_OF);
      } else {
        result = chooseAnyOf(fieldName, jsonNode, ONE_OF);
      }
    } else if (hasProperties(jsonNode)) {
      result = buildObjectField(fieldName, jsonNode, required);
    } else {
      throw new KLoadGenException(ERROR_NOT_SUPPORTED_FILE);
    }
    return result;
  }

  private Field buildField(final String fieldName, final JsonNode jsonNode) {
    return buildField(fieldName, jsonNode, null, null);
  }

  private Field buildField(final String fieldName, final JsonNode jsonNode, final Boolean required, final Boolean isParentObject) {
    final Field result;
    final String nodeType = getSafeType(jsonNode).toLowerCase();

    switch (nodeType) {
      case TYPE_INTEGER:
        result = IntegerField.builder().name(fieldName).build();
        break;
      case TYPE_NUMBER:
        result = buildNumberField(fieldName, jsonNode);
        break;
      case TYPE_ARRAY:
        result = buildArrayField(fieldName, jsonNode, checkRequiredCollection(isParentObject, required));
        break;
      case TYPE_OBJECT:
        result = buildObjectField(fieldName, jsonNode, required, isParentObject);
        break;
      case TYPE_BOOLEAN:
        result = buildBooleanField(fieldName);
        break;
      default:
        result = buildStringField(fieldName, jsonNode);
        break;
    }

    return result;
  }

  private boolean hasProperties(final JsonNode jsonNode) {
    return Objects.nonNull(jsonNode.get(PROPERTIES));
  }

  private Field buildStringField(final String fieldName, final JsonNode jsonNode) {
    final Field result;
    if (Objects.isNull(jsonNode.get(ENUM))) {
      final String regexStr = getSafeText(jsonNode, PATTERN);
      final int minLength = getSafeInt(jsonNode, MIN_LENGTH);
      final int maxLength = getSafeInt(jsonNode, MAX_LENGTH);
      final String format = getSafeText(jsonNode, FORMAT);
      if (Objects.nonNull(format)) {
        if (Set.of(DATE_TIME, TIME, DATE).contains(format)) {
          result = DateField.builder().name(fieldName).format(format).build();
        } else if (UUID.equals(format)) {
          result = UUIDField.builder().name(fieldName).build();
        } else {
          result = StringField.builder().name(fieldName).format(format).build();
        }
      } else {
        result = StringField.builder().name(fieldName).regex(regexStr).minLength(minLength).maxlength(maxLength).format(format).build();
      }
    } else {
      result = buildEnumField(fieldName, jsonNode);
    }
    return result;
  }

  private int getSafeInt(final JsonNode node, final String field) {
    int result = 0;
    if (Objects.nonNull(node.get(field))) {
      result = node.get(field).asInt();
    }
    return result;
  }

  private String getSafeText(final JsonNode node, final String field) {
    String result = null;
    if (Objects.nonNull(node.get(field))) {
      result = node.get(field).asText();
    }
    return result;
  }

  private Field buildEnumField(final String fieldName, final JsonNode jsonNode) {
    List<String> valueList = new ArrayList<>();
    if (jsonNode.get(ENUM).isArray()) {
      valueList = extractValues(jsonNode.get(ENUM).elements());
    }
    return EnumField.builder().name(fieldName).defaultValue(valueList.get(0)).enumValues(valueList).build();
  }

  private List<String> extractValues(final Iterator<JsonNode> enumValueList) {
    final List<String> valueList = new ArrayList<>();
    while (enumValueList.hasNext()) {
      valueList.add(enumValueList.next().asText());
    }
    return valueList;
  }

  private boolean isAnyType(final JsonNode node) {
    return Objects.nonNull(node.get(TYPE));
  }

  private Field chooseAnyOf(final String fieldName, final JsonNode jsonNode, final String type) {
    final List<JsonNode> properties = IteratorUtils.toList(jsonNode.get(type).elements());
    final int optionsNumber = properties.size();
    final Field resultObject;
    if (IterableUtils.matchesAll(properties, property -> property.hasNonNull(PROPERTIES)
                                                         || property.hasNonNull(REF))) {
      switch (type) {
        case ANY_OF:
        case ONE_OF:
          resultObject = buildCombinedField(fieldName, Collections.singletonList(properties.get(RandomUtils.nextInt(0, optionsNumber))));
          break;
        default:
          resultObject = buildCombinedField(fieldName, properties);
          break;
      }
    } else if (IterableUtils.matchesAll(properties, property -> !property.hasNonNull(PROPERTIES)
                                                                && !property.hasNonNull(REF))) {
      switch (type) {
        case ANY_OF:
        case ONE_OF:
          resultObject = buildCombinedType(fieldName, properties.get(RandomUtils.nextInt(0, optionsNumber)));
          break;
        default:
          throw new KLoadGenException(ERROR_INCORRECT_TYPE_IN_COMBINATION);
      }
    } else {
      throw new KLoadGenException(ERROR_INCORRECT_COMBINATION_TYPES_AND_PROPERTIES_MIXED);
    }
    return resultObject;
  }

  private Field buildCombinedType(final String fieldName, final JsonNode property) {
    return buildField(fieldName, property);
  }

  private Field buildCombinedField(final String fieldName, final List<JsonNode> properties) {
    return buildCombinedField(fieldName, properties, null);
  }

  private Field buildCombinedField(final String fieldName, final List<JsonNode> properties, final Boolean required) {
    final Field resultObject;
    final List<Field> fields = new ArrayList<>();
    for (JsonNode property : properties) {
      if (isRefNode(property)) {
        final String referenceName = extractRefName(property);
        final Field refField = definitionsMap.get(referenceName).cloneField(fieldName);
        if (isAnyType(property)) {
          fields.add(refField);
        } else {
          fields.addAll(refField.getProperties());
        }
      } else {
        if (Objects.nonNull(property.get(PROPERTIES))) {
          for (Iterator<Entry<String, JsonNode>> it = property.get(PROPERTIES).fields(); it.hasNext();) {
            final Entry<String, JsonNode> innProperty = it.next();
            fields.add(buildProperty(innProperty.getKey(), innProperty.getValue(), required));
          }
        }
      }
    }
    resultObject = buildObjectField(fieldName, fields, required);
    return resultObject;
  }

  private Field buildNumberField(final String fieldName, final JsonNode jsonNode) {
    final String maximum = jsonNode.path(MAXIMUM).asText(ZERO);
    final String minimum = jsonNode.path(MINIMUM).asText(ZERO);
    final String exclusiveMaximum = jsonNode.path(EXCLUSIVE_MAXIMUM).asText(ZERO);
    final String exclusiveMinimum = jsonNode.path(EXCLUSIVE_MINIMUM).asText(ZERO);
    final String multipleOf = jsonNode.path(MULTIPLE_OF).asText(ZERO);

    return NumberField
        .builder()
        .name(fieldName)
        .maximum(safeGetNumber(maximum))
        .minimum(safeGetNumber(minimum))
        .exclusiveMaximum(safeGetNumber(exclusiveMaximum))
        .exclusiveMinimum(safeGetNumber(exclusiveMinimum))
        .multipleOf(safeGetNumber(multipleOf))
        .build();
  }

  private Number safeGetNumber(final String numberStr) {
    final Number number;
    if (numberStr.contains(SEPARATOR_DOT)) {
      number = Float.parseFloat(numberStr);
    } else {
      number = Long.parseLong(numberStr);
    }
    return number;
  }

  private Field buildArrayField(final String fieldName, final JsonNode jsonNode, final Boolean required) {
    return buildArrayField(fieldName, jsonNode, buildProperty(null, jsonNode.path(ITEMS),
                                                              !StringUtils.isBlank(jsonNode.path(MIN_ITEMS).asText()) && !jsonNode.path(MIN_ITEMS).asText().equals(ZERO)),
                           required);
  }

  private Field buildArrayField(final String fieldName, final JsonNode jsonNode, final Field value, final Boolean required) {
    final String minItems = jsonNode.path(MIN_ITEMS).asText(ZERO);
    final String uniqueItems = jsonNode.path(UNIQUE_ITEMS).asText(FALSE);
    return ArrayField
        .builder()
        .name(fieldName)
        .value(value)
        .isFieldRequired(required)
        .minItems(Integer.parseInt(minItems))
        .uniqueItems(Boolean.parseBoolean(uniqueItems))
        .build();
  }

  private Field buildObjectField(final String fieldName, final JsonNode jsonNode, final Boolean required) {
    return buildObjectField(fieldName, jsonNode, required, null);
  }

  private Field buildObjectField(final String fieldName, final JsonNode jsonNode, final Boolean required, final Boolean isParentObject) {
    final Field returnField;
    final List<Field> properties = new ArrayList<>();
    final JsonNode requiredList = jsonNode.path(REQUIRED);
    final List<String> strRequired = new ArrayList<>();
    requiredList.elements().forEachRemaining(elm -> strRequired.add(elm.textValue()));

    CollectionUtils.filter(strRequired, StringUtils::isNotEmpty);
    if (!isCombine(jsonNode)) {
      if (jsonNode.path(ADDITIONAL_PROPERTIES).isNull() || jsonNode.path(ADDITIONAL_PROPERTIES).isEmpty()) {
        CollectionUtils.collect(jsonNode.path(PROPERTIES).fields(),
                                field -> buildProperty(field.getKey(), field.getValue(), strRequired.contains(field.getKey()), true),
                                properties);
        if (required != null) {
          returnField = ObjectField.builder().name(fieldName).properties(properties).required(strRequired).isFieldRequired(required).build();
        } else {
          returnField = ObjectField.builder().name(fieldName).properties(properties).required(strRequired).build();
        }
      } else {
        final JsonNode fieldReaded = jsonNode.path(ADDITIONAL_PROPERTIES);
        final Field field2 = buildProperty(INTERNAL_MAP_FIELD, fieldReaded, false);
        returnField = MapField.builder().name(fieldName).mapType(field2).isFieldRequired(checkRequiredCollection(isParentObject, required)).build();
      }


    } else {
      if (Objects.nonNull(jsonNode.get(ANY_OF))) {
        returnField = chooseAnyOf(fieldName, jsonNode, ANY_OF);
      } else if (Objects.nonNull(jsonNode.get(ALL_OF))) {
        returnField = chooseAnyOf(fieldName, jsonNode, ALL_OF);
      } else {
        returnField = chooseAnyOf(fieldName, jsonNode, ONE_OF);
      }
    }
    return returnField;
  }

  private Field buildObjectField(final String fieldName, final List<Field> properties, final Boolean required) {
    return ObjectField.builder().name(fieldName).properties(properties).isFieldRequired(required != null && required).build();
  }

  private Field buildBooleanField(final String fieldName) {
    return BooleanField.builder().name(fieldName).build();
  }

  private Boolean checkRequiredCollection(final Boolean isParentObject, final Boolean required) {
    final boolean isRequired = required != null && required;
    return isParentObject != null && isParentObject || isRequired;
  }

  private Field propagateRequired(final Field fieldDefinition, final Boolean required, final Boolean isParentObject) {
    final Field result;

    if (fieldDefinition instanceof ObjectField) {
      result = ObjectField.builder().name(fieldDefinition.getName())
                          .properties(fieldDefinition.getProperties())
                          .required(((ObjectField) fieldDefinition).getRequired())
                          .isFieldRequired(required).build();
    } else if (fieldDefinition instanceof ArrayField) {
      result = ArrayField.builder()
                         .name(fieldDefinition.getName())
                         .values(((ArrayField) fieldDefinition).getValues())
                         .isFieldRequired(checkRequiredCollection(isParentObject, required))
                         .minItems(((ArrayField) fieldDefinition).getMinItems())
                         .uniqueItems(((ArrayField) fieldDefinition).isUniqueItems())
                         .build();
    } else if (fieldDefinition instanceof MapField) {
      result = MapField.builder()
                       .name(fieldDefinition.getName())
                       .mapType(((MapField) fieldDefinition).getMapType())
                       .isFieldRequired(checkRequiredCollection(isParentObject, required)).build();
    } else {
      result = fieldDefinition;
    }
    return result;
  }


}
