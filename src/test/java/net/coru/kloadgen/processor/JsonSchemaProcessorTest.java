package net.coru.kloadgen.processor;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

    @Test
    void testNullOnOptionalFields() throws IOException {
        File testFile = fileHelper.getFile("/jsonschema/basic.jcs");
        List<FieldValueMapping> fieldValueMappingList =
                extractor.flatPropertiesList(extractor.schemaTypesList(testFile, "JSON"));

        fieldValueMappingList.get(0).setFieldValuesList("null");
        fieldValueMappingList.get(1).setFieldValuesList("null");
        fieldValueMappingList.get(2).setFieldValuesList("null");

        JsonSchemaProcessor jsonSchemaProcessor = new JsonSchemaProcessor();
        jsonSchemaProcessor.processSchema(fieldValueMappingList);
        ObjectNode message = jsonSchemaProcessor.next();

        assertThat(message.get("firstName")).isNull();
        assertThat(message.get("lastName")).isNotNull();
        assertThat(message.get("age")).isNotNull();
    }
}
