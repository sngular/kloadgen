/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.keyserialized;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.sngular.kloadgen.serializer.AvroSerializer;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@WireMockTest
class KeySerializedConfigElementTest {

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
  @DisplayName("Should configure Key Serialized Properties")
  void iterationStart(final WireMockRuntimeInfo wmRuntimeInfo) {

    JMeterContextService.getContext().getProperties().put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    JMeterContextService.getContext().getProperties().put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    final var keySerializedConfigElement = new KeySerializedConfigElement("avroSubject", Collections.emptyList(), "AVRO",
                                                                    AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    keySerializedConfigElement.iterationStart(null);
    Assertions.assertThat(JMeterContextService.getContext().getVariables().getObject(PropsKeysHelper.KEY_SUBJECT_NAME)).isNotNull();
    Assertions.assertThat(JMeterContextService.getContext().getVariables().getObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES)).isNotNull();

  }

}