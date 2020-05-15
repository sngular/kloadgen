package net.coru.kloadgen.model;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;

@Builder
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

    public FieldValueMapping(String fieldName, String fieldType) {
        this.setFieldName(fieldName);
        this.setValueLength(0);
        this.setFieldType(fieldType);
    }

    public FieldValueMapping(String fieldName, String fieldType,  Integer valueLength, String valueList) {
        this.setFieldName(fieldName);
        this.setValueLength(valueLength);
        this.setFieldType(fieldType);
        this.setFieldValuesList(valueList);
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
        List<String> result;
        String fieldValueList = getPropertyAsString(FIELD_VALUES_LIST);
        if (StringUtils.isNotBlank(fieldValueList) && !"[]".equalsIgnoreCase(fieldValueList)) {
            result = asList(fieldValueList.split(",", -1));
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    public void setFieldValuesList(String fieldValuesList) {
        this.fieldValueList = fieldValuesList;
        setProperty(FIELD_VALUES_LIST, fieldValuesList.replace("[","").replace("]",""));
    }

    public void init() {
        this.setName("Object Field");
    }
}
