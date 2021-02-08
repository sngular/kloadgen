/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.model;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public static final String FIELD_NAME = "fieldName";
    public static final String FIELD_TYPE = "fieldType";
    public static final String VALUE_LENGTH = "valueLength";
    public static final String FIELD_VALUES_LIST = "fieldValuesList";

    private String fieldName;
    private String fieldType;
    private Integer valueLength;
    private String fieldValueList;

    private Map<ConstraintTypeEnum, String> constrains = new EnumMap<>(ConstraintTypeEnum.class);

    private static final ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    public FieldValueMapping(String fieldName, String fieldType) {
        this.setFieldName(fieldName);
        this.setValueLength(0);
        this.setFieldType(fieldType);
    }

    public FieldValueMapping(String fieldName, String fieldType, Integer valueLength, String valueList) {
        this.setFieldName(fieldName);
        this.setValueLength(Objects.requireNonNullElse(valueLength, 0));
        this.setFieldType(fieldType);
        this.setFieldValuesList(valueList);
    }

    @Builder
    public FieldValueMapping(String fieldName, String fieldType, Integer valueLength, String fieldValueList,
        Map<ConstraintTypeEnum, String> constrains) {
        this.setFieldName(fieldName);
        this.setValueLength(Objects.requireNonNullElse(valueLength, 0));
        this.setFieldType(fieldType);
        this.setFieldValuesList(Objects.requireNonNullElse(fieldValueList, ""));
        this.constrains = constrains;
    }

    public String getFieldName() {
        return getPropertyAsString(FIELD_NAME);
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        setProperty(FIELD_NAME, fieldName);
    }

    public Integer getValueLength() {
        return getPropertyAsInt(VALUE_LENGTH);
    }

    public void setValueLength(Integer valueLength) {
        this.valueLength = valueLength;
        setProperty(VALUE_LENGTH, valueLength);
    }

    public String getFieldType() {
        return getPropertyAsString(FIELD_TYPE);
    }

    public void setFieldType(String propertyValue) {
        this.fieldType = propertyValue;
        setProperty(FIELD_TYPE, propertyValue);
    }

    public List<String> getFieldValuesList() {
        List<String> result = new ArrayList<>();
        String inputFieldValueList = getPropertyAsString(FIELD_VALUES_LIST);
        if (StringUtils.isNotBlank(inputFieldValueList) && !"[]".equalsIgnoreCase(inputFieldValueList)) {
            try {
                JsonNode nodes = mapper.readTree("[" + inputFieldValueList + "]");
                Iterator<JsonNode> nodeElements = nodes.elements();
                while (nodeElements.hasNext()) {
                    result.add(nodeElements.next().toString());
                }
            } catch (JsonProcessingException e) {
                result.addAll(asList(inputFieldValueList.split(",", - 1)));
            }

        }
        return result;
    }

    public void setFieldValuesList(String fieldValuesList) {
        this.fieldValueList = fieldValuesList;
        setProperty(FIELD_VALUES_LIST, fieldValuesList);
    }

    public void init() {
        this.setName("Object Field");
    }

    public Map<ConstraintTypeEnum, String> getConstrains() {
        return constrains;
    }

    public static class FieldValueMappingBuilder {

        private final Map<ConstraintTypeEnum, String> constrains = new EnumMap<>(ConstraintTypeEnum.class);

        public FieldValueMappingBuilder constrain(ConstraintTypeEnum key, String value) {
            constrains.putIfAbsent(key, value);
            return this;
        }

        public FieldValueMappingBuilder constrains(Map<ConstraintTypeEnum, String> newConstrains) {
            constrains.putAll(newConstrains);
            return this;
        }

        public FieldValueMappingBuilder clearConstrains() {
            constrains.clear();
            return this;
        }
    }
}
