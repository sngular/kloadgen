package com.sngular.kloadgen.processor;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.fixture.JsonSchemaFixturesConstants;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class JsonSchemaProcessorTest {

  private static Stream<Object> parametersForTestNullOnOptionalField() {
    return Stream.of(Arguments.of(JsonSchemaFixturesConstants.SIMPLE_SCHEMA, JsonSchemaFixturesConstants.SIMPLE_SCHEMA_EXPECTED));
  }

  private static Stream<Object> parametersForTestBasicStructure() {
    return Stream.of(Arguments.of(JsonSchemaFixturesConstants.SIMPLE_SCHEMA_REQUIRED, JsonSchemaFixturesConstants.SIMPLE_SCHEMA_REQUIRED_EXPECTED));
  }

  @BeforeEach
  public final void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @ParameterizedTest
  @MethodSource("parametersForTestNullOnOptionalField")
  final void testNullOnOptionalField(final List<FieldValueMapping> schemaAsJson, final String expected) {

    final SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();

    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, schemaAsJson);
    final ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    JSONAssert.assertEquals(message.toString(), expected, JSONCompareMode.STRICT);
  }

  @Test
  final void testNullOnMapWithChildren() {

    final SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SIMPLE_SCHEMA_MAP);

    final ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).contains("lastName\":\"García")
                                  .contains("itemTipo\":{").contains("itemType\":{");
  }

  @Test
  final void testNullOnNestedCollections() {

    final SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SCHEMA_NESTED_COLLECTIONS);

    final ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).contains("fruits\":[").contains("vegetables\":{").contains("\"birds\":[").contains("\"animals\":{");

  }

  @Test
  final void testNullOnComplexCollections() {

    final SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SCHEMA_COMPLEX_COLLECTIONS);

    final ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).contains("{\"fruits\":{\"tropical\":[]},\"vegetables\":{\"trees\":{}}")
                                  .contains("\"birds\":[[{\"nameBird\":")
                                  .contains("\"animals\":{").contains("nameAnimal\":");

  }
  
  @ParameterizedTest
  @MethodSource("parametersForTestBasicStructure")
  final void testBasicStructure(final List<FieldValueMapping> schemaAsJson, final String expected) {

    final SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();

    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, schemaAsJson);
    final ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    JSONAssert.assertEquals(message.toString(), expected, JSONCompareMode.STRICT);
  }

  @Test
  final void testNestedComplexLevels() {

    final SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SCHEMA_NESTED_ITERATION);

    final ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).containsPattern("^\\{\"flowers\":\\{(\"\\w+\":\\{\"name\":\\[(\"Edelweiss\",?)+],?)+.*");
    Assertions.assertThat(message.toString()).containsPattern(
        ".*\\\"bush\":\\{\"\\w+\":\\{\"maxHeight\":(\\[(\\{(\"\\w+\":\\d+,?)+},?)+],?)*\"leafs\":(\\[\\{(\"\\w+\":\\[(\"oval\",?)+],?)+}+],+)*.*$");
  }

}
