/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.keyfileserialized;

import java.beans.PropertyDescriptor;
import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
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
    final PropertyDescriptor[] propertyDescriptors = keyFileSerializedConfigElementBeanInfo.getPropertyDescriptors();
    Assertions.assertThat(propertyDescriptors).hasSize(8);
    Assertions.assertThat(propertyDescriptors[0].getName()).isEqualTo(KEY_NAME_STRATEGY);
    Assertions.assertThat(propertyDescriptors[1].getName()).isEqualTo(KEY_SCHEMA_DEFINITION);
    Assertions.assertThat(propertyDescriptors[2].getName()).isEqualTo(KEY_SCHEMA_PROPERTIES);
    Assertions.assertThat(propertyDescriptors[3].getName()).isEqualTo(KEY_SCHEMA_TYPE);
    Assertions.assertThat(propertyDescriptors[4].getName()).isEqualTo(SERIALIZER_PROPERTY);
    Assertions.assertThat(propertyDescriptors[5].getName()).isEqualTo(KEY_SUBJECT_NAME);
  }
}