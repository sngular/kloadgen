package com.sngular.kloadgen.serializer;

import static com.sngular.kloadgen.serializer.SerializerTestFixture.createFieldValueMapping;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.commons.math3.util.Pair;

public class ProtobuffSerializerTestFixture {

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLETE_PROTO = new Pair<>(
      new FileHelper().getFile("/proto-files/completeProto.proto"),
      Arrays.asList(
          createFieldValueMapping("name", "string"),
          createFieldValueMapping("id", "int"),
          createFieldValueMapping("addressesArray[].id[]", "string-array"),
          createFieldValueMapping("addressesArray[].zipcode", "long"),
          createFieldValueMapping("addressesDot[].id[]", "string-array"),
          createFieldValueMapping("addressesDot[].zipcode", "long"),
          createFieldValueMapping("addressesMap[:].id[]", "string-array"),
          createFieldValueMapping("addressesMap[:].zipcode", "long"),
          createFieldValueMapping("addressesNoDotMap[:].id[]", "string-array"),
          createFieldValueMapping("addressesNoDotMap[:].zipcode", "long"),
          FieldValueMapping.builder().fieldName("phones[]").fieldType("enum").valueLength(0).fieldValueList("[MOBILE, HOME, WORK]").required(true)
                           .isAncestorRequired(true).build())
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLEX = new Pair<>(
      new FileHelper().getFile("/proto-files/complexTest.proto"),
      Arrays.asList(
          createFieldValueMapping("phone_types[].phone", "long"),
          createFieldValueMapping("phone_types[].principal", "boolean"),
          createFieldValueMapping("name", "string"),
          createFieldValueMapping("age", "int"),
          createFieldValueMapping("address[].street[]", "string-array"),
          createFieldValueMapping("address[].number_street", "int"),
          createFieldValueMapping("pets[:].pet_name", "string"),
          createFieldValueMapping("pets[:].pet_age", "int"),
          createFieldValueMapping("pets[:].owner", "string"),
          createFieldValueMapping("descriptors[:]", "string-map"),
          createFieldValueMapping("dates[]", "string-array"),
          createFieldValueMapping("response", "string"),
          createFieldValueMapping("presents[:].options[]", "string-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_DATE_TIME = new Pair<>(
      new FileHelper().getFile("/proto-files/dateTimeTest.proto"),
      Arrays.asList(
          createFieldValueMapping("incident_date", ".google.type.Date"),
          createFieldValueMapping("incident_time", ".google.type.TimeOfDay"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_DEVE = new Pair<>(
      new FileHelper().getFile("/proto-files/deveTest.proto"),
      Arrays.asList(
          createFieldValueMapping("load_type", "string"),
          FieldValueMapping.builder().fieldName("shipment.carrier_identifier.type").fieldType("enum").valueLength(0)
                           .fieldValueList("[CARRIER_IDENTIFIER_TYPE_UNSPECIFIED, CARRIER_IDENTIFIER_TYPE_DOT_NUMBER]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("shipment.carrier_identifier.value", "string"),
          FieldValueMapping.builder().fieldName("shipment.shipment_identifiers[].type").fieldType("enum").valueLength(0)
                           .fieldValueList("[SHIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, SHIPMENT_IDENTIFIER_TYPE_BILL_OF_LADING, SHIPMENT_IDENTIFIER_TYPE_ORDER]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("shipment.shipment_identifiers[].value", "string"),
          FieldValueMapping.builder().fieldName("shipment.equipment_identifiers[].type").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[EQUIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, EQUIPMENT_IDENTIFIER_TYPE_MOBILE_PHONE_NUMBER, EQUIPMENT_IDENTIFIER_TYPE_VEHICLE_ID, " +
                               "EQUIPMENT_IDENTIFIER_TYPE_LICENSE_PLATE, EQUIPMENT_IDENTIFIER_TYPE_SENSITECH_DEVICE_ID, EQUIPMENT_IDENTIFIER_TYPE_EMERSON_DEVICE_ID, " +
                               "EQUIPMENT_IDENTIFIER_TYPE_TIVE_DEVICE_ID, EQUIPMENT_IDENTIFIER_TYPE_CONTAINER_ID]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("shipment.equipment_identifiers[].value", "string"),
          createFieldValueMapping("shipment.attributes[]", "string-array"),
          createFieldValueMapping("latest_status_update.timestamp", ".google.protobuf.Timestamp"),
          FieldValueMapping.builder().fieldName("latest_status_update.status_code").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[STATUS_UPDATE_CODE_UNSPECIFIED, STATUS_UPDATE_CODE_DISPATCHED, STATUS_UPDATE_CODE_IN_TRANSIT, STATUS_UPDATE_CODE_AT_STOP, " +
                               "STATUS_UPDATE_CODE_COMPLETED, STATUS_UPDATE_CODE_TRACKING_FAILED, STATUS_UPDATE_CODE_INFO, STATUS_UPDATE_CODE_DELETED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("latest_status_update.status_reason").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[STATUS_UPDATE_REASON_UNSPECIFIED, STATUS_UPDATE_REASON_PENDING_TRACKING_METHOD, STATUS_UPDATE_REASON_SCHEDULED, " +
                               "STATUS_UPDATE_REASON_PENDING_APPROVAL, STATUS_UPDATE_REASON_ACQUIRING_LOCATION, STATUS_UPDATE_REASON_PENDING_CARRIER, " +
                               "STATUS_UPDATE_REASON_IN_MOTION, STATUS_UPDATE_REASON_IDLE, STATUS_UPDATE_REASON_APPROVAL_DENIED, STATUS_UPDATE_REASON_TIMED_OUT, " +
                               "STATUS_UPDATE_REASON_CANCELED, STATUS_UPDATE_REASON_DEPARTED_FINAL_STOP, STATUS_UPDATE_REASON_ARRIVED_FINAL_STOP, " +
                               "STATUS_UPDATE_REASON_ARRIVED_FAILED_TO_ACQUIRE_LOCATION, STATUS_UPDATE_REASON_INFO]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("latest_status_update.geo_coordinates.latitude", "double"),
          createFieldValueMapping("latest_status_update.geo_coordinates.longitude", "double"),
          createFieldValueMapping("latest_status_update.address.postal_code", "string"),
          createFieldValueMapping("latest_status_update.address.address_lines[]", "string-array"),
          createFieldValueMapping("latest_status_update.address.city", "string"),
          createFieldValueMapping("latest_status_update.address.state", "string"),
          createFieldValueMapping("latest_status_update.address.country", "string"),
          createFieldValueMapping("latest_status_update.stop_number", "int"),
          createFieldValueMapping("latest_stop_statuses[].stop_number", "int"),
          FieldValueMapping.builder().fieldName("latest_stop_statuses[].status_code").fieldType("enum").valueLength(0)
                           .fieldValueList(
                               "[STOP_STATUS_CODE_UNSPECIFIED, STOP_STATUS_CODE_UNKNOWN, STOP_STATUS_CODE_EN_ROUTE, STOP_STATUS_CODE_ARRIVED, STOP_STATUS_CODE_DEPARTED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.start_date_time", "google.protobuf.Timestamp"),
          createFieldValueMapping("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.end_date_time", "google.protobuf.Timestamp"),
          createFieldValueMapping("latest_stop_statuses[].arrival_estimate.last_calculated_date_time", "google.protobuf.Timestamp"),
          FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_code").fieldType("enum").valueLength(0)
                           .fieldValueList("[ARRIVAL_CODE_UNSPECIFIED, ARRIVAL_CODE_UNKNOWN, ARRIVAL_CODE_EARLY, ARRIVAL_CODE_ON_TIME, ARRIVAL_CODE_LATE]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("latest_stop_statuses[].additional_appointment_window_statuses[]", "string-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_EASY = new Pair<>(
      new FileHelper().getFile("/proto-files/easyTest.proto"),
      Arrays.asList(
          createFieldValueMapping("street", "string"),
          createFieldValueMapping("number[]", "int-array"),
          createFieldValueMapping("zipcode", "long"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_EMBEDDED_TYPE = new Pair<>(
      new FileHelper().getFile("/proto-files/embeddedTypeTest.proto"),
      Arrays.asList(
          createFieldValueMapping("phones.addressesPhone[:].id[]", "string-array"),
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
          createFieldValueMapping("id", "Int32Value"),
          createFieldValueMapping("occurrence_id", "StringValue"),
          createFieldValueMapping("load_number", "StringValue"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ISSUE_311 = new Pair<>(
      new FileHelper().getFile("/proto-files/issue311Test.proto"),
      Arrays.asList(
          createFieldValueMapping("order_id", "int"),
          createFieldValueMapping("order_number", "string"),
          createFieldValueMapping("customer_account.billing_party.party_id", "string"),
          createFieldValueMapping("customer_account.billing_party.address.address_line_one", "string"),
          createFieldValueMapping("customer_account.billing_party.address.address_line_two", "string"),
          createFieldValueMapping("customer_account.billing_party.party_contact[].contact_id", "int"),
          createFieldValueMapping("customer_account.billing_party.party_contact[].contact_name", "string"),
          createFieldValueMapping("details.in_details.customs_details.party.party_address.address_line_one", "string"),
          createFieldValueMapping("details.in_details.customs_details.party.party_address.address_line_two", "string"),
          createFieldValueMapping("details.in_details.customs_details.party.party_contact[].contact_id", "int"),
          createFieldValueMapping("details.in_details.customs_details.party.party_contact[].contact_name", "string"),
          createFieldValueMapping("details.in_details.customs_details.party.p_contact[].contact_id", "int"),
          createFieldValueMapping("details.in_details.customs_details.party.p_contact[].contact_name", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MAP = new Pair<>(
      new FileHelper().getFile("/proto-files/mapTest.proto"),
      Arrays.asList(
          createFieldValueMapping("name[:]", "string-map"),
          createFieldValueMapping("addresses[:].street", "string"),
          createFieldValueMapping("addresses[:].number", "int"),
          createFieldValueMapping("addresses[:].zipcode", "int"),
          createFieldValueMapping("addressesNoDot[:].street", "string"),
          createFieldValueMapping("addressesNoDot[:].number", "int"),
          createFieldValueMapping("addressesNoDot[:].zipcode", "int"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_ONE_OF = new Pair<>(
      new FileHelper().getFile("/proto-files/oneOfTest.proto"),
      Arrays.asList(
          createFieldValueMapping("type.street", "string"),
          createFieldValueMapping("type.number", "int"),
          createFieldValueMapping("type.test", "string"),
          createFieldValueMapping("optionString", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_PROVIDED = new Pair<>(
      new FileHelper().getFile("/proto-files/providedTest.proto"),
      Arrays.asList(
          createFieldValueMapping("id", "int"),
          createFieldValueMapping("occurrence_id", "string"),
          createFieldValueMapping("load_number", "string"),
          createFieldValueMapping("claim_type.code", "string"),
          createFieldValueMapping("claim_type.description", "string"),
          createFieldValueMapping("collision_type.code", "string"),
          createFieldValueMapping("collision_type.description", "string"),
          createFieldValueMapping("incident_cause_type.code", "string"),
          createFieldValueMapping("incident_cause_type.description", "string"),
          createFieldValueMapping("incident_type.code", "string"),
          createFieldValueMapping("incident_type.description", "string"),
          createFieldValueMapping("review_status_type.code", "string"),
          createFieldValueMapping("review_status_type.description", "string"),
          createFieldValueMapping("incident_latitude", "double"),
          createFieldValueMapping("incident_longitude", "double"),
          createFieldValueMapping("incident_date", "string"),
          createFieldValueMapping("incident_time", "google.protobuf.Timestamp"),
          createFieldValueMapping("incident_city", "string"),
          createFieldValueMapping("incident_state", "string"),
          createFieldValueMapping("location_description", "string"),
          createFieldValueMapping("incident_equipment_details[].equipment_number", "string"),
          createFieldValueMapping("incident_equipment_details[].equipment_type", "string"),
          createFieldValueMapping("incident_equipment_details[].equipment_prefix", "string"),
          createFieldValueMapping("driver.driver_id", "int"),
          createFieldValueMapping("driver.driver_first_name", "string"),
          createFieldValueMapping("driver.driver_last_name", "string"),
          createFieldValueMapping("dot_accident_indicator", "string"),
          createFieldValueMapping("drug_test_required_indicator", "string"),
          createFieldValueMapping("hazardous_material_indicator", "string"),
          createFieldValueMapping("preventable_indicator", "string"),
          createFieldValueMapping("report_by_name", "string"),
          createFieldValueMapping("create_user_id", "string"))
  );
}
