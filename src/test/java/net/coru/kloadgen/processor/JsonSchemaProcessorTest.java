package net.coru.kloadgen.processor;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
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

    private final FileHelper fileHelper = new FileHelper();
    private final SchemaExtractor extractor = new SchemaExtractorImpl();

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
}
