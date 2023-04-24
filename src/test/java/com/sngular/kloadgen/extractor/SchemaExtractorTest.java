/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.ProtoBufExtractor;
import com.sngular.kloadgen.extractor.impl.SchemaExtractorImpl;
import com.sngular.kloadgen.sampler.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.sampler.schemaregistry.adapter.impl.ConfluentParsedSchemaMetadata;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.testutil.ParsedSchemaUtil;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import org.apache.avro.Schema;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private Properties properties = new Properties();

  private SchemaExtractorImpl schemaExtractor;

  private MockedStatic<JMeterHelper> jMeterHelperMockedStatic;

  private MockedStatic<JMeterContextService> jMeterContextServiceMockedStatic;

  @Mock
  private AvroExtractor avroExtractor;

  @Mock
  private JsonExtractor jsonExtractor;

  @Mock
  private ProtoBufExtractor protoBufExtractor;

  @BeforeEach
  void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
    schemaExtractor = new SchemaExtractorImpl(avroExtractor, jsonExtractor, protoBufExtractor);
    jMeterHelperMockedStatic = Mockito.mockStatic(JMeterHelper.class);
    jMeterContextServiceMockedStatic = Mockito.mockStatic(JMeterContextService.class, RETURNS_DEEP_STUBS);
  }

  @AfterEach
  void tearDown() {
    jMeterHelperMockedStatic.close();
    jMeterContextServiceMockedStatic.close();
    properties.clear();
  }

  @Test
  @DisplayName("Test flatPropertiesList with AVRO")
  void testFlatPropertiesListWithAVRO() throws IOException, RestClientException {

    final File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    Mockito.when(avroExtractor.getParsedSchema(Mockito.anyString())).thenCallRealMethod();
    final ParsedSchema parsedSchema = schemaExtractor.schemaTypesList(testFile, "AVRO");
    BaseParsedSchema baseParsedSchema = new BaseParsedSchema(ConfluentParsedSchemaMetadata.parse(parsedSchema));
    jMeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    jMeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    Mockito.when(avroExtractor.processConfluentParsedSchema(Mockito.any(Schema.class))).thenReturn(new ArrayList<>());
    schemaExtractor.flatPropertiesList("avroSubject");

    Mockito.verify(avroExtractor, Mockito.times(1)).processConfluentParsedSchema(Mockito.any(Schema.class));

  }

  @Test
  @DisplayName("Test flatPropertiesList with Json")
  void testFlatPropertiesListWithJson() throws IOException, RestClientException {

    final File testFile = fileHelper.getFile("/jsonschema/basic.jcs");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    Mockito.when(jsonExtractor.getParsedSchema(Mockito.anyString())).thenCallRealMethod();
    final ParsedSchema parsedSchema = schemaExtractor.schemaTypesList(testFile, "JSON");
    BaseParsedSchema baseParsedSchema = new BaseParsedSchema(ConfluentParsedSchemaMetadata.parse(parsedSchema));
    jMeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    jMeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    Mockito.when(jsonExtractor.processConfluentParsedSchema(Mockito.any(Object.class))).thenReturn(new ArrayList<>());

    schemaExtractor.flatPropertiesList("jsonSubject");

    Mockito.verify(jsonExtractor, Mockito.times(1)).processConfluentParsedSchema(Mockito.any(Object.class));

  }

  @Test
  @DisplayName("Test flatPropertiesList with Protobuf")
  void testFlatPropertiesListWithProtobuf() throws RestClientException, IOException {

    final File testFile = fileHelper.getFile("/proto-files/easyTest.proto");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());
    Mockito.when(protoBufExtractor.getParsedSchema(Mockito.anyString())).thenCallRealMethod();

    final ParsedSchema parsedSchema = schemaExtractor.schemaTypesList(testFile, "PROTOBUF");
    BaseParsedSchema baseParsedSchema = new BaseParsedSchema(ConfluentParsedSchemaMetadata.parse(parsedSchema));
    jMeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    jMeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    Mockito.when(protoBufExtractor.processConfluentParsedSchema(Mockito.any(ProtoFileElement.class))).thenReturn(new ArrayList<>());

    schemaExtractor.flatPropertiesList("protobufSubject");
    Mockito.verify(protoBufExtractor, Mockito.times(1)).processConfluentParsedSchema(Mockito.any(ProtoFileElement.class));
  }

  @Test
  @DisplayName("Test flatPropertiesList throws exception schema type not supported")
  void testFlatPropertiesListWithException() throws IOException, RestClientException {

    final ParsedSchema parsedSchema = new ParsedSchemaUtil();
    BaseParsedSchema baseParsedSchema = new BaseParsedSchema(ConfluentParsedSchemaMetadata.parse(parsedSchema));
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    jMeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

    jMeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    Assertions.assertThatExceptionOfType(KLoadGenException.class)
              .isThrownBy(() -> schemaExtractor.flatPropertiesList("exceptionSubject")
              ).withMessage(String.format("Schema type not supported %s", parsedSchema.schemaType()));

  }

}
