package net.coru.kloadgen.processor;

import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.COMPLEX_SCHEMA;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.COMPLEX_SCHEMA_EXPECTED;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_ARRAY;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_ARRAY_EXPECTED;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_EXPECTED;
import static net.coru.kloadgen.processor.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_MAP;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

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
                Arguments.of(SIMPLE_SCHEMA, SIMPLE_SCHEMA_EXPECTED),
                Arguments.of(SIMPLE_SCHEMA_ARRAY, SIMPLE_SCHEMA_ARRAY_EXPECTED),
                Arguments.of(COMPLEX_SCHEMA, COMPLEX_SCHEMA_EXPECTED)
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
    void testNullOnOptionalMapWithChildren(){
        JsonSchemaProcessor jsonSchemaProcessor = new JsonSchemaProcessor();
        jsonSchemaProcessor.processSchema(SIMPLE_SCHEMA_MAP);
        ObjectNode message = jsonSchemaProcessor.next();

        assertThat(message.toString()).contains("lastName\":\"García")
                .contains("itemTipo\":{").doesNotContain("itemType\":{");

    }
}
