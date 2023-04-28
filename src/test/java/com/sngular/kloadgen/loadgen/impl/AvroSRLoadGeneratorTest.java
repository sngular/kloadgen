/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.loadgen.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.sampler.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class AvroSRLoadGeneratorTest {

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
  void testAvroLoadGenerator(final WireMockRuntimeInfo wmRuntimeInfo) throws KLoadGenException {

    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("Name").fieldType("string").valueLength(0).fieldValueList("Jose").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("Age").fieldType("int").valueLength(0).fieldValueList("43").required(true).isAncestorRequired(true).build());

    final Map<String, String> originals = new HashMap<>();
    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry("apicurio");

    originals.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "apicurio");
    originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), wmRuntimeInfo.getHttpBaseUrl());

    final AvroSRLoadGenerator avroLoadGenerator = new AvroSRLoadGenerator();
    avroLoadGenerator.setUpGenerator(originals, "147fc92d-d8e6-44a9-89a4-bf9bb8246735", fieldValueMappingList);
    final Object message = avroLoadGenerator.nextMessage();
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);

    final EnrichedRecord enrichedRecord = (EnrichedRecord) message;
    Assertions.assertThat(enrichedRecord.getGenericRecord()).isNotNull().hasFieldOrPropertyWithValue("values", Arrays.asList("Jose", 43).toArray());
  }
}
