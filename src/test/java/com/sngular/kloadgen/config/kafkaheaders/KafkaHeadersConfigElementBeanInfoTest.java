/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.kafkaheaders;

import java.beans.PropertyDescriptor;
import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KafkaHeadersConfigElementBeanInfoTest {

  private static final String KAFKA_HEADERS = "kafkaHeaders";
  private static final String PROPERTIES = "props";
  private static final String SCHEMA = "schema";
  private KafkaHeadersConfigElementBeanInfo kafkaHeadersConfigElementBeanInfo;

  @BeforeEach
  public void setUp() {
    JMeterUtils.setLocale(Locale.ENGLISH);
    kafkaHeadersConfigElementBeanInfo = new KafkaHeadersConfigElementBeanInfo();
  }

  @Test
  final void shouldGenerateElements() {
    final PropertyDescriptor[] propertyDescriptors = kafkaHeadersConfigElementBeanInfo.getPropertyDescriptors();
    Assertions.assertThat(propertyDescriptors).hasSize(3);
    Assertions.assertThat(propertyDescriptors[0].getName()).isEqualTo(KAFKA_HEADERS);
    Assertions.assertThat(propertyDescriptors[1].getName()).isEqualTo(PROPERTIES);
    Assertions.assertThat(propertyDescriptors[2].getName()).isEqualTo(SCHEMA);
  }
}