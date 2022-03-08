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
  @DisplayName("Be able to process embedded schema")
  void textEmbeddedTypeTestSchemaProcessor() throws KLoadGenException, IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/embeddedTypeTest.proto");
    List<FieldValueMapping> fieldValueMappingList = List.of(
        new FieldValueMapping("phones.addressesPhone[1:].id[1]", "string-array", 0, "Pablo"),
        new FieldValueMapping("phones.phoneType", "enum", 0, "[MOBILE, HOME, WORK]"));
    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
  @DisplayName("Be able to process map in schema")
  void testProtoBufMapTestProcessor() throws IOException, DescriptorValidationException {
    File testFile = fileHelper.getFile("/proto-files/mapTest.proto");
    List<FieldValueMapping> fieldValueMappingList = asList(
        new FieldValueMapping("name[:]", "string-map", 0, "Pablo"),
        new FieldValueMapping("addresses[:].street", "string", 0, "Sor Joaquina"),
        new FieldValueMapping("addresses[:].number", "int", 0, "2"),
        new FieldValueMapping("addresses[:].zipcode", "int", 0, "15011"),
        new FieldValueMapping("addressesNoDot[:].street", "string", 0, "Sor Joaquina"),
        new FieldValueMapping("addressesNoDot[:].number", "int", 0, "6"),
        new FieldValueMapping("addressesNoDot[:].zipcode", "int", 0, "15011"));
    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
        new FieldValueMapping("presents[:].options[]", "string-array", 0, ""));
    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
        new FieldValueMapping("create_user_id", "string", 0, ""));
    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
            new FieldValueMapping("load_type", "string", 0, ""),
            new FieldValueMapping("shipment.carrier_identifier.type", "enum", 0, "[CARRIER_IDENTIFIER_TYPE_UNSPECIFIED, CARRIER_IDENTIFIER_TYPE_DOT_NUMBER]"),
            new FieldValueMapping("shipment.carrier_identifier.value", "string", 0, ""),
            new FieldValueMapping("shipment.shipment_identifiers[].type", "enum", 0, "[SHIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, " +
                                                                                     "SHIPMENT_IDENTIFIER_TYPE_BILL_OF_LADING, SHIPMENT_IDENTIFIER_TYPE_ORDER]"),
            new FieldValueMapping("shipment.shipment_identifiers[].value", "string", 0, ""),
            new FieldValueMapping("shipment.equipment_identifiers[].type", "enum", 0, "[EQUIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_MOBILE_PHONE_NUMBER, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_VEHICLE_ID, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_LICENSE_PLATE, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_SENSITECH_DEVICE_ID, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_EMERSON_DEVICE_ID, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_TIVE_DEVICE_ID, " +
                                                                                      "EQUIPMENT_IDENTIFIER_TYPE_CONTAINER_ID]"),
            new FieldValueMapping("shipment.equipment_identifiers[].value", "string", 0, ""),
            new FieldValueMapping("shipment.attributes[]", "string-array", 0, ""),
            new FieldValueMapping("latest_status_update.timestamp", ".google.protobuf.Timestamp", 0, ""),
            new FieldValueMapping("latest_status_update.stop_number", "int", 0, ""),
            new FieldValueMapping("latest_status_update.status_code", "enum", 0, "[STATUS_UPDATE_CODE_UNSPECIFIED, STATUS_UPDATE_CODE_DISPATCHED, STATUS_UPDATE_CODE_IN_TRANSIT, " +
                                                                                 "STATUS_UPDATE_CODE_AT_STOP, STATUS_UPDATE_CODE_COMPLETED, STATUS_UPDATE_CODE_TRACKING_FAILED, " +
                                                                                 "STATUS_UPDATE_CODE_INFO, STATUS_UPDATE_CODE_DELETED]"),
            new FieldValueMapping("latest_status_update.status_reason", "enum", 0, "[STATUS_UPDATE_REASON_UNSPECIFIED, STATUS_UPDATE_REASON_PENDING_TRACKING_METHOD, " +
                                                                                   "STATUS_UPDATE_REASON_SCHEDULED, STATUS_UPDATE_REASON_PENDING_APPROVAL, " +
                                                                                   "STATUS_UPDATE_REASON_ACQUIRING_LOCATION, STATUS_UPDATE_REASON_PENDING_CARRIER, STATUS_UPDATE_REASON_IN_MOTION, STATUS_UPDATE_REASON_IDLE, STATUS_UPDATE_REASON_APPROVAL_DENIED, STATUS_UPDATE_REASON_TIMED_OUT, STATUS_UPDATE_REASON_CANCELED, STATUS_UPDATE_REASON_DEPARTED_FINAL_STOP, STATUS_UPDATE_REASON_ARRIVED_FINAL_STOP, STATUS_UPDATE_REASON_ARRIVED_FAILED_TO_ACQUIRE_LOCATION, STATUS_UPDATE_REASON_INFO]"),
            new FieldValueMapping("latest_status_update.geo_coordinates.latitude", "double", 0, ""),
            new FieldValueMapping("latest_status_update.geo_coordinates.longitude", "double", 0, ""),
            new FieldValueMapping("latest_status_update.address.postal_code", "string", 0, ""),
            new FieldValueMapping("latest_status_update.address.address_lines[]", "string-array", 0, ""),
            new FieldValueMapping("latest_status_update.address.city", "string", 0, ""),
            new FieldValueMapping("latest_status_update.address.state", "string", 0, ""),
            new FieldValueMapping("latest_status_update.address.country", "string", 0, ""),
            new FieldValueMapping("latest_stop_statuses[].stop_number", "int", 0, ""),
            new FieldValueMapping("latest_stop_statuses[].status_code", "enum", 0, "[STOP_STATUS_CODE_UNSPECIFIED, STOP_STATUS_CODE_UNKNOWN, STOP_STATUS_CODE_EN_ROUTE, " +
                                                                                   "STOP_STATUS_CODE_ARRIVED, STOP_STATUS_CODE_DEPARTED]"),
            new FieldValueMapping("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.start_date_time", ".google.protobuf.Timestamp", 0, ""),
            new FieldValueMapping("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.end_date_time", ".google.protobuf.Timestamp", 0, ""),
            new FieldValueMapping("latest_stop_statuses[].arrival_estimate.last_calculated_date_time", ".google.protobuf.Timestamp", 0, ""),
            new FieldValueMapping("latest_stop_statuses[].arrival_code", "enum", 0, "[ARRIVAL_CODE_UNSPECIFIED, ARRIVAL_CODE_UNKNOWN, ARRIVAL_CODE_EARLY, ARRIVAL_CODE_ON_TIME, " +
                                                                                    "ARRIVAL_CODE_LATE]"),
            new FieldValueMapping("latest_stop_statuses[].additional_appointment_window_statuses[]", "string-array", 0, "")
        );

    ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
    protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile, "Protobuf"), new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = protobufSchemaProcessor.next();
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
}
