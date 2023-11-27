package com.sngular.kloadgen.sampler;

import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class AsyncApiSamplerTest {
    private final FileHelper fileHelper = new FileHelper();

    private JMeterContext jmcx;

    private static Stream<Object> parametersForConfigureValueGeneratorTest() {
        return Stream.of("localhost:8081", "");
    }

    private AsyncApiSampler sampler = new AsyncApiSampler();

    private ApiExtractor apiExtractor;
    @BeforeEach
    public final void setUp() throws IOException {
        final File file = new File("src/test/resources");
        final String absolutePath = file.getAbsolutePath();
        JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
        jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());
        JMeterUtils.setLocale(Locale.ENGLISH);
        sampler = new AsyncApiSampler();
        apiExtractor = mock(ApiExtractor.class);

    }

    /*@Test
    void testAsyncApiSampleBasic() throws IOException {
        JsonNode asyncApiFileNode = mock(JsonNode.class);
        sampler.setAsyncApiFileNode(asyncApiFileNode);
        JMeterVariables jmvar = new JMeterVariables();
        jmvar.putAll(getVariablesJsonSchema());




    }*/

/*
    public JMeterVariables getVariablesJsonSchema() throws IOException {

        final File testFile = fileHelper.getFile("/asyncapi/event-api.yml");
        final ParsedSchema parsedSchema = SchemaParseUtil.getParsedSchema(testFile, "JSON");

        final var variables = new JMeterVariables();
        variables.put(PropsKeysHelper.KEY_SCHEMA_TYPE, "JSON");
        variables.put(PropsKeysHelper.VALUE_SUBJECT_NAME, "jsonSubject");
        variables.put(PropsKeysHelper.KEY_SUBJECT_NAME, "jsonSubject");
        variables.put(PropsKeysHelper.VALUE_SCHEMA, String.valueOf(parsedSchema));
        variables.put(PropsKeysHelper.KEY_SCHEMA, String.valueOf(parsedSchema));
        variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, JsonSchemaFixturesConstants.SIMPLE_SCHEMA_NONREQUIRED);
        variables.put(ProducerKeysHelper.KEY_NAME_STRATEGY, "theStrategy");
        return variables;
    }*/



}