package net.coru.kloadgen.model;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;

public class FieldValueMapping extends AbstractTestElement {

    public static final String FIELD_NAME = "fieldName";
    public static final String VALUE_EXPRESSION = "valueExpression";

    public static final String VALUE_LENGTH = "valueLength";
    public static final String FIELD_VALUES_LIST = "fieldValuesList";

    public String getFieldName() {
        return getPropertyAsString(FIELD_NAME);
    }

    public void setFieldName(String fieldName) {
        setProperty(FIELD_NAME, fieldName);
    }

    public String getValueExpression() {
        return getPropertyAsString(VALUE_EXPRESSION);
    }

    public void setValueExpression(String propertyValue) {
        setProperty(VALUE_EXPRESSION, propertyValue);
    }

    public Integer getValueLength() {
        return getPropertyAsInt(VALUE_LENGTH);
    }

    public void setValueLength(Integer valueLength) {
        setProperty(VALUE_LENGTH, valueLength);
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
        setProperty(FIELD_VALUES_LIST, fieldValuesList.replace("[","").replace("]",""));
    }
}
