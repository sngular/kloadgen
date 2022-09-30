/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keysimple;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Locale;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KeySimpleConfigElementTest {

  private JMeterContext jmcx;

  @BeforeEach
  public void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should configure Key Simple Properties")
  void iterationStart() {

    final var keySimpleConfigElement = new KeySimpleConfigElement();
    keySimpleConfigElement.setKeyType("string");
    keySimpleConfigElement.setKeyValue("");
    keySimpleConfigElement.setKeySerializerConfiguration("org.apache.kafka.common.serialization.StringSerializer");

    keySimpleConfigElement.iterationStart(null);

    final var variables = jmcx.getVariables();

    Assertions.assertThat(variables).isNotNull();
    Assertions.assertThat(variables.entrySet())
              .hasSize(5)
              .containsExactlyInAnyOrder(new SimpleEntry<>("key.type", "string"),
                                   new SimpleEntry<>("key.value", ""),
                                   new SimpleEntry<>("key.serializer.class.property", "org.apache.kafka.common.serialization.StringSerializer"),
                                   new SimpleEntry<>("simple.keyed.message", true),
                                   new SimpleEntry<>("key.schema.type","NoSchema"));

  }
}