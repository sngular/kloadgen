/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.loadgen.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.testutil.FileHelper;
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

  private final FileHelper fileHelper = new FileHelper();

  @BeforeEach
  public void setUp(final WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  void testAvroLoadGeneratorApicurio(final WireMockRuntimeInfo wmRuntimeInfo) throws KLoadGenException, IOException {
    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("Name").fieldType("string").valueLength(0).fieldValueList("Jose").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("Age").fieldType("int").valueLength(0).fieldValueList("43").required(true).isAncestorRequired(true).build());
    final Map<String, String> originals = new HashMap<>();
    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry("apicurio");

    originals.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "apicurio");
    originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), wmRuntimeInfo.getHttpBaseUrl());

    final AvroSRLoadGenerator avroLoadGenerator = new AvroSRLoadGenerator();
    avroLoadGenerator.setUpGenerator(originals, "dad37185-782b-4bed-9cf6-678d1d4587d9", fieldValueMappingList);
    final Object message = avroLoadGenerator.nextMessage();
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);

    final EnrichedRecord enrichedRecord = (EnrichedRecord) message;
    Assertions.assertThat(enrichedRecord.getGenericRecord()).isNotNull().hasFieldOrPropertyWithValue("values", Arrays.asList("Jose", 43).toArray());
  }

  @Test
  void testAvroLoadGeneratorConfluent(final WireMockRuntimeInfo wmRuntimeInfo) throws KLoadGenException, IOException {

    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
            FieldValueMapping.builder().fieldName("Name").fieldType("string").valueLength(0).fieldValueList("Jose").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("Age").fieldType("int").valueLength(0).fieldValueList("43").required(true).isAncestorRequired(true).build());

    final Map<String, String> originals = new HashMap<>();
    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry("confluent");

    originals.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "confluent");
    originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), wmRuntimeInfo.getHttpBaseUrl());

    final AvroSRLoadGenerator avroLoadGenerator = new AvroSRLoadGenerator();
    avroLoadGenerator.setUpGenerator(originals, "avroSubject", fieldValueMappingList);
    final Object message = avroLoadGenerator.nextMessage();
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);

    final EnrichedRecord enrichedRecord = (EnrichedRecord) message;
    Assertions.assertThat(enrichedRecord.getGenericRecord()).isNotNull().hasFieldOrPropertyWithValue("values", Arrays.asList("Jose", 43).toArray());
  }
}
