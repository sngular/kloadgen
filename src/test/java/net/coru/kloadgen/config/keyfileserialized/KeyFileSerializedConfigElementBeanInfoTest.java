/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keyfileserialized;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeyFileSerializedConfigElementBeanInfoTest {

  private static final String KEY_SUBJECT_NAME = "keySubjectName";

  private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

  private static final String KEY_SCHEMA_TYPE = "keySchemaType";

  private static final String KEY_SCHEMA_DEFINITION = "keySchemaDefinition";

  private static final String KEY_NAME_STRATEGY = "keyNameStrategy";

  private static final String SERIALIZER_PROPERTY = "keySerializerConfiguration";

  private KeyFileSerializedConfigElementBeanInfo keyFileSerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    keyFileSerializedConfigElementBeanInfo = new KeyFileSerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    PropertyDescriptor[] propertyDescriptors = keyFileSerializedConfigElementBeanInfo.getPropertyDescriptors();
    assertThat(propertyDescriptors).hasSize(6);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(KEY_NAME_STRATEGY);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(KEY_SCHEMA_DEFINITION);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(KEY_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[3].getName()).isEqualTo(KEY_SCHEMA_TYPE);
    assertThat(propertyDescriptors[4].getName()).isEqualTo(SERIALIZER_PROPERTY);
    assertThat(propertyDescriptors[5].getName()).isEqualTo(KEY_SUBJECT_NAME);
  }
}