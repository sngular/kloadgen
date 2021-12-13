package net.coru.kloadgen.extractor;

import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
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

class ProtobufExtractorTest {

    private final FileHelper fileHelper = new FileHelper();
    private final SchemaExtractor schemaExtractor = new SchemaExtractorImpl();

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
    void testFlatProperties() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/easyTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        assertThat(fieldValueMappingList)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("Address.street", "string", 0, ""),
                        new FieldValueMapping("Address.number[]", "int", 0, ""),
                        new FieldValueMapping("Address.zipcode", "long", 0, "")
                );
    }

    @Test
    void testEmbeddedTypes() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/embeddedTypeTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        assertThat(fieldValueMappingList)
                .hasSize(7)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("Person.name", "string", 0, ""),
                        new FieldValueMapping("Person.id", "int", 0, ""),
                        new FieldValueMapping("Person.email", "string", 0, ""),
                        new FieldValueMapping("Person.addresses[].id", "string", 0, ""),
                        new FieldValueMapping("Person.phones[].number", "string", 0, ""),
                        new FieldValueMapping("Person.phones[].addresses[].id", "string", 0, ""),
                        new FieldValueMapping("Book.name[]","string", 0, "")
                );
    }

    @Test
    void testEnumType() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/enumTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        assertThat(fieldValueMappingList)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("Person.phoneTypes", "enum", 0, "[MOBILE, HOME, WORK]")

                );
    }

    @Test
    void testProvided() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/providedTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        assertThat(fieldValueMappingList)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("Person.phoneTypes", "enum", 0, "[MOBILE, HOME, WORK]")

                );
    }

}
