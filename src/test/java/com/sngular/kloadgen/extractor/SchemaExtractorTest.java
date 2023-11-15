/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.testutil.ParsedSchemaUtil;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
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
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final Properties properties = new Properties();

  private MockedStatic<JMeterHelper> jmeterHelperMockedStatic;

  private MockedStatic<JMeterContextService> jmeterContextServiceMockedStatic;

  @BeforeEach
  void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
    jmeterHelperMockedStatic = Mockito.mockStatic(JMeterHelper.class);
    jmeterContextServiceMockedStatic = Mockito.mockStatic(JMeterContextService.class, Answers.RETURNS_DEEP_STUBS);
  }

  @AfterEach
  void tearDown() {
    jmeterHelperMockedStatic.close();
    jmeterContextServiceMockedStatic.close();
    properties.clear();
  }

  @Test
  @DisplayName("Test flatPropertiesList with AVRO")
  void testFlatPropertiesListWithAVRO() throws IOException {

    final File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.APICURIO.toString());

    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "AVRO");
    final var baseParsedSchema = new BaseParsedSchema<>(ApicurioAbstractParsedSchemaMetadata.parse(parsedSchema));
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    final var result = SchemaExtractor.flatPropertiesList("avroSubject");

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Test flatPropertiesList with Json")
  void testFlatPropertiesListWithJson() throws IOException {

    final File testFile = fileHelper.getFile("/jsonschema/basic.jcs");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final var parsedSchema = new ParsedSchema(testFile, "JSON");
    final var baseParsedSchema = new BaseParsedSchema<>(ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema));
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    final var result = SchemaExtractor.flatPropertiesList("jsonSubject");

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Test flatPropertiesList with Protobuf")
  void testFlatPropertiesListWithProtobuf() throws IOException {

    final File testFile = fileHelper.getFile("/proto-files/easyTest.proto");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "PROTOBUF");
   final BaseParsedSchema baseParsedSchema = new BaseParsedSchema<>(ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema));
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    final var result = SchemaExtractor.flatPropertiesList("protobufSubject");
    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Test readSchemaFile with AVRO")
  void testReadSchemaFile() throws IOException {
    final String testFilePath = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc").toPath().toString();

    final var result = SchemaExtractor.readSchemaFile(testFilePath);
    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Test flatPropertiesList throws exception schema type not supported")
  void testFlatPropertiesListWithException() {

    final var parsedSchema = new ParsedSchemaUtil();
    final var baseParsedSchema = new BaseParsedSchema<>(ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema));
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    Assertions.assertThatExceptionOfType(KLoadGenException.class)
              .isThrownBy(() -> SchemaExtractor.flatPropertiesList("exceptionSubject")
              ).withMessage(String.format("Schema type not supported %s", parsedSchema.schemaType()));

  }

  @Test
  @DisplayName("Test flatPropertiesList with AVRO and CONFLUENT")
  void testSchemaTypeIsCorrectWhenAvroConfluent() throws IOException {
    final File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "AVRO");
    final var baseParsedSchema = new BaseParsedSchema<>(ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema));
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

    final var result = SchemaExtractor.flatPropertiesList("avroSubject");

    Assertions.assertThat(result).isNotNull();
  }

}
