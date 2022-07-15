package net.coru.kloadgen.processor;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants;
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
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @ParameterizedTest
  @MethodSource("parametersForTestNullOnOptionalField")
  void testNullOnOptionalField(List<FieldValueMapping> schemaAsJson, String expected) {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();

    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, schemaAsJson);
    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    JSONAssert.assertEquals(message.toString(), expected, JSONCompareMode.STRICT);
  }

  @Test
  void testNullOnMapWithChildren() {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SIMPLE_SCHEMA_MAP);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).contains("lastName\":\"García")
                                  .contains("itemTipo\":{").contains("itemType\":{");
  }

  @Test
  void testNullOnNestedCollections() {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SCHEMA_NESTED_COLLECTIONS);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).contains("fruits\":[").contains("vegetables\":{").contains("\"birds\":[").contains("\"animals\":{");

  }

  @Test
  void testNullOnComplexCollections() {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SCHEMA_COMPLEX_COLLECTIONS);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).contains("{\"fruits\":{\"tropical\":[]},\"vegetables\":{\"trees\":{}}")
                                  .contains("\"birds\":[[{\"nameBird\":")
                                  .contains("\"animals\":{").contains("nameAnimal\":");

  }
  
  @ParameterizedTest
  @MethodSource("parametersForTestBasicStructure")
  void testBasicStructure(List<FieldValueMapping> schemaAsJson, String expected) {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();

    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, schemaAsJson);
    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    JSONAssert.assertEquals(message.toString(), expected, JSONCompareMode.STRICT);
  }

  @Test
  void testNestedComplexLevels() {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, JsonSchemaFixturesConstants.SCHEMA_NESTED_ITERATION);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    Assertions.assertThat(message.toString()).containsPattern("^\\{\"flowers\":\\{(\"\\w+\":\\{\"name\":\\[(\"Edelweiss\",?)+],?)+.*");
    Assertions.assertThat(message.toString()).containsPattern(
        ".*\\\"bush\":\\{\"\\w+\":\\{\"maxHeight\":(\\[(\\{(\"\\w+\":\\d+,?)+},?)+],?)*\"leafs\":(\\[\\{(\"\\w+\":\\[(\"oval\",?)+],?)+}+],+)*.*$");
  }

}
