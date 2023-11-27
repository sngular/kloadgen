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
import com.sngular.kloadgen.parsedschema.AvroParsedSchema;
import com.sngular.kloadgen.parsedschema.JsonParsedSchema;
import com.sngular.kloadgen.parsedschema.ProtobufParsedSchema;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import com.squareup.wire.schema.Location;
import com.squareup.wire.schema.internal.parser.ProtoParser;
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

    final var parsedSchema = new AvroParsedSchema("test", new Schema.Parser().parse(testFile));
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(parsedSchema);
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    final var result = SchemaExtractor.flatPropertiesList("avroSubject");

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Test flatPropertiesList with Json")
  void testFlatPropertiesListWithJson() throws IOException {

    final var testFile = fileHelper.getContent("/jsonschema/basic.jcs");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final var parsedSchema = new JsonParsedSchema("JSON", testFile);
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(parsedSchema);
    final var result = SchemaExtractor.flatPropertiesList("jsonSubject");

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Test flatPropertiesList with Protobuf")
  void testFlatPropertiesListWithProtobuf() throws IOException {

    final var testFile = fileHelper.getContent("/proto-files/easyTest.proto");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final var parsedSchema = new ProtobufParsedSchema("test", new ProtoParser(Location.get("/"), testFile.toCharArray()).readProtoFile());
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(parsedSchema);
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

    final var parsedSchema = new FakeParsedSchema("Test", "TestType", null);
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(parsedSchema);
    Assertions.assertThatExceptionOfType(KLoadGenException.class)
              .isThrownBy(() -> SchemaExtractor.flatPropertiesList("exceptionSubject")
              ).withMessage(String.format("Schema type not supported %s", parsedSchema.schemaType()));

  }

  @Test
  @DisplayName("Test flatPropertiesList with AVRO and CONFLUENT")
  void testSchemaTypeIsCorrectWhenAvroConfluent() throws IOException {
    final var testFile = fileHelper.getContent("/avro-files/embedded-avros-example-test.avsc");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final var parsedSchema = new AvroParsedSchema("test", new Schema.Parser().parse(testFile));
    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(parsedSchema);
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

    final var result = SchemaExtractor.flatPropertiesList("avroSubject");

    Assertions.assertThat(result).isNotNull();
  }

}
