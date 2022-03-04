package net.coru.kloadgen.processor;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaProcessorTest {

    @BeforeEach
    public void setUp() {
        File file = new File("src/test/resources");
        String absolutePath = file.getAbsolutePath();
        JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
        JMeterContext jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());
        JMeterUtils.setLocale(Locale.ENGLISH);
    }

    private static Stream<Object> parametersForTestNullOnOptionalField(){
        return Stream.of(
                Arguments.of(SIMPLE_SCHEMA, SIMPLE_SCHEMA_EXPECTED)/*,
                /*Arguments.of(SIMPLE_SCHEMA_COLLECTIONS, SIMPLE_SCHEMA_COLLECTIONS_EXPECTED),
                Arguments.of(COMPLEX_SCHEMA, COMPLEX_SCHEMA_EXPECTED)*/
        );
    }

    @ParameterizedTest
    @MethodSource("parametersForTestNullOnOptionalField")
    void testNullOnOptionalField(List<FieldValueMapping> schemaAsJson, String expected) {

        JsonSchemaProcessor jsonSchemaProcessor = new JsonSchemaProcessor();

        jsonSchemaProcessor.processSchema(schemaAsJson);
        ObjectNode message = jsonSchemaProcessor.next();

        assertThat(message.toString()).isEqualTo(expected);
    }

    @Test
    void testNullOnMapWithChildren(){
        JsonSchemaProcessor jsonSchemaProcessor = new JsonSchemaProcessor();
        jsonSchemaProcessor.processSchema(SIMPLE_SCHEMA_MAP);
        ObjectNode message = jsonSchemaProcessor.next();

        assertThat(message.toString()).contains("lastName\":\"García")
                .contains("itemTipo\":{").contains("itemType\":{");

    }

    @Test
    void testNullOnNestedCollections(){
        JsonSchemaProcessor jsonSchemaProcessor = new JsonSchemaProcessor();
        jsonSchemaProcessor.processSchema(SCHEMA_NESTED_COLLECTIONS);
        ObjectNode message = jsonSchemaProcessor.next();

        assertThat(message.toString()).contains("fruits\":[").contains("vegetables\":{")
                                      .contains("\"birds\":[").contains("\"animals\":{");

    }

    @Test
    void testNullOnComplexCollections(){
        JsonSchemaProcessor jsonSchemaProcessor = new JsonSchemaProcessor();
        jsonSchemaProcessor.processSchema(SCHEMA_COMPLEX_COLLECTIONS);
        ObjectNode message = jsonSchemaProcessor.next();

        System.out.println(SCHEMA_COMPLEX_COLLECTIONS);
        assertThat(message.toString()).contains("{\"fruits\":{\"tropical\":[]},\"vegetables\":{\"trees\":{}}")
                                      .contains("\"birds\":[[{\"nameBird\":")
                                      .contains("\"animals\":{").contains("nameAnimal\":");

    }


}
