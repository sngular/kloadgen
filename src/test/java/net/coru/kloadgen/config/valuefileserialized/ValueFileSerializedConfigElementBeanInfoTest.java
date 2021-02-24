package net.coru.kloadgen.config.valuefileserialized;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueFileSerializedConfigElementBeanInfoTest {

  private static final String VALUE_SUBJECT_NAME = "valueSubjectName";

  private static final String VALUE_SCHEMA_PROPERTIES = "valueSchemaProperties";

  private static final String VALUE_SCHEMA_TYPE = "valueSchemaType";

  private static final String VALUE_SCHEMA_DEFINITION = "valueSchemaDefinition";

  private static final String SERIALIZER_PROPERTY = "valueSerializerConfiguration";

  private ValueFileSerializedConfigElementBeanInfo valueFileSerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    valueFileSerializedConfigElementBeanInfo = new ValueFileSerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    PropertyDescriptor[] propertyDescriptors = valueFileSerializedConfigElementBeanInfo.getPropertyDescriptors();
    assertThat(propertyDescriptors).hasSize(5);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(VALUE_SCHEMA_DEFINITION);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(VALUE_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(VALUE_SCHEMA_TYPE);
    assertThat(propertyDescriptors[3].getName()).isEqualTo(SERIALIZER_PROPERTY);
    assertThat(propertyDescriptors[4].getName()).isEqualTo(VALUE_SUBJECT_NAME);
  }
}