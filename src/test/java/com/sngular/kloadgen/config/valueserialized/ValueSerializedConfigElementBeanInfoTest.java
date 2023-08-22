/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.valueserialized;

import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
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
    final var propertyDescriptors = valueSerializedConfigElementBeanInfo.getPropertyDescriptors();
    Assertions.assertThat(propertyDescriptors).hasSize(7);
    Assertions.assertThat(propertyDescriptors[2].getName()).isEqualTo(VALUE_NAME_STRATEGY);
    Assertions.assertThat(propertyDescriptors[3].getName()).isEqualTo(VALUE_SCHEMA_PROPERTIES);
    Assertions.assertThat(propertyDescriptors[4].getName()).isEqualTo(VALUE_SCHEMA_TYPE);
    Assertions.assertThat(propertyDescriptors[5].getName()).isEqualTo(VALUE_SERIALIZER_CONFIGURATION);
    Assertions.assertThat(propertyDescriptors[6].getName()).isEqualTo(VALUE_SUBJECT_NAME);
  }
}