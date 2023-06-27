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
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class ProtobufSRLoadGeneratorTest {

  private FileHelper fileHelper = new FileHelper();

  @BeforeEach
  public void setUp(final WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    final String kloadPath = absolutePath + "/kloadgen.properties";
    final Map<String, String> properties = new HashMap<>();
    properties.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    properties.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "confluent");
    properties.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE);
    properties.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG, ProducerKeysHelper.FLAG_YES);
    properties.put(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "foo");
    properties.put(SchemaRegistryClientConfig.USER_INFO_CONFIG, "foo");
    JMeterUtils.loadJMeterProperties(kloadPath);
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.getProperties().putAll(properties);
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);

  }

  @Test
  void testProtobufLoadGeneratorConfluent(final WireMockRuntimeInfo wmRuntimeInfo) throws KLoadGenException {

    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("propertyTest1.importedProperty.nestedProperty").fieldType("string").valueLength(0).fieldValueList("").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("propertyTest1.entityNumberTwo").fieldType("string").valueLength(0).fieldValueList("").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("propertyTest2.propertyNumberOne").fieldType("int").valueLength(0).fieldValueList("").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("propertyTest2.propertyNumberTwo").fieldType("string").valueLength(0).fieldValueList("").required(true)
                         .isAncestorRequired(true).build()
    );

    final var schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry("confluent");
    final Map<String, String> originals = new HashMap<>();

    originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), wmRuntimeInfo.getHttpBaseUrl());
    originals.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "confluent");
    originals.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    originals.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    final var protobufLoadGenerator = new ProtobufLoadGenerator();
    protobufLoadGenerator.setUpGenerator(originals, "protobufSubjectWithImport", fieldValueMappingList);
    final var message = protobufLoadGenerator.nextMessage();
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);

    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(message.getGenericRecord().toString()).contains("propertyTest1");
    Assertions.assertThat(message.getGenericRecord().toString()).contains("entityNumberTwo");
    Assertions.assertThat(message.getGenericRecord().toString()).contains("propertyNumberOne");
    Assertions.assertThat(message.getGenericRecord().toString()).contains("propertyNumberTwo");
  }

}