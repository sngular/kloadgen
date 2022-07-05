package net.coru.kloadgen.processor;

import static java.util.Arrays.asList;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtobufSchemaProcessorTest {

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
  @DisplayName("Be able to process simple schema")
  void testProtoBufFactory() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/complexTest.proto");
    List<FieldValueMapping> fieldValueMappingList = asList(
        FieldValueMapping.builder().fieldName("name").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("age").fieldType("int").required(true).isAncestorRequired(true).build());

    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );

    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();
    assertThat(assertKeys).hasSize(9)
                          .containsExactlyInAnyOrder("tutorial.Test.name",
                                                     "tutorial.Test.age");
    assertThat(assertValues).hasSize(9).isNotNull();
    assertThat(assertValues.get(2)).isInstanceOf(Integer.class);
    assertThat(assertValues.get(7)).isInstanceOf(String.class);
  }



  @Test
  @DisplayName("Be able to process embedded schema")
  void textEmbeddedTypeTestSchemaProcessor() throws KLoadGenException, IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/embeddedTypeTest.proto");
    List<FieldValueMapping> fieldValueMappingList = List.of(
        FieldValueMapping.builder().fieldName("phones.addressesPhone[1:].id[1]").fieldType("string-array").fieldValueList("Pablo").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("phones.phoneType").fieldType("enum").fieldValueList("[MOBILE, HOME, WORK]").required(true).isAncestorRequired(true).build());
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );
    String idField = getIdFieldForEmbeddedTypeTest(assertValues);
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(assertKeys).hasSize(1).containsExactlyInAnyOrder("tutorial.Person.phones");
    assertThat(idField).isEqualTo("[Pablo]");
  }

  @Test
  @DisplayName("Be able to process complex types like StringValue or Int32Value and get values by default")
  void testProtobufGoogleTypes() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/googleTypesTest.proto");
    List<FieldValueMapping> fieldValueMappingList = List.of(
        FieldValueMapping.builder().fieldName("id").fieldType("Int32Value").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("occurrence_id").fieldType("StringValue").fieldValueList("Isabel").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("load_number").fieldType("Int32Value").required(true).isAncestorRequired(true).build());
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );
    DynamicMessage dynamicMessage = (DynamicMessage) assertValues.get(1);
    String secondValue = (String) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(secondValue).isEqualTo("Isabel");
    assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("abc.Incident.id",
                                                                "abc.Incident.occurrence_id",
                                                                "abc.Incident.load_number");
  }

  private String getIdFieldForEmbeddedTypeTest(List<Object> assertValues) {
    DynamicMessage dynamicMessage = (DynamicMessage) assertValues.get(0);
    DynamicMessage firstMap = (DynamicMessage) ((List) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("addressesPhone"))).get(0);
    DynamicMessage secondMapAsDynamicField = (DynamicMessage) firstMap.getField(firstMap.getDescriptorForType().findFieldByName("value"));
    return secondMapAsDynamicField.getField(secondMapAsDynamicField.getDescriptorForType().findFieldByName("id")).toString();
  }

  @Test
  @DisplayName("Be able to process enum in the schema")
  void testProtoBufEnumSchemaProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/enumTest.proto");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
    fieldValueMappingList.get(0).setFieldValuesList("HOME, WORK");
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );
    String firstValue = assertValues.get(0).toString();
    List<Object> secondValue = (List<Object>) assertValues.get(1);
    List<Object> thirdValueMap = (List<Object>) assertValues.get(2);
    DynamicMessage dynamicMessage = (DynamicMessage) thirdValueMap.get(0);
    Object thirdValue = dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(firstValue)
        .isNotNull()
        .isIn("HOME", "WORK", "MOBILE");
    assertThat(secondValue.get(0).toString())
        .isNotNull()
        .isIn("HOME", "WORK", "MOBILE");
    assertThat(thirdValue.toString())
        .isNotNull()
        .isIn("HOME", "WORK", "MOBILE");
    assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.phoneTypes",
                                                                "tutorial.Person.phoneTypesArray",
                                                                "tutorial.Person.phoneTypesMap");
  }

  @Test
  @DisplayName("Be able to process easy schema")
  void testProtoBufEasyTestProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/easyTest.proto");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );
    List<Object> integerList = (List<Object>) assertValues.get(1);
    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();
    assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Address.street", "tutorial.Address.number", "tutorial.Address.zipcode");
    assertThat(assertValues).hasSize(3);
    assertThat(assertValues.get(0)).isInstanceOf(String.class);
    assertThat(integerList.get(0)).isInstanceOf(Integer.class);
    assertThat(assertValues.get(2)).isInstanceOf(Long.class);

  }

  @Test
  @DisplayName("Be able to process oneOf fields")
  void testProtoBufOneOfProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/oneOfTest.proto");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );
    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();
    assertThat(assertKeys)
        .hasSize(2)
        .containsAnyOf("tutorial.Address.type", "tutorial.Address.optionInt", "tutorial.Address.optionLong", "tutorial.Address.optionString")
        .element(0)
        .isEqualTo("tutorial.Address.type");
    assertThat(assertValues).hasSize(2);

  }

  @Test
  @DisplayName("Be able to process map in schema")
  void testProtoBufMapTestProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/mapTest.proto");
    List<FieldValueMapping> fieldValueMappingList = asList(
        FieldValueMapping.builder().fieldName("name[:]").fieldType("string-map").fieldValueList("Pablo").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addresses[:].street").fieldType("string").fieldValueList("Sor Joaquina").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addresses[:].number").fieldType("int").fieldValueList("2").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addresses[:].zipcode").fieldType("int").fieldValueList("15011").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addressesNoDot[:].street").fieldType("string").fieldValueList("Sor Joaquina").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addressesNoDot[:].number").fieldType("int").fieldValueList("6").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addressesNoDot[:].zipcode").fieldType("int").fieldValueList("15011").required(true).isAncestorRequired(true).build());
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );
    String personName = getPersonNameForMapTestProcessor(assertValues);
    List<Object> objectList = (List<Object>) assertValues.get(1);
    DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
    Object street = getSubFieldForMapTestProcessor(dynamicMessage, "street");
    Object number = getSubFieldForMapTestProcessor(dynamicMessage, "number");
    Object zipcode = getSubFieldForMapTestProcessor(dynamicMessage, "zipcode");

    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();
    assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.name",
                                                                "tutorial.Person.addresses",
                                                                "tutorial.Person.addressesNoDot");
    assertThat(assertValues).hasSize(3);
    assertThat(personName).isEqualTo("Pablo");
    assertThat(street).isInstanceOf(String.class).isEqualTo("Sor Joaquina");
    assertThat(number).isInstanceOf(Integer.class).isEqualTo(2);
    assertThat(zipcode).isInstanceOf(Integer.class).isEqualTo(15011);
  }

  private String getPersonNameForMapTestProcessor(List<Object> assertValues) {
    List<Object> objectList = (List<Object>) assertValues.get(0);
    DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
    return (String) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
  }

  private Object getSubFieldForMapTestProcessor(DynamicMessage dynamicMessage, String field) {
    DynamicMessage subDynamicMessage = (DynamicMessage) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    return subDynamicMessage.getField(subDynamicMessage.getDescriptorForType().findFieldByName(field));
  }

  @Test
  @DisplayName("Be able to process complex schema")
  void testProtoBufComplexTestProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/complexTest.proto");
    List<FieldValueMapping> fieldValueMappingList = asList(
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
        FieldValueMapping.builder().fieldName("presents[:].options[]").fieldType("string-array").required(true).isAncestorRequired(true).build());
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );

    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();
    assertThat(assertKeys).hasSize(9)
                          .containsExactlyInAnyOrder("tutorial.Test.phone_types",
                                                     "tutorial.Test.name",
                                                     "tutorial.Test.age",
                                                     "tutorial.Test.address",
                                                     "tutorial.Test.pets",
                                                     "tutorial.Test.descriptors",
                                                     "tutorial.Test.dates",
                                                     "tutorial.Test.response",
                                                     "tutorial.Test.presents");
    assertThat(assertValues).hasSize(9).isNotNull();
    assertThat(assertValues.get(2)).isInstanceOf(Integer.class);
    assertThat(assertValues.get(7)).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("Be able to process provided complex schema")
  void testProtoBufProvidedComplexTestProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/providedTest.proto");
    List<FieldValueMapping> fieldValueMappingList = asList(
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
        FieldValueMapping.builder().fieldName("create_user_id").fieldType("string").required(true).isAncestorRequired(true).build());
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );

    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();
    assertThat(assertKeys).hasSize(23)
                          .containsExactlyInAnyOrder(
                              "company.IncidentEvent.id",
                              "company.IncidentEvent.occurrence_id",
                              "company.IncidentEvent.load_number",
                              "company.IncidentEvent.claim_type",
                              "company.IncidentEvent.collision_type",
                              "company.IncidentEvent.incident_cause_type",
                              "company.IncidentEvent.incident_type",
                              "company.IncidentEvent.review_status_type",
                              "company.IncidentEvent.incident_latitude",
                              "company.IncidentEvent.incident_longitude",
                              "company.IncidentEvent.incident_date",
                              "company.IncidentEvent.incident_time",
                              "company.IncidentEvent.incident_city",
                              "company.IncidentEvent.incident_state",
                              "company.IncidentEvent.location_description",
                              "company.IncidentEvent.incident_equipment_details",
                              "company.IncidentEvent.driver",
                              "company.IncidentEvent.dot_accident_indicator",
                              "company.IncidentEvent.drug_test_required_indicator",
                              "company.IncidentEvent.hazardous_material_indicator",
                              "company.IncidentEvent.preventable_indicator",
                              "company.IncidentEvent.report_by_name",
                              "company.IncidentEvent.create_user_id");
    assertThat(assertValues).isNotNull().hasSize(23);
  }

  @Test
  void testFailing() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/deveTest.proto");
    List<FieldValueMapping> fieldValueMappingList = asList(
        FieldValueMapping.builder().fieldName("load_type").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.carrier_identifier.type").fieldType("enum")
                         .fieldValueList("[CARRIER_IDENTIFIER_TYPE_UNSPECIFIED, CARRIER_IDENTIFIER_TYPE_DOT_NUMBER]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.carrier_identifier.value").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.shipment_identifiers[].type").fieldType("enum").fieldValueList("[SHIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, " +
                                                                                                                       "SHIPMENT_IDENTIFIER_TYPE_BILL_OF_LADING, " +
                                                                                                                       "SHIPMENT_IDENTIFIER_TYPE_ORDER]")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.shipment_identifiers[].value").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.equipment_identifiers[].type").fieldType("enum").fieldValueList("[EQUIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_MOBILE_PHONE_NUMBER, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_VEHICLE_ID, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_LICENSE_PLATE, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_SENSITECH_DEVICE_ID, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_EMERSON_DEVICE_ID, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_TIVE_DEVICE_ID, " +
                                                                                                                        "EQUIPMENT_IDENTIFIER_TYPE_CONTAINER_ID]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.equipment_identifiers[].value").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.attributes[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.timestamp").fieldType(".google.protobuf.Timestamp").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.stop_number").fieldType("int").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.status_code").fieldType("enum")
                         .fieldValueList("[STATUS_UPDATE_CODE_UNSPECIFIED, STATUS_UPDATE_CODE_DISPATCHED, STATUS_UPDATE_CODE_IN_TRANSIT, " +
                                         "STATUS_UPDATE_CODE_AT_STOP, STATUS_UPDATE_CODE_COMPLETED, STATUS_UPDATE_CODE_TRACKING_FAILED, " +
                                         "STATUS_UPDATE_CODE_INFO, STATUS_UPDATE_CODE_DELETED]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.status_reason").fieldType("enum")
                         .fieldValueList("[STATUS_UPDATE_REASON_UNSPECIFIED, STATUS_UPDATE_REASON_PENDING_TRACKING_METHOD, " +
                                         "STATUS_UPDATE_REASON_SCHEDULED, STATUS_UPDATE_REASON_PENDING_APPROVAL, " +
                                         "STATUS_UPDATE_REASON_ACQUIRING_LOCATION, STATUS_UPDATE_REASON_PENDING_CARRIER, STATUS_UPDATE_REASON_IN_MOTION, " +
                                         "STATUS_UPDATE_REASON_IDLE, STATUS_UPDATE_REASON_APPROVAL_DENIED, STATUS_UPDATE_REASON_TIMED_OUT, STATUS_UPDATE_REASON_CANCELED, " +
                                         "STATUS_UPDATE_REASON_DEPARTED_FINAL_STOP, STATUS_UPDATE_REASON_ARRIVED_FINAL_STOP, " +
                                         "STATUS_UPDATE_REASON_ARRIVED_FAILED_TO_ACQUIRE_LOCATION, STATUS_UPDATE_REASON_INFO]")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.geo_coordinates.latitude").fieldType("double").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.geo_coordinates.longitude").fieldType("double").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.address.postal_code").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.address.address_lines[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.address.city").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.address.state").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.address.country").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].stop_number").fieldType("int").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].status_code").fieldType("enum")
                         .fieldValueList("[STOP_STATUS_CODE_UNSPECIFIED, STOP_STATUS_CODE_UNKNOWN, STOP_STATUS_CODE_EN_ROUTE, " +
                                         "STOP_STATUS_CODE_ARRIVED, STOP_STATUS_CODE_DEPARTED]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.start_date_time").fieldType(".google.protobuf.Timestamp")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.end_date_time").fieldType(".google.protobuf.Timestamp")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_estimate.last_calculated_date_time").fieldType(".google.protobuf.Timestamp").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_code").fieldType("enum")
                         .fieldValueList("[ARRIVAL_CODE_UNSPECIFIED, ARRIVAL_CODE_UNKNOWN, ARRIVAL_CODE_EARLY, ARRIVAL_CODE_ON_TIME, " +
                                         "ARRIVAL_CODE_LATE]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].additional_appointment_window_statuses[]").fieldType("string-array").required(true).isAncestorRequired(true)
                         .build()
    );

    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );

    assertThat(message).isNotNull()
                       .isInstanceOf(EnrichedRecord.class)
                       .extracting(EnrichedRecord::getGenericRecord)
                       .isNotNull();

    assertThat(assertKeys).hasSize(4);

  }

  @Test
  @DisplayName("Be able to process Date and TimeOfDay types")
  void testDateTimeTypes() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/dateTimeTest.proto");
    List<FieldValueMapping> fieldValueMappingList = List.of(
        FieldValueMapping.builder().fieldName("incident_date").fieldType(".google.type.Date").fieldValueList("2022-05-30").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("incident_time").fieldType(".google.type.TimeOfDay").fieldValueList("14:20:30-05:00").required(true).isAncestorRequired(true)
                         .build());
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    List<String> assertKeys = new ArrayList<>();
    List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) ->
                {
                  assertKeys.add(key.getFullName());
                  assertValues.add(value);
                }
    );

    DynamicMessage firstDynamicMessage = (DynamicMessage) assertValues.get(0);
    Integer year = (Integer) firstDynamicMessage.getField(firstDynamicMessage.getDescriptorForType().findFieldByName("year"));
    Integer month = (Integer) firstDynamicMessage.getField(firstDynamicMessage.getDescriptorForType().findFieldByName("month"));
    Integer day = (Integer) firstDynamicMessage.getField(firstDynamicMessage.getDescriptorForType().findFieldByName("day"));

    DynamicMessage secondDynamicMessage = (DynamicMessage) assertValues.get(1);
    Integer hours = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("hours"));
    Integer minutes = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("minutes"));
    Integer seconds = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("seconds"));
    Integer nanos = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("nanos"));

    assertThat(year).isEqualTo(2022);
    assertThat(month).isEqualTo(5);
    assertThat(day).isEqualTo(30);
    assertThat(hours).isEqualTo(9);
    assertThat(minutes).isEqualTo(20);
    assertThat(seconds).isEqualTo(30);
    assertThat(nanos).isEqualTo(0);
    assertThat(assertKeys).hasSize(2).containsExactlyInAnyOrder("abc.Incident.incident_date",
                                                                "abc.Incident.incident_time");
  }
}