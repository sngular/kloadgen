package com.sngular.kloadgen.serializer;

import static com.sngular.kloadgen.serializer.SerializerTestFixture.createFieldValueMapping;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.commons.math3.util.Pair;

public class JsonSerializerTestFixture {

  static final Pair<File, List<FieldValueMapping>> TEST_BASIC = new Pair<>(
      new FileHelper().getFile("/jsonschema/basic.jcs"),
      Arrays.asList(
          createFieldValueMapping("firstName", "string"),
          createFieldValueMapping("lastName", "string"),
          createFieldValueMapping("age", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_BASIC_ARRAY = new Pair<>(
      new FileHelper().getFile("/jsonschema/basic-array.jcs"),
      Arrays.asList(
          createFieldValueMapping("fruits[]", "string-array"),
          createFieldValueMapping("vegetables[].veggieName", "string"),
          createFieldValueMapping("vegetables[].veggieLike", "boolean"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_BASIC_NUMBER = new Pair<>(
      new FileHelper().getFile("/jsonschema/basic-number.jcs"),
      Arrays.asList(
          createFieldValueMapping("latitude", "number"),
          createFieldValueMapping("longitude", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COLLECTIONS = new Pair<>(
      new FileHelper().getFile("/jsonschema/collections.jcs"),
      Arrays.asList(
          createFieldValueMapping("mapOfStrings[:]", "string-map"),
          createFieldValueMapping("arrayOfObjectsOfBasicTypes[].stringOfObject", "string"),
          createFieldValueMapping("arrayOfObjectsOfBasicTypes[].numberOfObject", "number"),
          createFieldValueMapping("objectOfCollectionsOfBasicTypes.arrayOfStrings[]", "string-array"),
          createFieldValueMapping("objectOfCollectionsOfBasicTypes.mapOfIntegers[:]", "number-map"),
          createFieldValueMapping("objectOfCollectionsOfBasicTypes.stringControl", "string"),
          createFieldValueMapping("objectOfCollectionsOfObject.stringControl", "string"),
          createFieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].namePerson", "string"),
          createFieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].phonePerson", "number"),
          createFieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].nameDog", "string"),
          createFieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.dogId", "number"),
          createFieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.breedName", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLEX_DEFINITIONS = new Pair<>(
      new FileHelper().getFile("/jsonschema/complex-definitions.jcs"),
      Arrays.asList(
          createFieldValueMapping("objectOfDefinitions.stringControl", "string"),
          createFieldValueMapping("objectOfDefinitions.arrayOfStrings[]", "string-array"),
          createFieldValueMapping("objectOfDefinitions.mapOfStrings[:]", "string-map"),
          createFieldValueMapping("arrayOfObjects[].stringOfObject", "string"),
          createFieldValueMapping("arrayOfObjects[].numberOfObject", "number"),
          createFieldValueMapping("mapOfObjects[:].arrayOfInternalObject[]", "string-array"),
          createFieldValueMapping("mapOfMaps[:][:].stringControlObject", "string"),
          createFieldValueMapping("mapOfMaps[:][:].arrayOfArraysOfStrings[][]", "string-array-array"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_COMPLEX_DOCUMENT = new Pair<>(
      new FileHelper().getFile("/jsonschema/complex-document.jcs"),
      Arrays.asList(
          createFieldValueMapping("_id", "string"),
          createFieldValueMapping("userId", "number"),
          createFieldValueMapping("storeId", "number"),
          createFieldValueMapping("snapshotId", "string"),
          createFieldValueMapping("addressId", "string"),
          createFieldValueMapping("addressLine", "string"),
          createFieldValueMapping("alias", "string"),
          createFieldValueMapping("contactInformation.email", "string"),
          createFieldValueMapping("contactInformation.firstName", "string"),
          createFieldValueMapping("contactInformation.middleName", "string"),
          createFieldValueMapping("contactInformation.lastName", "string"),
          createFieldValueMapping("contactInformation.honorific", "string"),
          createFieldValueMapping("contactInformation.phones[].prefix", "string"),
          createFieldValueMapping("contactInformation.phones[].number", "string"),
          createFieldValueMapping("countryCode", "string"),
          createFieldValueMapping("location.streetName", "string"),
          createFieldValueMapping("location.streetNumber", "string"),
          createFieldValueMapping("location.floor", "string"),
          createFieldValueMapping("location.door", "string"),
          createFieldValueMapping("location.doorCode", "string"),
          createFieldValueMapping("location.zipCode", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level1.code", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level1.freeForm", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level2.code", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level2.freeForm", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level3.code", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level3.freeForm", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level4.code", "string"),
          createFieldValueMapping("geopoliticalSubdivisions.level4.freeForm", "string"),
          createFieldValueMapping("_metadata.createdAt", "timestamp"),
          createFieldValueMapping("_metadata.createdBy", "string"),
          createFieldValueMapping("_metadata.lastUpdatedAt", "timestamp"),
          createFieldValueMapping("_metadata.lastUpdatedBy", "string"),
          createFieldValueMapping("_metadata.deletedAt", "timestamp"),
          createFieldValueMapping("_metadata.projectVersion", "string"),
          createFieldValueMapping("_metadata.projectName", "string"),
          createFieldValueMapping("_metadata.deletedBy", "string"),
          createFieldValueMapping("_metadata.schema", "number"),
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
          createFieldValueMapping("_metadata.createdAt", "number"),
          createFieldValueMapping("_metadata.createdBy", "string"),
          createFieldValueMapping("_metadata.deletedAt", "number"),
          createFieldValueMapping("_metadata.deletedBy", "string"),
          createFieldValueMapping("_metadata.lastUpdatedAt", "number"),
          createFieldValueMapping("_metadata.lastUpdatedBy", "string"),
          createFieldValueMapping("_metadata.projectName", "string"),
          createFieldValueMapping("_metadata.projectVersion", "string"),
          createFieldValueMapping("_metadata.schema", "number"),
          createFieldValueMapping("orderId", "string"),
          createFieldValueMapping("storeId", "number"),
          createFieldValueMapping("paymentId", "string"),
          createFieldValueMapping("parentId", "string"),
          createFieldValueMapping("externalId", "string"),
          createFieldValueMapping("editorId", "number"),
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
          createFieldValueMapping("customerId", "number"),
          FieldValueMapping.builder().fieldName("customerType").fieldType("enum").valueLength(0).fieldValueList("[REGISTERED, GUEST]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("guestAction").fieldType("enum").valueLength(0).fieldValueList("[NO_ACTION, CONVERSION, LINK_ACCOUNT]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("locale", "string"),
          createFieldValueMapping("preorder", "boolean"),
          createFieldValueMapping("fastSint", "boolean"),
          createFieldValueMapping("pvpTaxesIncluded", "boolean"),
          createFieldValueMapping("placedDatetime", "string"),
          createFieldValueMapping("cancelledDatetime", "string"),
          createFieldValueMapping("lastUpdateDatetime", "string"),
          createFieldValueMapping("snapshotDatetime", "string"),
          createFieldValueMapping("duty.amount.value", "number"),
          createFieldValueMapping("duty.amount.currency", "string"),
          createFieldValueMapping("duty.amount.exponent", "number"),
          createFieldValueMapping("duty.confirmedAmount.value", "number"),
          createFieldValueMapping("duty.confirmedAmount.currency", "string"),
          createFieldValueMapping("duty.confirmedAmount.exponent", "number"),
          FieldValueMapping.builder().fieldName("billing.status").fieldType("enum").valueLength(0).fieldValueList("[NOT_APPLIED, PENDING, BILLED]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("billing.addressId", "string"),
          createFieldValueMapping("origin.systemCode", "string"),
          FieldValueMapping.builder().fieldName("origin.systemType").fieldType("enum").valueLength(0).fieldValueList("[CHECKOUT, MPS, BACKOFFICE, STORE]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("origin.systemUser", "string"),
          createFieldValueMapping("origin.systemDeviceId", "string"),
          createFieldValueMapping("origin.deviceType", "string"),
          createFieldValueMapping("origin.deviceUserAgent", "string"),
          createFieldValueMapping("orderItems[].id", "string"),
          createFieldValueMapping("orderItems[].partNumber", "string"),
          FieldValueMapping.builder().fieldName("orderItems[].type").fieldType("enum").valueLength(0)
                           .fieldValueList("[PRODUCT, VIRTUAL_GIFT_CARD, PHYSICAL_GIFT_CARD, SHIPPING_COST, SERVICE]").required(true)
                           .isAncestorRequired(true).build(),
          FieldValueMapping.builder().fieldName("orderItems[].status").fieldType("enum").valueLength(0).fieldValueList("[RECEIVED, PREPARING, SHIPPED, CLOSED, CANCELLED]")
                           .required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("orderItems[].statusReason", "string"),
          createFieldValueMapping("orderItems[].statusDatetime", "string"),
          createFieldValueMapping("orderItems[].pvpAmount.value", "number"),
          createFieldValueMapping("orderItems[].pvpAmount.currency", "string"),
          createFieldValueMapping("orderItems[].pvpAmount.exponent", "number"),
          createFieldValueMapping("orderItems[].taxesAmount.value", "number"),
          createFieldValueMapping("orderItems[].taxesAmount.currency", "string"),
          createFieldValueMapping("orderItems[].taxesAmount.exponent", "number"),
          createFieldValueMapping("orderItems[].taxesPercentage", "number"),
          createFieldValueMapping("orderItems[].initialMinimumDate", "string"),
          createFieldValueMapping("orderItems[].initialMaximumDate", "string"),
          createFieldValueMapping("orderItems[].adjustment.value", "number"),
          createFieldValueMapping("orderItems[].adjustment.currency", "string"),
          createFieldValueMapping("orderItems[].adjustment.exponent", "number"),
          createFieldValueMapping("orderItems[].product.catalogEntryId", "number"),
          FieldValueMapping.builder().fieldName("orderItems[].product.stockMode").fieldType("enum").valueLength(0)
                           .fieldValueList("[UNKNOWN, NORMAL, TRANSIT_PRESALE, VIRTUALSTOCK_PRESALE, SOD_PRESALE, PREORDER]").required(true)
                           .isAncestorRequired(true).build(),
          createFieldValueMapping("orderItems[].virtualGiftCard.catalogEntryId", "number"),
          createFieldValueMapping("orderItems[].virtualGiftCard.senderName", "string"),
          createFieldValueMapping("orderItems[].virtualGiftCard.message", "string"),
          createFieldValueMapping("orderItems[].virtualGiftCard.style", "string"),
          createFieldValueMapping("orderItems[].virtualGiftCard.receiverName", "string"),
          createFieldValueMapping("orderItems[].virtualGiftCard.deliveryDate", "string"),
          createFieldValueMapping("orderItems[].physicalGiftCard.catalogEntryId", "number"),
          createFieldValueMapping("orderItems[].physicalGiftCard.senderName", "string"),
          createFieldValueMapping("orderItems[].physicalGiftCard.message", "string"),
          createFieldValueMapping("orderItems[].physicalGiftCard.receiverPhonePrefix", "string"),
          createFieldValueMapping("orderItems[].physicalGiftCard.receiverPhoneNumber", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MULTIPLE_TYPE = new Pair<>(
      new FileHelper().getFile("/jsonschema/multiple-type.jcs"),
      Arrays.asList(
          createFieldValueMapping("id", "number"),
          createFieldValueMapping("version", "number"),
          createFieldValueMapping("dtype", "string"),
          createFieldValueMapping("timestamp", "string"),
          createFieldValueMapping("event_type", "string"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MULTIPLE_TYPE_SINGLE = new Pair<>(
      new FileHelper().getFile("/jsonschema/multiple-type-single.jcs"),
      Arrays.asList(
          createFieldValueMapping("id", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_NESTED_COLLECTIONS = new Pair<>(
      new FileHelper().getFile("/jsonschema/nested-collections.jcs"),
      Arrays.asList(
          createFieldValueMapping("arrayOfMapsOfObjects[][:].stringObject", "string"),
          createFieldValueMapping("arrayOfMapsOfObjects[][:].numberObject", "number"),
          createFieldValueMapping("arrayOfArraysOfStrings[][]", "string-array-array"),
          createFieldValueMapping("mapOfArraysOfStrings[:][]", "string-array-map"),
          createFieldValueMapping("mapOfMapsOfObjects[:][:].name4Object", "string"),
          createFieldValueMapping("mapOfMapsOfObjects[:][:].number4Object", "number"),
          createFieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].stringControl", "string"),
          createFieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].numberControl", "number"))
  );

  static final Pair<File, List<FieldValueMapping>> TEST_MAP = new Pair<>(
      new FileHelper().getFile("/jsonschema/test-map.jcs"),
      Arrays.asList(
          createFieldValueMapping("firstName", "string"),
          createFieldValueMapping("lastName", "string"),
          createFieldValueMapping("age", "number"),
          createFieldValueMapping("testMap.itemType[:]", "number-map"),
          createFieldValueMapping("testMap.itemTipo[:]", "string-map"))
  );

}
