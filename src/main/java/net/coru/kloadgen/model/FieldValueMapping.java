package net.coru.kloadgen.model;

import org.apache.jmeter.testelement.AbstractTestElement;

public class FieldValueMapping extends AbstractTestElement {

    public static final String FIELD_NAME = "fieldName";
    public static final String VALUE_EXPRESSION = "valueExpression";

    public String getFieldName() {
        return getProperty(FIELD_NAME).getStringValue();
    }

    public void setFieldName(String fieldName) {
        setProperty(FIELD_NAME, fieldName);
    }

    public String getValueExpression() {
        return getProperty(VALUE_EXPRESSION).getStringValue();
    }

    public void setValueExpression(String propertyValue) {
        setProperty(VALUE_EXPRESSION, propertyValue);
    }

}
