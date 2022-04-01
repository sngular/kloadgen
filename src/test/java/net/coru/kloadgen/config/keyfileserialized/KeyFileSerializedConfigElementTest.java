/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keyfileserialized;

import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SUBJECT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import net.coru.kloadgen.serializer.AvroSerializer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeyFileSerializedConfigElementTest {

  @BeforeEach
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  public void iterationStart() {

    String schemaDefinition
        = "{\"type\": \"record\", \"name\": \"Key\", \"namespace\": \"my.topic\", \"fields\": [  {   \"name\": \"myKey\",   \"type\": \"long\"  } ], \"connect.name\": \"my.topic" +
          ".Key\"}";

    KeyFileSerializedConfigElement
        keyFileSerializedConfigElement =
        new KeyFileSerializedConfigElement("avroSubject", Collections.emptyList(), schemaDefinition, "AVRO",
                                           AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    keyFileSerializedConfigElement.iterationStart(null);
    assertThat(JMeterContextService.getContext().getVariables().getObject(KEY_SUBJECT_NAME)).isNotNull();
    assertThat(JMeterContextService.getContext().getVariables().getObject(KEY_SCHEMA_PROPERTIES)).isNotNull();

  }

}