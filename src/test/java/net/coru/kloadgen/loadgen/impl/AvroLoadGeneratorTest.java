/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import static java.util.Arrays.asList;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class AvroLoadGeneratorTest {

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
  void testAvroLoadGenerator(WireMockRuntimeInfo wmRuntimeInfo) throws KLoadGenException {

    List<FieldValueMapping> fieldValueMappingList = asList(
        FieldValueMapping.builder().fieldName("Name").fieldType("string").valueLength(0).fieldValueList("Jose").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("Age").fieldType("int").valueLength(0).fieldValueList("43").required(true).isAncestorRequired(true).build());

    Map<String, String> originals = new HashMap<>();
    originals.put(SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    originals.put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    originals.put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    AvroLoadGenerator avroLoadGenerator = new AvroLoadGenerator();
    avroLoadGenerator.setUpGenerator(originals, "avroSubject", fieldValueMappingList);
    Object message = avroLoadGenerator.nextMessage();
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);

    EnrichedRecord enrichedRecord = (EnrichedRecord) message;
    assertThat(enrichedRecord.getGenericRecord()).isNotNull().hasFieldOrPropertyWithValue("values", asList("Jose", 43).toArray());
  }
}