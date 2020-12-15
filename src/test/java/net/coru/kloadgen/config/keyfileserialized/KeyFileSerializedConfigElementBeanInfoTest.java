package net.coru.kloadgen.config.keyfileserialized;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeyFileSerializedConfigElementBeanInfoTest {

  private static final String KEY_NAME = "keyName";

  private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

  private static final String KEY_SCHEMA_TYPE = "keySchemaType";

  private static final String KEY_SCHEMA_DEFINITION = "keySchemaDefinition";

  private KeyFileSerializedConfigElementBeanInfo keyFileSerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    keyFileSerializedConfigElementBeanInfo = new KeyFileSerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    PropertyDescriptor[] propertyDescriptors = keyFileSerializedConfigElementBeanInfo.getPropertyDescriptors();
    assertThat(propertyDescriptors).hasSize(4);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(KEY_NAME);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(KEY_SCHEMA_DEFINITION);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(KEY_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[3].getName()).isEqualTo(KEY_SCHEMA_TYPE);

  }
}