/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.valuefileserialized;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

import com.sngular.kloadgen.serializer.AvroSerializer;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import com.sngular.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueFileSerializedConfigElementTest {

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
  void iterationStart() {

    final String definitionSchema
        = "{\"type\": \"record\", \"name\": \"value\", \"namespace\": \"my.topic\", \"fields\": [  {   \"name\": \"myValue\",   \"type\": \"long\"  } ], \"connect.name\": \"my"
          + ".topic.value\"}";

    final var valueFileSerializedConfigElement = new ValueFileSerializedConfigElement("avroSubject", Collections.emptyList(), definitionSchema, "AVRO",
                                             AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    valueFileSerializedConfigElement.iterationStart(null);
    Assertions.assertThat(JMeterContextService.getContext().getVariables().getObject(PropsKeysHelper.VALUE_SUBJECT_NAME)).isNotNull();
    Assertions.assertThat(JMeterContextService.getContext().getVariables().getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES)).isNotNull();

  }

}