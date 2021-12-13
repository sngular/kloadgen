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
                .hasSize(32)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("IncidentEvent.id", "int", 0, ""),
                        new FieldValueMapping("IncidentEvent.occurrence_id", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.load_number", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.claim_type.code", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.claim_type.description", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.collision_type.code", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.collision_type.description", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_cause_type.code", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_cause_type.description", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_type.code", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_type.description", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.review_status_type.code", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.review_status_type.description", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_latitude", "double", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_longitude", "double", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_date", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_time", "Timestamp", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_city", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_state", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.location_description", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_equipment_details[].equipment_number", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_equipment_details[].equipment_type", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.incident_equipment_details[].equipment_prefix", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.driver.driver_id", "int", 0, ""),
                        new FieldValueMapping("IncidentEvent.driver.driver_first_name", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.driver.driver_last_name", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.dot_accident_indicator", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.drug_test_required_indicator", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.hazardous_material_indicator", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.preventable_indicator", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.report_by_name", "string", 0, ""),
                        new FieldValueMapping("IncidentEvent.create_user_id", "string", 0, "")

                );
    }

    @Test
    void testMap() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/mapTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        assertThat(fieldValueMappingList)
                .hasSize(7)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("Person.name[:]", "string", 0, ""),
                        new FieldValueMapping("Person.addresses[:].street", "string", 0, ""),
                        new FieldValueMapping("Person.addresses[:].number", "int", 0, ""),
                        new FieldValueMapping("Person.addresses[:].zipcode", "int", 0, ""),
                        new FieldValueMapping("Person.addressesNoDot[:].street", "string", 0, ""),
                        new FieldValueMapping("Person.addressesNoDot[:].number", "int", 0, ""),
                        new FieldValueMapping("Person.addressesNoDot[:].zipcode", "int", 0, "")
                );
    }

    @Test
    void completeTest() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/completeProto.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        assertThat(fieldValueMappingList)
                .hasSize(12)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("Person.name", "string", 0, ""),
                        new FieldValueMapping("Person.id", "int", 0, ""),
                        new FieldValueMapping("Person.addressesArray[].id[]", "string", 0, ""),
                        new FieldValueMapping("Person.addressesArray[].zipcode", "long", 0, ""),
                        new FieldValueMapping("Person.addressesDot[].id[]", "string", 0, ""),
                        new FieldValueMapping("Person.addressesDot[].zipcode", "long", 0, ""),
                        new FieldValueMapping("Person.addressesMap[:].id[]", "string", 0, ""),
                        new FieldValueMapping("Person.addressesMap[:].zipcode", "long", 0, ""),
                        new FieldValueMapping("Person.addressesNoDotMap[:].id[]", "string", 0, ""),
                        new FieldValueMapping("Person.addressesNoDotMap[:].zipcode", "long", 0, ""),
                        new FieldValueMapping("Person.phones[]", "enum", 0, "[MOBILE, HOME, WORK]"),
                        new FieldValueMapping("Pet.name[]", "string", 0, "")
                );
    }
}
