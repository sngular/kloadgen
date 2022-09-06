/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.valuesimple;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Locale;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValueSimpleConfigElementTest {

  private JMeterContext jmcx;

  @BeforeEach
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should configure Value Simple Properties")
  void iterationStart() {

    ValueSimpleConfigElement valueSimpleConfigElement = new ValueSimpleConfigElement();
    valueSimpleConfigElement.setValue("");
    valueSimpleConfigElement.setValueSerializerConfiguration("org.apache.kafka.common.serialization.StringSerializer");

    valueSimpleConfigElement.iterationStart(null);

    var variables = jmcx.getVariables();

    assertThat(variables).isNotNull();
    assertThat(variables.entrySet())
        .hasSize(3)
        .containsExactlyInAnyOrder(new SimpleEntry<>("value", ""),
                                   new SimpleEntry<>("value.serializer.class.property", "org.apache.kafka.common.serialization.StringSerializer"),
                                   new SimpleEntry<>("simple.valued.message", true));
  }
}