package net.coru.kloadgen.config.valueserialized;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueSerializedConfigElementBeanInfoTest {

  private static final String VALUE_SUBJECT_NAME = "valueSubjectName";

  private static final String VALUE_SCHEMA_PROPERTIES = "valueSchemaProperties";

  private static final String VALUE_SCHEMA_TYPE = "valueSchemaType";

  private ValueSerializedConfigElementBeanInfo valueSerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    valueSerializedConfigElementBeanInfo = new ValueSerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    PropertyDescriptor[] propertyDescriptors = valueSerializedConfigElementBeanInfo.getPropertyDescriptors();
    assertThat(propertyDescriptors).hasSize(3);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(VALUE_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(VALUE_SCHEMA_TYPE);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(VALUE_SUBJECT_NAME);
  }
}