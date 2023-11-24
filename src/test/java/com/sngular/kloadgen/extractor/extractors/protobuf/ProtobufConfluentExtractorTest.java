package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.io.File;
import java.util.List;
import java.util.Locale;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtobufConfluentExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final Extractor<ParsedSchema> protoBufConfluentExtractor = new ProtoBufConfluentExtractor();

  @BeforeEach
  public void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  
  @Test
  @DisplayName("Test Extractor with simple proto file")
  void testFlatProperties() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/easyTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(3)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("street").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("number[]").fieldType("int-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("zipcode").fieldType("long").required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with data structure map and array")
  void testEmbeddedTypes() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/embeddedTypeTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(2)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("phones.addressesPhone[:].id[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("phones.phoneType").fieldType("enum").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                                   .isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with data structure enums and collections")
  void testEnumType() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/enumTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(3)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("phoneTypes").fieldType("enum").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("phoneTypesArray[]").fieldType("enum-array").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("phoneTypesMap[:]").fieldType("enum-map").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                                   .isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with data structure Any of")
  void testOneOfsType() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/oneOfTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(4)
              .contains(
                  FieldValueMapping.builder().fieldName("type.street").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("type.number").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("type.test").fieldType("string").required(true).isAncestorRequired(true).build()
              )
              .containsAnyOf(
                  FieldValueMapping.builder().fieldName("optionString").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("optionLong").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("optionInt").fieldType("int").required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with complex structure")
  void testComplexProto() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/complexTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(13)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("phone_types[].phone").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("phone_types[].principal").fieldType("boolean").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("name").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("age").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("address[].street[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("address[].number_street").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("pets[:].pet_name").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("pets[:].pet_age").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("pets[:].owner").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("descriptors[:]").fieldType("string-map").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("dates[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("response").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("presents[:].options[]").fieldType("string-array").required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with real proto")
  void testProvided() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/providedTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(32)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("id").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("occurrence_id").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("load_number").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("claim_type.code").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("claim_type.description").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("collision_type.code").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("collision_type.description").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_cause_type.code").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_cause_type.description").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_type.code").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_type.description").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("review_status_type.code").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("review_status_type.description").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_latitude").fieldType("double").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_longitude").fieldType("double").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_date").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_time").fieldType(".google.protobuf.Timestamp").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_city").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_state").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("location_description").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_equipment_details[].equipment_number").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_equipment_details[].equipment_type").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("incident_equipment_details[].equipment_prefix").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("driver.driver_id").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("driver.driver_first_name").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("driver.driver_last_name").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("dot_accident_indicator").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("drug_test_required_indicator").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("hazardous_material_indicator").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("preventable_indicator").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("report_by_name").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("create_user_id").fieldType("string").required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with data structure maps")
  void testMap() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/mapTest.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(7)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("name[:]").fieldType("string-map").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addresses[:].street").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addresses[:].number").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addresses[:].zipcode").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesNoDot[:].street").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesNoDot[:].number").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesNoDot[:].zipcode").fieldType("int").required(true).isAncestorRequired(true).build()
              );
  }

  @Test
  @DisplayName("Test Extractor with multi types")
  void completeTest() throws Exception {
    final String testFile = fileHelper.getContent("/proto-files/completeProto.proto");
    final ParsedSchema schema = new ParsedSchema(testFile, "PROTOBUF");
    final List<FieldValueMapping> fieldValueMappingList = protoBufConfluentExtractor.processSchema(schema);
    Assertions.assertThat(fieldValueMappingList)
              .hasSize(11)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("name").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("id").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesArray[].id[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesArray[].zipcode").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesDot[].id[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesDot[].zipcode").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesMap[:].id[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesMap[:].zipcode").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesNoDotMap[:].id[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("addressesNoDotMap[:].zipcode").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("phones[]").fieldType("enum-array").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                                   .isAncestorRequired(true).build()
              );
  }

}