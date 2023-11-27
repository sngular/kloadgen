package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.commons.math3.util.Pair;

public class ProtobufSerializerTestFixture {

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLETE_PROTO = new Pair<>(
      new FileHelper().getFile("/proto-files/completeProto.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("name", "string"),
          SerializerTestFixture.createFieldValueMapping("id", "int"),
          SerializerTestFixture.createFieldValueMapping("addressesArray[].id[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("addressesArray[].zipcode", "long"),
          SerializerTestFixture.createFieldValueMapping("addressesDot[].id[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("addressesDot[].zipcode", "long"),
          SerializerTestFixture.createFieldValueMapping("addressesMap[:].id[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("addressesMap[:].zipcode", "long"),
          SerializerTestFixture.createFieldValueMapping("addressesNoDotMap[:].id[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("addressesNoDotMap[:].zipcode", "long"),
          FieldValueMapping.builder().fieldName("phones[]").fieldType("enum").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                           .isAncestorRequired(true).build())
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLEX = new Pair<>(
      new FileHelper().getFile("/proto-files/complexTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("phone_types[].phone", "long"),
          SerializerTestFixture.createFieldValueMapping("phone_types[].principal", "boolean"),
          SerializerTestFixture.createFieldValueMapping("name", "string"),
          SerializerTestFixture.createFieldValueMapping("age", "int"),
          SerializerTestFixture.createFieldValueMapping("address[].street[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("address[].number_street", "int"),
          SerializerTestFixture.createFieldValueMapping("pets[:].pet_name", "string"),
          SerializerTestFixture.createFieldValueMapping("pets[:].pet_age", "int"),
          SerializerTestFixture.createFieldValueMapping("pets[:].owner", "string"),
          SerializerTestFixture.createFieldValueMapping("descriptors[:]", "string-map"),
          SerializerTestFixture.createFieldValueMapping("dates[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("response", "string"),
          SerializerTestFixture.createFieldValueMapping("presents[:].options[]", "string-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_DATE_TIME = new Pair<>(
      new FileHelper().getFile("/proto-files/dateTimeTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("incident_date", ".google.type.Date"),
          SerializerTestFixture.createFieldValueMapping("incident_time", ".google.type.TimeOfDay"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_DEVE = new Pair<>(
      new FileHelper().getFile("/proto-files/deveTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("load_type", "string"),
          FieldValueMapping.builder().fieldName("shipment.carrier_identifier.type").fieldType("enum").valueLength(0)
                           .fieldValueList("[CARRIER_IDENTIFIER_TYPE_UNSPECIFIED, CARRIER_IDENTIFIER_TYPE_DOT_NUMBER]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("shipment.carrier_identifier.value", "string"),
          FieldValueMapping.builder().fieldName("shipment.shipment_identifiers[].type").fieldType("enum").valueLength(0)
                           .fieldValueList("[SHIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, SHIPMENT_IDENTIFIER_TYPE_BILL_OF_LADING, SHIPMENT_IDENTIFIER_TYPE_ORDER]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("shipment.shipment_identifiers[].value", "string"),
          FieldValueMapping.builder().fieldName("shipment.equipment_identifiers[].type").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[EQUIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, EQUIPMENT_IDENTIFIER_TYPE_MOBILE_PHONE_NUMBER, EQUIPMENT_IDENTIFIER_TYPE_VEHICLE_ID, "
                               + "EQUIPMENT_IDENTIFIER_TYPE_LICENSE_PLATE, EQUIPMENT_IDENTIFIER_TYPE_SENSITECH_DEVICE_ID, EQUIPMENT_IDENTIFIER_TYPE_EMERSON_DEVICE_ID, "
                               + "EQUIPMENT_IDENTIFIER_TYPE_TIVE_DEVICE_ID, EQUIPMENT_IDENTIFIER_TYPE_CONTAINER_ID]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("shipment.equipment_identifiers[].value", "string"),
          SerializerTestFixture.createFieldValueMapping("shipment.attributes[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.timestamp", ".google.protobuf.Timestamp"),
          FieldValueMapping.builder().fieldName("latest_status_update.status_code").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[STATUS_UPDATE_CODE_UNSPECIFIED, STATUS_UPDATE_CODE_DISPATCHED, STATUS_UPDATE_CODE_IN_TRANSIT, STATUS_UPDATE_CODE_AT_STOP, "
                               + "STATUS_UPDATE_CODE_COMPLETED, STATUS_UPDATE_CODE_TRACKING_FAILED, STATUS_UPDATE_CODE_INFO, STATUS_UPDATE_CODE_DELETED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("latest_status_update.status_reason").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[STATUS_UPDATE_REASON_UNSPECIFIED, STATUS_UPDATE_REASON_PENDING_TRACKING_METHOD, STATUS_UPDATE_REASON_SCHEDULED, "
                               + "STATUS_UPDATE_REASON_PENDING_APPROVAL, STATUS_UPDATE_REASON_ACQUIRING_LOCATION, STATUS_UPDATE_REASON_PENDING_CARRIER, "
                               + "STATUS_UPDATE_REASON_IN_MOTION, STATUS_UPDATE_REASON_IDLE, STATUS_UPDATE_REASON_APPROVAL_DENIED, STATUS_UPDATE_REASON_TIMED_OUT, "
                               + "STATUS_UPDATE_REASON_CANCELED, STATUS_UPDATE_REASON_DEPARTED_FINAL_STOP, STATUS_UPDATE_REASON_ARRIVED_FINAL_STOP, "
                               + "STATUS_UPDATE_REASON_ARRIVED_FAILED_TO_ACQUIRE_LOCATION, STATUS_UPDATE_REASON_INFO]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.geo_coordinates.latitude", "double"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.geo_coordinates.longitude", "double"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.address.postal_code", "string"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.address.address_lines[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.address.city", "string"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.address.state", "string"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.address.country", "string"),
          SerializerTestFixture.createFieldValueMapping("latest_status_update.stop_number", "int"),
          SerializerTestFixture.createFieldValueMapping("latest_stop_statuses[].stop_number", "int"),
          FieldValueMapping.builder().fieldName("latest_stop_statuses[].status_code").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[STOP_STATUS_CODE_UNSPECIFIED, STOP_STATUS_CODE_UNKNOWN, STOP_STATUS_CODE_EN_ROUTE, STOP_STATUS_CODE_ARRIVED, STOP_STATUS_CODE_DEPARTED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.start_date_time", "google.protobuf.Timestamp"),
          SerializerTestFixture.createFieldValueMapping("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.end_date_time", "google.protobuf.Timestamp"),
          SerializerTestFixture.createFieldValueMapping("latest_stop_statuses[].arrival_estimate.last_calculated_date_time", "google.protobuf.Timestamp"),
          FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_code").fieldType("enum").valueLength(0)
                           .fieldValueList("[ARRIVAL_CODE_UNSPECIFIED, ARRIVAL_CODE_UNKNOWN, ARRIVAL_CODE_EARLY, ARRIVAL_CODE_ON_TIME, ARRIVAL_CODE_LATE]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("latest_stop_statuses[].additional_appointment_window_statuses[]", "string-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_EASY = new Pair<>(
      new FileHelper().getFile("/proto-files/easyTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("street", "string"),
          SerializerTestFixture.createFieldValueMapping("number[]", "int-array"),
          SerializerTestFixture.createFieldValueMapping("zipcode", "long"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_EMBEDDED_TYPE = new Pair<>(
      new FileHelper().getFile("/proto-files/embeddedTypeTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("phones.addressesPhone[:].id[]", "string-array"),
          FieldValueMapping.builder().fieldName("phones.phoneType").fieldType("enum").valueLength(0)
                           .fieldValueList("[MOBILE, HOME, WORK]").required(true)
                           .isAncestorRequired(true).build())
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ENUM = new Pair<>(
      new FileHelper().getFile("/proto-files/enumTest.proto"),
      Arrays.asList(
          FieldValueMapping.builder().fieldName("phoneTypes").fieldType("enum").valueLength(0)
                           .fieldValueList("[MOBILE, HOME, WORK]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("phoneTypesArray[]").fieldType("enum-array").valueLength(0)
                           .fieldValueList("[MOBILE, HOME, WORK]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("phoneTypesMap[:]").fieldType("enum-map").valueLength(0)
                           .fieldValueList("[MOBILE, HOME, WORK]").required(true)
                           .isAncestorRequired(true).build())
  );

  static final Pair<File, List<FieldValueMapping>> TEST_GOOGLE_TYPES = new Pair<>(
      new FileHelper().getFile("/proto-files/googleTypesTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("id", "Int32Value"),
          SerializerTestFixture.createFieldValueMapping("occurrence_id", "StringValue"),
          SerializerTestFixture.createFieldValueMapping("load_number", "StringValue"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ISSUE_311 = new Pair<>(
      new FileHelper().getFile("/proto-files/issue311Test.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("order_id", "int"),
          SerializerTestFixture.createFieldValueMapping("order_number", "string"),
          SerializerTestFixture.createFieldValueMapping("customer_account.billing_party.party_id", "string"),
          SerializerTestFixture.createFieldValueMapping("customer_account.billing_party.address.address_line_one", "string"),
          SerializerTestFixture.createFieldValueMapping("customer_account.billing_party.address.address_line_two", "string"),
          SerializerTestFixture.createFieldValueMapping("customer_account.billing_party.party_contact[].contact_id", "int"),
          SerializerTestFixture.createFieldValueMapping("customer_account.billing_party.party_contact[].contact_name", "string"),
          SerializerTestFixture.createFieldValueMapping("details.in_details.customs_details.party.party_address.address_line_one", "string"),
          SerializerTestFixture.createFieldValueMapping("details.in_details.customs_details.party.party_address.address_line_two", "string"),
          SerializerTestFixture.createFieldValueMapping("details.in_details.customs_details.party.party_contact[].contact_id", "int"),
          SerializerTestFixture.createFieldValueMapping("details.in_details.customs_details.party.party_contact[].contact_name", "string"),
          SerializerTestFixture.createFieldValueMapping("details.in_details.customs_details.party.p_contact[].contact_id", "int"),
          SerializerTestFixture.createFieldValueMapping("details.in_details.customs_details.party.p_contact[].contact_name", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MAP = new Pair<>(
      new FileHelper().getFile("/proto-files/mapTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("name[:]", "string-map"),
          SerializerTestFixture.createFieldValueMapping("addresses[:].street", "string"),
          SerializerTestFixture.createFieldValueMapping("addresses[:].number", "int"),
          SerializerTestFixture.createFieldValueMapping("addresses[:].zipcode", "int"),
          SerializerTestFixture.createFieldValueMapping("addressesNoDot[:].street", "string"),
          SerializerTestFixture.createFieldValueMapping("addressesNoDot[:].number", "int"),
          SerializerTestFixture.createFieldValueMapping("addressesNoDot[:].zipcode", "int"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ONE_OF = new Pair<>(
      new FileHelper().getFile("/proto-files/oneOfTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("type.street", "string"),
          SerializerTestFixture.createFieldValueMapping("type.number", "int"),
          SerializerTestFixture.createFieldValueMapping("type.test", "string"),
          SerializerTestFixture.createFieldValueMapping("optionString", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_PROVIDED = new Pair<>(
      new FileHelper().getFile("/proto-files/providedTest.proto"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("id", "int"),
          SerializerTestFixture.createFieldValueMapping("occurrence_id", "string"),
          SerializerTestFixture.createFieldValueMapping("load_number", "string"),
          SerializerTestFixture.createFieldValueMapping("claim_type.code", "string"),
          SerializerTestFixture.createFieldValueMapping("claim_type.description", "string"),
          SerializerTestFixture.createFieldValueMapping("collision_type.code", "string"),
          SerializerTestFixture.createFieldValueMapping("collision_type.description", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_cause_type.code", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_cause_type.description", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_type.code", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_type.description", "string"),
          SerializerTestFixture.createFieldValueMapping("review_status_type.code", "string"),
          SerializerTestFixture.createFieldValueMapping("review_status_type.description", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_latitude", "double"),
          SerializerTestFixture.createFieldValueMapping("incident_longitude", "double"),
          SerializerTestFixture.createFieldValueMapping("incident_date", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_time", "google.protobuf.Timestamp"),
          SerializerTestFixture.createFieldValueMapping("incident_city", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_state", "string"),
          SerializerTestFixture.createFieldValueMapping("location_description", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_equipment_details[].equipment_number", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_equipment_details[].equipment_type", "string"),
          SerializerTestFixture.createFieldValueMapping("incident_equipment_details[].equipment_prefix", "string"),
          SerializerTestFixture.createFieldValueMapping("driver.driver_id", "int"),
          SerializerTestFixture.createFieldValueMapping("driver.driver_first_name", "string"),
          SerializerTestFixture.createFieldValueMapping("driver.driver_last_name", "string"),
          SerializerTestFixture.createFieldValueMapping("dot_accident_indicator", "string"),
          SerializerTestFixture.createFieldValueMapping("drug_test_required_indicator", "string"),
          SerializerTestFixture.createFieldValueMapping("hazardous_material_indicator", "string"),
          SerializerTestFixture.createFieldValueMapping("preventable_indicator", "string"),
          SerializerTestFixture.createFieldValueMapping("report_by_name", "string"),
          SerializerTestFixture.createFieldValueMapping("create_user_id", "string"))
  );

  protected ProtobufSerializerTestFixture() {
  }
}
