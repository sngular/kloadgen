/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;

@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FieldValueMapping extends AbstractTestElement {

  public static final String FIELD_CONSTRAINTS = "constraints";

  public static final String FIELD_REQUIRED = "required";

  public static final String FIELD_ANCESTOR_REQUIRED = "ancestorRequired";

  public static final String FIELD_NAME = "fieldName";

  public static final String FIELD_TYPE = "fieldType";

  public static final String VALUE_LENGTH = "valueLength";

  public static final String FIELD_VALUES_LIST = "fieldValuesList";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  private String fieldName;

  private String fieldType;

  private Integer valueLength;

  private String fieldValueList = "";

  private Boolean required = true;

  private Boolean isAncestorRequired = true;

  private Map<ConstraintTypeEnum, String> constraints = new EnumMap<>(ConstraintTypeEnum.class);

  @Builder
  public FieldValueMapping(
      final String fieldName, final String fieldType, final Integer valueLength, final String fieldValueList,
      final Map<ConstraintTypeEnum, String> constraints, final Boolean required, final Boolean isAncestorRequired) {
    this.setFieldName(fieldName);
    this.setValueLength(Objects.requireNonNullElse(valueLength, 0));
    this.setFieldType(fieldType);
    this.setFieldValuesList(Objects.requireNonNullElse(fieldValueList, ""));
    this.setConstraints(constraints);
    this.setRequired(required != null && required);
    this.setAncestorRequired(isAncestorRequired != null && isAncestorRequired);
  }

  public final String getFieldName() {
    return getPropertyAsString(FIELD_NAME);
  }

  public final void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
    setProperty(FIELD_NAME, fieldName);
  }

  public final Integer getValueLength() {
    return getPropertyAsInt(VALUE_LENGTH);
  }

  public final void setValueLength(final Integer valueLength) {
    this.valueLength = valueLength;
    setProperty(VALUE_LENGTH, valueLength);
  }

  public final String getFieldType() {
    return getPropertyAsString(FIELD_TYPE);
  }

  public final void setFieldType(final String propertyValue) {
    this.fieldType = propertyValue;
    setProperty(FIELD_TYPE, propertyValue);
  }

  public final List<String> getFieldValuesList() {
    final List<String> result = new ArrayList<>();
    final String inputFieldValueList = getPropertyAsString(FIELD_VALUES_LIST);
    String inputFieldValueAux;
    if (StringUtils.isNotBlank(inputFieldValueList) && !"[]".equalsIgnoreCase(inputFieldValueList)) {
      try {
        inputFieldValueAux = inputFieldValueList;

        if (inputFieldValueAux.charAt(0) != '[') {
          inputFieldValueAux = "[" + inputFieldValueAux;
        }
        if (inputFieldValueAux.charAt(inputFieldValueAux.length() - 1) != ']') {
          inputFieldValueAux += "]";
        }
        final JsonNode nodes = OBJECT_MAPPER.readTree(inputFieldValueAux);
        final Iterator<JsonNode> nodeElements = nodes.elements();
        while (nodeElements.hasNext()) {
          result.add(nodeElements.next().toString());

        }
      } catch (final JsonProcessingException ex) {
        // Warning: even though IntelliJ say that can be simplified, it can't be simplified!! (test fails)

        if (inputFieldValueList.startsWith("[") && inputFieldValueList.endsWith("]")) {
          final String pattern = "(?<=\\[?)((([À-ÿ\\p{Alnum}\\p{Punct}&&[^,\\[\\]]]+:([À-ÿ\\p{Alnum}\\p{Punct}&&[^,\\[\\]]]+|\\[([À-ÿ\\p{Alnum}\\p{Punct}&&[^,\\[\\]]]+,"
                                 + "[À-ÿ\\p{Alnum}\\p{Punct}&&[^,\\[\\]]]+|)*]))|[À-ÿ\\p{Alnum}\\p{Punct}&&[^,\\[\\]]]+)(?=[]|,]))";
          final Pattern r = Pattern.compile(pattern);
          final Matcher matcher = r.matcher(inputFieldValueList.trim());
          while (matcher.find()) {
            result.add(matcher.group(0));
          }
        } else {
          final String pattern = "([À-ÿ\\p{Alnum}\\p{Punct}&&[^,\\[\\]]][À-ÿ\\s\\p{Alnum}\\p{Punct}&&[^,\\[\\]]]+)[^,\\s]?+";
          final Pattern r = Pattern.compile(pattern);
          final Matcher matcher = r.matcher(inputFieldValueList.trim());
          while (matcher.find()) {
            result.add(matcher.group(0));
          }
        }

      }
    }

    return result;
  }

  public final void setFieldValuesList(final String fieldValuesList) {
    this.fieldValueList = fieldValuesList;
    setProperty(FIELD_VALUES_LIST, fieldValuesList);
  }

  public final Boolean getRequired() {
    return getPropertyAsBoolean(FIELD_REQUIRED);
  }

  public final void setRequired(final Boolean required) {
    this.required = required;
    setProperty(FIELD_REQUIRED, required);
  }

  public final Boolean getAncestorRequired() {
    return getPropertyAsBoolean(FIELD_ANCESTOR_REQUIRED);
  }

  public final void setAncestorRequired(final Boolean ancestorRequired) {
    this.isAncestorRequired = ancestorRequired;
    setProperty(FIELD_ANCESTOR_REQUIRED, ancestorRequired);
  }

  public final void init() {
    this.setName("Object Field");
  }

  public final Map<ConstraintTypeEnum, String> getConstraints() {
    return constraints;
  }

  public final void setConstraints(final Map<ConstraintTypeEnum, String> constraints) {
    this.constraints = constraints;
  }

  public static final class FieldValueMappingBuilder {

    private final Map<ConstraintTypeEnum, String> constraints = new EnumMap<>(ConstraintTypeEnum.class);

    public FieldValueMappingBuilder constraint(final ConstraintTypeEnum key, final String value) {
      constraints.putIfAbsent(key, value);
      return this;
    }

    public FieldValueMappingBuilder constraints(final Map<ConstraintTypeEnum, String> newConstraints) {
      constraints.putAll(newConstraints);
      return this;
    }

    public FieldValueMappingBuilder clearConstraints() {
      constraints.clear();
      return this;
    }
  }
}
