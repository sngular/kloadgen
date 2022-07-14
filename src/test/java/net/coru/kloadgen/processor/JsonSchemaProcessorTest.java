package net.coru.kloadgen.processor;

import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SCHEMA_COMPLEX_COLLECTIONS;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SCHEMA_NESTED_COLLECTIONS;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SCHEMA_NESTED_ITERATION;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SCHEMA_NESTED_ITERATION_EXPECTED;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_EXPECTED;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_MAP;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_REQUIRED;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_REQUIRED_EXPECTED;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class JsonSchemaProcessorTest {

  private static Stream<Object> parametersForTestNullOnOptionalField() {
    return Stream.of(
        Arguments.of(SIMPLE_SCHEMA, SIMPLE_SCHEMA_EXPECTED)
    );
  }

  private static Stream<Object> parametersForTestBasicStructure() {
    return Stream.of(
        Arguments.of(SIMPLE_SCHEMA_REQUIRED, SIMPLE_SCHEMA_REQUIRED_EXPECTED)
    );
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
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, SIMPLE_SCHEMA_MAP);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    assertThat(message.toString()).contains("lastName\":\"García")
                                  .contains("itemTipo\":{").contains("itemType\":{");
  }

  @Test
  void testNullOnNestedCollections() {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, SCHEMA_NESTED_COLLECTIONS);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    assertThat(message.toString()).contains("fruits\":[").contains("vegetables\":{")
                                  .contains("\"birds\":[").contains("\"animals\":{");

  }

  @Test
  void testNullOnComplexCollections() {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, SCHEMA_COMPLEX_COLLECTIONS);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();

    assertThat(message.toString()).contains("{\"fruits\":{\"tropical\":[]},\"vegetables\":{\"trees\":{}}")
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

  private static Stream<Object> parametersForTestNestedLevels() {
    return Stream.of(
        Arguments.of(SCHEMA_NESTED_ITERATION , SCHEMA_NESTED_ITERATION_EXPECTED)
    );
  }
  @ParameterizedTest
  @MethodSource("parametersForTestNestedLevels")
  void testNestedComplexLevels(List<FieldValueMapping> schemaAsJson, String expected) {

    SchemaProcessor jsonSchemaProcessor = new SchemaProcessor();
    jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null,null, schemaAsJson);

    ObjectNode message = (ObjectNode) jsonSchemaProcessor.next();
    System.out.println(message);
    /*assertThat(message.toString()).contains("{\"fruits\":{\"tropical\":[]},\"vegetables\":{\"trees\":{}}")
                                  .contains("\"birds\":[[{\"nameBird\":")
                                  .contains("\"animals\":{").contains("nameAnimal\":");*/
    JSONAssert.assertEquals(message.toString(),expected, JSONCompareMode.LENIENT);
  }

}
