package net.coru.kloadgen.model;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;

@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class FieldValueMapping extends AbstractTestElement {

    public static final String FIELD_NAME = "fieldName";
    public static final String VALUE_EXPRESSION = "valueExpression";
    public static final String VALUE_LENGTH = "valueLength";
    public static final String FIELD_VALUES_LIST = "fieldValuesList";

    private String fieldName;
    private String valueExpression;
    private Integer valueLength;
    private String fieldValueList;

    public FieldValueMapping(String fieldName, String valueExpression) {
        this.setFieldName(fieldName);
        this.setValueLength(0);
        this.setValueExpression(valueExpression);
    }

    public FieldValueMapping(String fieldName, String valueExpression,  Integer valueLength, String valueList) {
        this.setFieldName(fieldName);
        this.setValueLength(valueLength);
        this.setValueExpression(valueExpression);
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

    public String getValueExpression() {
        return getPropertyAsString(VALUE_EXPRESSION);
    }

    public void setValueExpression(String propertyValue) {
        this.valueExpression = valueExpression;
        setProperty(VALUE_EXPRESSION, propertyValue);
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
