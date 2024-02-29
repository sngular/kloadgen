/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.keyserialized;

import java.beans.PropertyDescriptor;
import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeySerializedConfigElementBeanInfoTest {

  private static final String KEY_SUBJECT_NAME = "keySubjectName";

  private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

  private static final String KEY_SCHEMA_TYPE = "keySchemaType";

  private static final String KEY_NAME_STRATEGY = "keyNameStrategy";

  private static final String KEY_SERIALIZER_CONFIGURATION = "keySerializerConfiguration";

  private static final String PROPERTIES = "props";

  private static final String SCHEMA = "schema";

  private KeySerializedConfigElementBeanInfo keySerializedConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    keySerializedConfigElementBeanInfo = new KeySerializedConfigElementBeanInfo();
  }

  @Test
  void shouldGenerateElements() {
    final PropertyDescriptor[] propertyDescriptors = keySerializedConfigElementBeanInfo.getPropertyDescriptors();
    Assertions.assertThat(propertyDescriptors).hasSize(7);
    Assertions.assertThat(propertyDescriptors[0].getName()).isEqualTo(KEY_NAME_STRATEGY);
    Assertions.assertThat(propertyDescriptors[1].getName()).isEqualTo(KEY_SCHEMA_PROPERTIES);
    Assertions.assertThat(propertyDescriptors[2].getName()).isEqualTo(KEY_SCHEMA_TYPE);
    Assertions.assertThat(propertyDescriptors[3].getName()).isEqualTo(KEY_SERIALIZER_CONFIGURATION);
    Assertions.assertThat(propertyDescriptors[4].getName()).isEqualTo(KEY_SUBJECT_NAME);
    Assertions.assertThat(propertyDescriptors[5].getName()).isEqualTo(PROPERTIES);
    Assertions.assertThat(propertyDescriptors[6].getName()).isEqualTo(SCHEMA);
  }
}