package net.coru.kloadgen.config.keyserialized;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeySerializedConfigElementBeanInfoTest {

  private static final String KEY_SUBJECT_NAME = "keySubjectName";

  private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

  private static final String KEY_SCHEMA_TYPE = "keySchemaType";

  private static final String KEY_NAME_STRATEGY = "keyNameStrategy";

  private static final String KEY_SERIALIZER_CONFIGURATION = "keySerializerConfiguration";

  private KeySerializedConfigElementBeanInfo keySerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    keySerializedConfigElementBeanInfo = new KeySerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    PropertyDescriptor[] propertyDescriptors = keySerializedConfigElementBeanInfo.getPropertyDescriptors();
    assertThat(propertyDescriptors).hasSize(5);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(KEY_NAME_STRATEGY);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(KEY_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(KEY_SCHEMA_TYPE);
    assertThat(propertyDescriptors[3].getName()).isEqualTo(KEY_SERIALIZER_CONFIGURATION);
    assertThat(propertyDescriptors[4].getName()).isEqualTo(KEY_SUBJECT_NAME);
  }
}