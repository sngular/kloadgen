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

  private static final String VALUE_NAME_STRATEGY = "valueNameStrategy";

  private static final String VALUE_SERIALIZER_CONFIGURATION = "valueSerializerConfiguration";

  private ValueSerializedConfigElementBeanInfo valueSerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    valueSerializedConfigElementBeanInfo = new ValueSerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    PropertyDescriptor[] propertyDescriptors = valueSerializedConfigElementBeanInfo.getPropertyDescriptors();
    assertThat(propertyDescriptors).hasSize(5);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(VALUE_NAME_STRATEGY);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(VALUE_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(VALUE_SCHEMA_TYPE);
    assertThat(propertyDescriptors[3].getName()).isEqualTo(VALUE_SERIALIZER_CONFIGURATION);
    assertThat(propertyDescriptors[4].getName()).isEqualTo(VALUE_SUBJECT_NAME);
  }
}