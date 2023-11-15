/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.sampler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.fixture.JsonSchemaFixturesConstants;
import com.sngular.kloadgen.serializer.SerializerTestFixture;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SamplerUtilTest {

  private final FileHelper fileHelper = new FileHelper();

  private JMeterContext jmcx;

  private static Stream<Object> parametersForConfigureValueGeneratorTest() {
    return Stream.of("localhost:8081", "");
  }

  private static Stream<Object> parametersForConfigureKeyGeneratorTest() {
    return Stream.of("avro", "json", "protobuf");
  }

  @BeforeEach
  public final void setUp() throws IOException {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @ParameterizedTest
  @MethodSource("parametersForConfigureValueGeneratorTest")
  void configureValueGeneratorTest(final String jmeterProps) throws IOException {
    final Properties props = new Properties();
    props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, jmeterProps);
    jmcx.setVariables(getVariablesAvro());
    final var generator = SamplerUtil.configureValueGenerator(props);
    Assertions.assertThat(generator.nextMessage()).isNotNull();
  }

  public JMeterVariables getVariablesAvro() throws IOException {
    final File testFile = fileHelper.getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "AVRO");
    final var variables = new JMeterVariables();
    variables.put(PropsKeysHelper.KEY_SCHEMA_TYPE, "avro");
    variables.put(PropsKeysHelper.VALUE_SUBJECT_NAME, "test");
    variables.put(PropsKeysHelper.KEY_SUBJECT_NAME, "test");
    variables.put(PropsKeysHelper.VALUE_SCHEMA, String.valueOf(parsedSchema.rawSchema()));
    variables.put(PropsKeysHelper.VALUE_SCHEMA_TYPE, "avro");
    variables.put(PropsKeysHelper.KEY_SCHEMA, String.valueOf(parsedSchema.rawSchema()));
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, Arrays.asList(
        FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityIntArray[2]").fieldType("int-array").valueLength(0).fieldValueList("[1]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("topLevelIntArray[3]").fieldType("int-array").valueLength(0).fieldValueList("[2]").required(true).isAncestorRequired(true).build())
    );
    return variables;
  }

  @ParameterizedTest
  @MethodSource("parametersForConfigureKeyGeneratorTest")
  void configureKeyGeneratorTest(final String jmeterProps) throws IOException {
    final Properties props = new Properties();
    props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, "localhost:8081");
    props.put(PropsKeysHelper.KEY_SCHEMA_TYPE, jmeterProps);

    if ("json".equalsIgnoreCase(jmeterProps)) {
      jmcx.setVariables(getVariablesJsonSchema());
    }
    if ("avro".equalsIgnoreCase(jmeterProps)) {
      jmcx.setVariables(getVariablesAvro());
    }
    if ("protobuf".equalsIgnoreCase(jmeterProps)) {
      jmcx.setVariables(getVariablesProtobuf());
    }

    final var generator = SamplerUtil.configureKeyGenerator(props);
    Assertions.assertThat(generator.nextMessage()).isNotNull();

  }

  public JMeterVariables getVariablesJsonSchema() throws IOException {

    final File testFile = fileHelper.getFile("/jsonschema/basic.jcs");
    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "JSON");

    final var variables = new JMeterVariables();
    variables.put(PropsKeysHelper.KEY_SCHEMA_TYPE, "JSON");
    variables.put(PropsKeysHelper.VALUE_SUBJECT_NAME, "jsonSubject");
    variables.put(PropsKeysHelper.KEY_SUBJECT_NAME, "jsonSubject");
    variables.put(PropsKeysHelper.VALUE_SCHEMA, String.valueOf(parsedSchema.rawSchema()));
    variables.put(PropsKeysHelper.KEY_SCHEMA, String.valueOf(parsedSchema.rawSchema()));
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, JsonSchemaFixturesConstants.SIMPLE_SCHEMA);
    return variables;
  }

  public JMeterVariables getVariablesProtobuf() throws IOException {

    final File testFile = fileHelper.getFile("/proto-files/easyTest.proto");
    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "PROTOBUF");
    final var variables = new JMeterVariables();
    variables.put(PropsKeysHelper.KEY_SCHEMA_TYPE, "protobuf");
    variables.put(PropsKeysHelper.VALUE_SUBJECT_NAME, "protobufSubject");
    variables.put(PropsKeysHelper.VALUE_SCHEMA, String.valueOf(parsedSchema.schema()));
    variables.put(PropsKeysHelper.VALUE_SCHEMA_TYPE, "protobuf");
    variables.put(PropsKeysHelper.KEY_SCHEMA, String.valueOf(parsedSchema.schema()));
    variables.put(PropsKeysHelper.KEY_SUBJECT_NAME, "protobufSubject");
    variables.putObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES, Arrays.asList(
        SerializerTestFixture.createFieldValueMapping("street", "string"),
        SerializerTestFixture.createFieldValueMapping("number[]", "int-array"),
        SerializerTestFixture.createFieldValueMapping("zipcode", "long")));
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, Arrays.asList(
        SerializerTestFixture.createFieldValueMapping("street", "string"),
        SerializerTestFixture.createFieldValueMapping("number[]", "int-array"),
        SerializerTestFixture.createFieldValueMapping("zipcode", "long")));
    return variables;
  }

}
