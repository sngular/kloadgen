/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.valueserialized;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import net.coru.kloadgen.serializer.AvroSerializer;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValueSerializedConfigElementTest {

  @BeforeEach
  public void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should configure Value Serialized Properties")
  void iterationStart() {

    final var valueSerializedConfigElement = new ValueSerializedConfigElement("avroSubject", Collections.emptyList(), "AVRO",
                                                                        AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    valueSerializedConfigElement.iterationStart(null);
    Assertions.assertThat(JMeterContextService.getContext().getVariables().getObject(PropsKeysHelper.VALUE_SUBJECT_NAME)).isNotNull();
    Assertions.assertThat(JMeterContextService.getContext().getVariables().getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES)).isNotNull();

  }

}