package com.sngular.kloadgen.serializer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.commons.math3.util.Pair;

final class JsonSerializerTestFixture {

  static final Pair<File, List<FieldValueMapping>> TEST_BASIC = new Pair<>(
      new FileHelper().getFile("/jsonschema/basic.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("firstName", "string"),
          SerializerTestFixture.createFieldValueMapping("lastName", "string"),
          SerializerTestFixture.createFieldValueMapping("age", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_BASIC_ARRAY = new Pair<>(
      new FileHelper().getFile("/jsonschema/basic-array.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("fruits[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("vegetables[].veggieName", "string"),
          SerializerTestFixture.createFieldValueMapping("vegetables[].veggieLike", "boolean"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_BASIC_NUMBER = new Pair<>(
      new FileHelper().getFile("/jsonschema/basic-number.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("latitude", "number"),
          SerializerTestFixture.createFieldValueMapping("longitude", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COLLECTIONS = new Pair<>(
      new FileHelper().getFile("/jsonschema/collections.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("mapOfStrings[:]", "string-map"),
          SerializerTestFixture.createFieldValueMapping("arrayOfObjectsOfBasicTypes[].stringOfObject", "string"),
          SerializerTestFixture.createFieldValueMapping("arrayOfObjectsOfBasicTypes[].numberOfObject", "number"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfBasicTypes.arrayOfStrings[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfBasicTypes.mapOfIntegers[:]", "number-map"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfBasicTypes.stringControl", "string"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfObject.stringControl", "string"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].namePerson", "string"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].phonePerson", "number"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].nameDog", "string"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.dogId", "number"),
          SerializerTestFixture.createFieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.breedName", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLEX_DEFINITIONS = new Pair<>(
      new FileHelper().getFile("/jsonschema/complex-definitions.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("objectOfDefinitions.stringControl", "string"),
          SerializerTestFixture.createFieldValueMapping("objectOfDefinitions.arrayOfStrings[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("objectOfDefinitions.mapOfStrings[:]", "string-map"),
          SerializerTestFixture.createFieldValueMapping("arrayOfObjects[].stringOfObject", "string"),
          SerializerTestFixture.createFieldValueMapping("arrayOfObjects[].numberOfObject", "number"),
          SerializerTestFixture.createFieldValueMapping("mapOfObjects[:].arrayOfInternalObject[]", "string-array"),
          SerializerTestFixture.createFieldValueMapping("mapOfMaps[:][:].stringControlObject", "string"),
          SerializerTestFixture.createFieldValueMapping("mapOfMaps[:][:].arrayOfArraysOfStrings[][]", "string-array-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLEX_DOCUMENT = new Pair<>(
      new FileHelper().getFile("/jsonschema/complex-document.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("_id", "string"),
          SerializerTestFixture.createFieldValueMapping("userId", "number"),
          SerializerTestFixture.createFieldValueMapping("storeId", "number"),
          SerializerTestFixture.createFieldValueMapping("snapshotId", "string"),
          SerializerTestFixture.createFieldValueMapping("addressId", "string"),
          SerializerTestFixture.createFieldValueMapping("addressLine", "string"),
          SerializerTestFixture.createFieldValueMapping("alias", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.email", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.firstName", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.middleName", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.lastName", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.honorific", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.phones[].prefix", "string"),
          SerializerTestFixture.createFieldValueMapping("contactInformation.phones[].number", "string"),
          SerializerTestFixture.createFieldValueMapping("countryCode", "string"),
          SerializerTestFixture.createFieldValueMapping("location.streetName", "string"),
          SerializerTestFixture.createFieldValueMapping("location.streetNumber", "string"),
          SerializerTestFixture.createFieldValueMapping("location.floor", "string"),
          SerializerTestFixture.createFieldValueMapping("location.door", "string"),
          SerializerTestFixture.createFieldValueMapping("location.doorCode", "string"),
          SerializerTestFixture.createFieldValueMapping("location.zipCode", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level1.code", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level1.freeForm", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level2.code", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level2.freeForm", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level3.code", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level3.freeForm", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level4.code", "string"),
          SerializerTestFixture.createFieldValueMapping("geopoliticalSubdivisions.level4.freeForm", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.createdAt", "timestamp"),
          SerializerTestFixture.createFieldValueMapping("_metadata.createdBy", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.lastUpdatedAt", "timestamp"),
          SerializerTestFixture.createFieldValueMapping("_metadata.lastUpdatedBy", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.deletedAt", "timestamp"),
          SerializerTestFixture.createFieldValueMapping("_metadata.projectVersion", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.projectName", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.deletedBy", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.schema", "number"),
          FieldValueMapping.builder().fieldName("_entity").fieldType("enum").valueLength(0).fieldValueList("[AddressSnapshot]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("_class").fieldType("enum").valueLength(0).fieldValueList("[AddressSnapshot]").required(true)
                           .isAncestorRequired(true).build())
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MEDIUM_DOCUMENT = new Pair<>(
      new FileHelper().getFile("/jsonschema/medium-document.jcs"),
      Arrays.asList(
          FieldValueMapping.builder().fieldName("_class").fieldType("enum").valueLength(0).fieldValueList("[OrderPublicDetailDocument]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("_entity").fieldType("enum").valueLength(0).fieldValueList("[OrderPublicDetailDocument]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("_metadata.createdAt", "number"),
          SerializerTestFixture.createFieldValueMapping("_metadata.createdBy", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.deletedAt", "number"),
          SerializerTestFixture.createFieldValueMapping("_metadata.deletedBy", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.lastUpdatedAt", "number"),
          SerializerTestFixture.createFieldValueMapping("_metadata.lastUpdatedBy", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.projectName", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.projectVersion", "string"),
          SerializerTestFixture.createFieldValueMapping("_metadata.schema", "number"),
          SerializerTestFixture.createFieldValueMapping("orderId", "string"),
          SerializerTestFixture.createFieldValueMapping("storeId", "number"),
          SerializerTestFixture.createFieldValueMapping("paymentId", "string"),
          SerializerTestFixture.createFieldValueMapping("parentId", "string"),
          SerializerTestFixture.createFieldValueMapping("externalId", "string"),
          SerializerTestFixture.createFieldValueMapping("editorId", "number"),
          FieldValueMapping.builder().fieldName("type").fieldType("enum").valueLength(0)
                           .fieldValueList("[REGULAR, REPLACEMENT, EXCHANGE, REPOSITION, ONLINE_EXCHANGE, STORE_EXCHANGE]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("status").fieldType("enum").valueLength(0).fieldValueList("[RECEIVED, VALIDATED, PREPARING, SHIPPED, CLOSED, CANCELLED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("fraudStatus").fieldType("enum").valueLength(0).fieldValueList("[NOT_APPLIED, PENDING, ACCEPTED, REQUIRED_REVIEW, REJECTED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("stockStatus").fieldType("enum").valueLength(0).fieldValueList("[UNKNOWN, NOT_APPLIED, SYNCHRONIZED, SKIPPED]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("paymentStatus").fieldType("enum").valueLength(0).fieldValueList("[NOT_APPLIED, UNKNOWN, REJECTED, ACCEPTED]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("paymentSystem").fieldType("enum").valueLength(0).fieldValueList("[UNKNOWN, ALPHA, WCS]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("manualReviewStatus").fieldType("enum").valueLength(0).fieldValueList("[NA, PENDING, ACCEPTED, REFUSED]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("customerId", "number"),
          FieldValueMapping.builder().fieldName("customerType").fieldType("enum").valueLength(0).fieldValueList("[REGISTERED, GUEST]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("guestAction").fieldType("enum").valueLength(0).fieldValueList("[NO_ACTION, CONVERSION, LINK_ACCOUNT]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("locale", "string"),
          SerializerTestFixture.createFieldValueMapping("preorder", "boolean"),
          SerializerTestFixture.createFieldValueMapping("fastSint", "boolean"),
          SerializerTestFixture.createFieldValueMapping("pvpTaxesIncluded", "boolean"),
          SerializerTestFixture.createFieldValueMapping("placedDatetime", "string"),
          SerializerTestFixture.createFieldValueMapping("cancelledDatetime", "string"),
          SerializerTestFixture.createFieldValueMapping("lastUpdateDatetime", "string"),
          SerializerTestFixture.createFieldValueMapping("snapshotDatetime", "string"),
          SerializerTestFixture.createFieldValueMapping("duty.amount.value", "number"),
          SerializerTestFixture.createFieldValueMapping("duty.amount.currency", "string"),
          SerializerTestFixture.createFieldValueMapping("duty.amount.exponent", "number"),
          SerializerTestFixture.createFieldValueMapping("duty.confirmedAmount.value", "number"),
          SerializerTestFixture.createFieldValueMapping("duty.confirmedAmount.currency", "string"),
          SerializerTestFixture.createFieldValueMapping("duty.confirmedAmount.exponent", "number"),
          FieldValueMapping.builder().fieldName("billing.status").fieldType("enum").valueLength(0).fieldValueList("[NOT_APPLIED, PENDING, BILLED]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("billing.addressId", "string"),
          SerializerTestFixture.createFieldValueMapping("origin.systemCode", "string"),
          FieldValueMapping.builder().fieldName("origin.systemType").fieldType("enum").valueLength(0).fieldValueList("[CHECKOUT, MPS, BACKOFFICE, STORE]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("origin.systemUser", "string"),
          SerializerTestFixture.createFieldValueMapping("origin.systemDeviceId", "string"),
          SerializerTestFixture.createFieldValueMapping("origin.deviceType", "string"),
          SerializerTestFixture.createFieldValueMapping("origin.deviceUserAgent", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].id", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].partNumber", "string"),
          FieldValueMapping.builder().fieldName("orderItems[].type").fieldType("enum").valueLength(0)
                           .fieldValueList("[PRODUCT, VIRTUAL_GIFT_CARD, PHYSICAL_GIFT_CARD, SHIPPING_COST, SERVICE]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("orderItems[].status").fieldType("enum").valueLength(0).fieldValueList("[RECEIVED, PREPARING, SHIPPED, CLOSED, CANCELLED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("orderItems[].statusReason", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].statusDatetime", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].pvpAmount.value", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].pvpAmount.currency", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].pvpAmount.exponent", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].taxesAmount.value", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].taxesAmount.currency", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].taxesAmount.exponent", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].taxesPercentage", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].initialMinimumDate", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].initialMaximumDate", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].adjustment.value", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].adjustment.currency", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].adjustment.exponent", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].product.catalogEntryId", "number"),
          FieldValueMapping.builder().fieldName("orderItems[].product.stockMode").fieldType("enum").valueLength(0)
                           .fieldValueList("[UNKNOWN, NORMAL, TRANSIT_PRESALE, VIRTUALSTOCK_PRESALE, SOD_PRESALE, PREORDER]").required(true)
                           .isAncestorRequired(true).build(),
          SerializerTestFixture.createFieldValueMapping("orderItems[].virtualGiftCard.catalogEntryId", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].virtualGiftCard.senderName", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].virtualGiftCard.message", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].virtualGiftCard.style", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].virtualGiftCard.receiverName", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].virtualGiftCard.deliveryDate", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].physicalGiftCard.catalogEntryId", "number"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].physicalGiftCard.senderName", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].physicalGiftCard.message", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].physicalGiftCard.receiverPhonePrefix", "string"),
          SerializerTestFixture.createFieldValueMapping("orderItems[].physicalGiftCard.receiverPhoneNumber", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MULTIPLE_TYPE = new Pair<>(
      new FileHelper().getFile("/jsonschema/multiple-type.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("id", "number"),
          SerializerTestFixture.createFieldValueMapping("version", "number"),
          SerializerTestFixture.createFieldValueMapping("dtype", "string"),
          SerializerTestFixture.createFieldValueMapping("timestamp", "string"),
          SerializerTestFixture.createFieldValueMapping("event_type", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MULTIPLE_TYPE_SINGLE = new Pair<>(
      new FileHelper().getFile("/jsonschema/multiple-type-single.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("id", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_NESTED_COLLECTIONS = new Pair<>(
      new FileHelper().getFile("/jsonschema/nested-collections.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("arrayOfMapsOfObjects[][:].stringObject", "string"),
          SerializerTestFixture.createFieldValueMapping("arrayOfMapsOfObjects[][:].numberObject", "number"),
          SerializerTestFixture.createFieldValueMapping("arrayOfArraysOfStrings[][]", "string-array-array"),
          SerializerTestFixture.createFieldValueMapping("mapOfArraysOfStrings[:][]", "string-array-map"),
          SerializerTestFixture.createFieldValueMapping("mapOfMapsOfObjects[:][:].name4Object", "string"),
          SerializerTestFixture.createFieldValueMapping("mapOfMapsOfObjects[:][:].number4Object", "number"),
          SerializerTestFixture.createFieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].stringControl", "string"),
          SerializerTestFixture.createFieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].numberControl", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MAP = new Pair<>(
      new FileHelper().getFile("/jsonschema/test-map.jcs"),
      Arrays.asList(
          SerializerTestFixture.createFieldValueMapping("firstName", "string"),
          SerializerTestFixture.createFieldValueMapping("lastName", "string"),
          SerializerTestFixture.createFieldValueMapping("age", "number"),
          SerializerTestFixture.createFieldValueMapping("testMap.itemType[:]", "number-map"),
          SerializerTestFixture.createFieldValueMapping("testMap.itemTipo[:]", "string-map"))
  );

  private JsonSerializerTestFixture() {
  }
}
