/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keyserialized;

import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SUBJECT_NAME;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import net.coru.kloadgen.serializer.AvroSerializer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@WireMockTest
class KeySerializedConfigElementTest {

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
  @DisplayName("Should configure Key Serialized Properties")
  void iterationStart(WireMockRuntimeInfo wmRuntimeInfo) {

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    KeySerializedConfigElement
        keySerializedConfigElement = new KeySerializedConfigElement("avroSubject", Collections.emptyList(), "AVRO",
           AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    keySerializedConfigElement.iterationStart(null);
    assertThat(JMeterContextService.getContext().getVariables().getObject(KEY_SUBJECT_NAME)).isNotNull();
    assertThat(JMeterContextService.getContext().getVariables().getObject(KEY_SCHEMA_PROPERTIES)).isNotNull();

  }

}