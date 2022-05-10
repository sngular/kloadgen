package net.coru.kloadgen.extractor.extractors;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtobufExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final ProtoBufExtractor protoBufExtractor = new ProtoBufExtractor();

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
  @DisplayName("Test Extractor with simple proto file")
  void testFlatProperties() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/easyTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("street", "string", 0, ""),
            new FieldValueMapping("number[]", "int-array", 0, ""),
            new FieldValueMapping("zipcode", "long", 0, "")
        );
  }

  @Test
  @DisplayName("Test Extractor with data structure map and array")
  void testEmbeddedTypes() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/embeddedTypeTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("phones.addressesPhone[:].id[]", "string-array", 0, ""),
            new FieldValueMapping("phones.phoneType", "enum", 0, "[MOBILE, HOME, WORK]")
        );
  }

  @Test
  @DisplayName("Test Extractor with data structure enums and collections")
  void testEnumType() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/enumTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("phoneTypes", "enum", 0, "[MOBILE, HOME, WORK]"),
            new FieldValueMapping("phoneTypesArray[]", "enum-array", 0, "[MOBILE, HOME, WORK]"),
            new FieldValueMapping("phoneTypesMap[:]", "enum-map", 0, "[MOBILE, HOME, WORK]")
        );
  }

  @Test
  @DisplayName("Test Extractor with data structure Any of")
  void testOneOfsType() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/oneOfTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(4)
        .contains(
            new FieldValueMapping("type.street", "string", 0, ""),
            new FieldValueMapping("type.number", "int", 0, ""),
            new FieldValueMapping("type.test", "string", 0, "")
        )
        .containsAnyOf(
            new FieldValueMapping("optionString", "string", 0, ""),
            new FieldValueMapping("optionLong", "long", 0, ""),
            new FieldValueMapping("optionInt", "int", 0, "")
        );
  }

  @Test
  @DisplayName("Test Extractor with complex structure")
  void testComplexProto() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/complexTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(13)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("phone_types[].phone", "long", 0, ""),
            new FieldValueMapping("phone_types[].principal", "boolean", 0, ""),
            new FieldValueMapping("name", "string", 0, ""),
            new FieldValueMapping("age", "int", 0, ""),
            new FieldValueMapping("address[].street[]", "string-array", 0, ""),
            new FieldValueMapping("address[].number_street", "int", 0, ""),
            new FieldValueMapping("pets[:].pet_name", "string", 0, ""),
            new FieldValueMapping("pets[:].pet_age", "int", 0, ""),
            new FieldValueMapping("pets[:].owner", "string", 0, ""),
            new FieldValueMapping("descriptors[:]", "string-map", 0, ""),
            new FieldValueMapping("dates[]", "string-array", 0, ""),
            new FieldValueMapping("response", "string", 0, ""),
            new FieldValueMapping("presents[:].options[]", "string-array", 0, "")
        );
  }

  @Test
  @DisplayName("Test Extractor with real proto")
  void testProvided() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/providedTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(32)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("id", "int", 0, ""),
            new FieldValueMapping("occurrence_id", "string", 0, ""),
            new FieldValueMapping("load_number", "string", 0, ""),
            new FieldValueMapping("claim_type.code", "string", 0, ""),
            new FieldValueMapping("claim_type.description", "string", 0, ""),
            new FieldValueMapping("collision_type.code", "string", 0, ""),
            new FieldValueMapping("collision_type.description", "string", 0, ""),
            new FieldValueMapping("incident_cause_type.code", "string", 0, ""),
            new FieldValueMapping("incident_cause_type.description", "string", 0, ""),
            new FieldValueMapping("incident_type.code", "string", 0, ""),
            new FieldValueMapping("incident_type.description", "string", 0, ""),
            new FieldValueMapping("review_status_type.code", "string", 0, ""),
            new FieldValueMapping("review_status_type.description", "string", 0, ""),
            new FieldValueMapping("incident_latitude", "double", 0, ""),
            new FieldValueMapping("incident_longitude", "double", 0, ""),
            new FieldValueMapping("incident_date", "string", 0, ""),
            new FieldValueMapping("incident_time", ".google.protobuf.Timestamp", 0, ""),
            new FieldValueMapping("incident_city", "string", 0, ""),
            new FieldValueMapping("incident_state", "string", 0, ""),
            new FieldValueMapping("location_description", "string", 0, ""),
            new FieldValueMapping("incident_equipment_details[].equipment_number", "string", 0, ""),
            new FieldValueMapping("incident_equipment_details[].equipment_type", "string", 0, ""),
            new FieldValueMapping("incident_equipment_details[].equipment_prefix", "string", 0, ""),
            new FieldValueMapping("driver.driver_id", "int", 0, ""),
            new FieldValueMapping("driver.driver_first_name", "string", 0, ""),
            new FieldValueMapping("driver.driver_last_name", "string", 0, ""),
            new FieldValueMapping("dot_accident_indicator", "string", 0, ""),
            new FieldValueMapping("drug_test_required_indicator", "string", 0, ""),
            new FieldValueMapping("hazardous_material_indicator", "string", 0, ""),
            new FieldValueMapping("preventable_indicator", "string", 0, ""),
            new FieldValueMapping("report_by_name", "string", 0, ""),
            new FieldValueMapping("create_user_id", "string", 0, "")

        );
  }

  @Test
  @DisplayName("Test Extractor with data structure maps")
  void testMap() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/mapTest.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(7)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("name[:]", "string-map", 0, ""),
            new FieldValueMapping("addresses[:].street", "string", 0, ""),
            new FieldValueMapping("addresses[:].number", "int", 0, ""),
            new FieldValueMapping("addresses[:].zipcode", "int", 0, ""),
            new FieldValueMapping("addressesNoDot[:].street", "string", 0, ""),
            new FieldValueMapping("addressesNoDot[:].number", "int", 0, ""),
            new FieldValueMapping("addressesNoDot[:].zipcode", "int", 0, "")
        );
  }

  @Test
  @DisplayName("Test Extractor with multi types")
  void completeTest() throws Exception {
    String testFile = fileHelper.getContent("/proto-files/completeProto.proto");
    List<FieldValueMapping> fieldValueMappingList = protoBufExtractor.processSchema(new ProtobufSchema(testFile).rawSchema());
    assertThat(fieldValueMappingList)
        .hasSize(11)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("name", "string", 0, ""),
            new FieldValueMapping("id", "int", 0, ""),
            new FieldValueMapping("addressesArray[].id[]", "string-array", 0, ""),
            new FieldValueMapping("addressesArray[].zipcode", "long", 0, ""),
            new FieldValueMapping("addressesDot[].id[]", "string-array", 0, ""),
            new FieldValueMapping("addressesDot[].zipcode", "long", 0, ""),
            new FieldValueMapping("addressesMap[:].id[]", "string-array", 0, ""),
            new FieldValueMapping("addressesMap[:].zipcode", "long", 0, ""),
            new FieldValueMapping("addressesNoDotMap[:].id[]", "string-array", 0, ""),
            new FieldValueMapping("addressesNoDotMap[:].zipcode", "long", 0, ""),
            new FieldValueMapping("phones[]", "enum-array", 0, "[MOBILE, HOME, WORK]")
        );
  }
}
