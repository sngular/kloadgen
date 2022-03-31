/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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

  private static final String VALUE_NAME_STRATEGY = "valueNameStrategy";

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
    assertThat(propertyDescriptors).hasSize(6);
    assertThat(propertyDescriptors[0].getName()).isEqualTo(VALUE_NAME_STRATEGY);
    assertThat(propertyDescriptors[1].getName()).isEqualTo(VALUE_SCHEMA_DEFINITION);
    assertThat(propertyDescriptors[2].getName()).isEqualTo(VALUE_SCHEMA_PROPERTIES);
    assertThat(propertyDescriptors[3].getName()).isEqualTo(VALUE_SCHEMA_TYPE);
    assertThat(propertyDescriptors[4].getName()).isEqualTo(SERIALIZER_PROPERTY);
    assertThat(propertyDescriptors[5].getName()).isEqualTo(VALUE_SUBJECT_NAME);
  }
}