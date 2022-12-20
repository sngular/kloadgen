package com.sngular.kloadgen.extractor.parser.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sngular.kloadgen.model.json.ArrayField;
import com.sngular.kloadgen.model.json.BooleanField;
import com.sngular.kloadgen.model.json.DateField;
import com.sngular.kloadgen.model.json.EnumField;
import com.sngular.kloadgen.model.json.IntegerField;
import com.sngular.kloadgen.model.json.MapField;
import com.sngular.kloadgen.model.json.NumberField;
import com.sngular.kloadgen.model.json.ObjectField;
import com.sngular.kloadgen.model.json.Schema;
import com.sngular.kloadgen.model.json.StringField;

public class JsonSchemaFixturesConstants {

  public static final Schema SIMPLE_SCHEMA =
      Schema.builder()
            .id("https://example.com/person.schema.json")
            .name("http://json-schema.org/draft-07/schema#")
            .type("object")
            .requiredFields(Arrays.asList("lastName", "age"))
            .property(StringField.builder().name("firstName").build())
            .property(StringField.builder().name("lastName").build())
            .property(IntegerField.builder().name("age").build())
            .build();

  public static final Schema SIMPLE_SCHEMA_NUMBER =
      Schema.builder()
            .id("https://example.com/geographical-location.schema.json")
            .name("http://json-schema.org/draft-07/schema#")
            .type("object")
            .requiredFields(Arrays.asList("latitude", "longitude"))
            .property(NumberField
                          .builder()
                          .name("latitude")
                          .minimum(-90)
                          .maximum(90)
                          .exclusiveMaximum(0)
                          .exclusiveMinimum(0)
                          .multipleOf(0)
                          .build())
            .property(NumberField
                          .builder()
                          .name("longitude")
                          .minimum(-180)
                          .maximum(180)
                          .exclusiveMinimum(0)
                          .exclusiveMaximum(0)
                          .multipleOf(0)
                          .build())
            .build();

  public static final Schema SIMPLE_SCHEMA_ARRAY =
      Schema.builder()
            .id("https://example.com/arrays.schema.json")
            .name("http://json-schema.org/draft-07/schema#")
            .type("object")
            .property(ArrayField.builder().name("fruits").value(StringField.builder().build()).isFieldRequired(true).build())
            .property(ArrayField
                          .builder()
                          .name("vegetables")
                          .value(ObjectField
                                     .builder()
                                     .property(StringField.builder().name("veggieName").build())
                                     .property(BooleanField.builder().name("veggieLike").build())
                                     .isFieldRequired(false)
                                     .required(Arrays.asList("veggieName", "veggieLike"))
                                     .build())
                          .isFieldRequired(true)
                          .build())
            .description(ObjectField
                             .builder()
                             .name("veggie")
                             .property(StringField.builder().name("veggieName").build())
                             .property(BooleanField.builder().name("veggieLike").build())
                             .isFieldRequired(false)
                             .required(Arrays.asList("veggieName", "veggieLike"))
                             .build())
            .build();

  public static final Schema COMPLEX_SCHEMA =
      Schema.builder()
            .id("")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
            .requiredFields(
                Arrays.asList("_id", "userId", "storeId", "snapshotId", "addressId", "addressLine", "alias", "contactInformation", "countryCode", "geopoliticalSubdivisions",
                              "_metadata",
                              "_entity", "_class", "contactInformation.email", "contactInformation.firstName", "contactInformation.phones", "geopoliticalSubdivisions.level1",
                              "_metadata.createdAt", "_metadata.createdBy", "_metadata.lastUpdatedAt", "_metadata.lastUpdatedBy", "_metadata.projectVersion",
                              "_metadata.projectName",
                              "_metadata.schema"))
            .property(StringField.builder().name("_id").build())
            .property(NumberField.builder().name("userId").minimum(1).maximum(0).exclusiveMinimum(0).exclusiveMaximum(0).multipleOf(0).build())
            .property(NumberField.builder().name("storeId").minimum(0).maximum(0).exclusiveMinimum(0).exclusiveMaximum(0).multipleOf(0).build())
            .property(StringField.builder().name("snapshotId").build())
            .property(StringField.builder().name("addressId").build())
            .property(StringField.builder().name("addressLine").build())
            .property(StringField.builder().name("alias").build())
            .property(ObjectField
                          .builder()
                          .name("contactInformation")
                          .property(StringField.builder().name("email").build())
                          .property(StringField.builder().name("firstName").build())
                          .property(StringField.builder().name("middleName").build())
                          .property(StringField.builder().name("lastName").build())
                          .property(StringField.builder().name("honorific").maxlength(3).minLength(2).regex("^[a-zA-Z]{2,3}$").build())
                          .property(ArrayField
                                        .builder()
                                        .name("phones")
                                        .value(ObjectField
                                                   .builder()
                                                   .property(StringField.builder().name("prefix").maxlength(3).minLength(2).build())
                                                   .property(StringField.builder().name("number").maxlength(6).build())
                                                   .required(Arrays.asList("prefix", "number"))
                                                   .isFieldRequired(false)
                                                   .build())
                                        .isFieldRequired(true)
                                        .build())
                          .required(Arrays.asList("email", "firstName", "phones"))
                          .isFieldRequired(true)
                          .build())
            .property(StringField.builder().name("countryCode").maxlength(2).minLength(2).regex("^[a-zA-Z]{2}$").build())
            .property(ObjectField.builder().name("location")
                                 .property(StringField.builder().name("streetName").build())
                                 .property(StringField.builder().name("streetNumber").build())
                                 .property(StringField.builder().name("floor").build())
                                 .property(StringField.builder().name("door").build())
                                 .property(StringField.builder().name("doorCode").build())
                                 .property(StringField.builder().name("zipCode").build())
                                 .build())
            .property(ObjectField
                          .builder()
                          .name("geopoliticalSubdivisions")
                          .property(ObjectField
                                        .builder()
                                        .name("level1")
                                        .property(StringField.builder().name("code").maxlength(3).minLength(2).build())
                                        .property(StringField.builder().name("freeForm").maxlength(256).minLength(1).build())
                                        .isFieldRequired(true)
                                        .build())
                          .property(ObjectField
                                        .builder()
                                        .name("level2")
                                        .property(StringField.builder().name("code").maxlength(3).minLength(2).build())
                                        .property(StringField.builder().name("freeForm").maxlength(256).minLength(1).build())
                                        .build())
                          .property(ObjectField
                                        .builder()
                                        .name("level3")
                                        .property(StringField.builder().name("code").maxlength(3).minLength(2).build())
                                        .property(StringField.builder().name("freeForm").maxlength(256).minLength(1).build())
                                        .build())
                          .property(ObjectField
                                        .builder()
                                        .name("level4")
                                        .property(StringField.builder().name("code").maxlength(3).minLength(2).build())
                                        .property(StringField.builder().name("freeForm").maxlength(256).minLength(1).build())
                                        .build())
                          .required("level1")
                          .isFieldRequired(true)
                          .build())
            .property(ObjectField
                          .builder()
                          .name("_metadata")
                          .property(DateField.builder().name("createdAt").format("date-time").build())
                          .property(StringField.builder().name("createdBy").build())
                          .property(DateField.builder().name("lastUpdatedAt").format("date-time").build())
                          .property(StringField.builder().name("lastUpdatedBy").build())
                          .property(DateField.builder().name("deletedAt").format("date-time").build())
                          .property(StringField.builder().name("projectVersion").build())
                          .property(StringField.builder().name("projectName").build())
                          .property(StringField.builder().name("deletedBy").build())
                          .property(NumberField.builder().name("schema").minimum(0).maximum(0).exclusiveMaximum(0).exclusiveMinimum(0).multipleOf(0).build())
                          .required(Arrays.asList("createdAt", "createdBy", "lastUpdatedAt", "lastUpdatedBy", "projectVersion", "projectName", "schema"))
                          .isFieldRequired(true)
                          .build())
            .property(EnumField.builder().name("_entity").enumValues(Collections.singletonList("AddressSnapshot")).defaultValue("AddressSnapshot").build())
            .property(EnumField.builder().name("_class").enumValues(Collections.singletonList("AddressSnapshot")).defaultValue("AddressSnapshot").build())
            .build();

  public static final Schema MEDIUM_COMPLEX_SCHEMA =
      Schema.builder()
            .id("")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
            .property(EnumField.builder().name("_class").enumValues(Collections.singletonList("OrderPublicDetailDocument")).defaultValue("OrderPublicDetailDocument").build())
            .property(EnumField.builder().name("_entity").enumValues(Collections.singletonList("OrderPublicDetailDocument")).defaultValue("OrderPublicDetailDocument").build())
            .property(ObjectField
                          .builder()
                          .name("_metadata")
                          .property(NumberField.builder().name("createdAt").minimum(0).maximum(0).exclusiveMaximum(0).exclusiveMinimum(0).multipleOf(0).build())
                          .property(StringField.builder().name("createdBy").build())
                          .property(NumberField.builder().name("deletedAt").minimum(0).maximum(0).exclusiveMinimum(0).exclusiveMaximum(0).multipleOf(0).build())
                          .property(StringField.builder().name("deletedBy").build())
                          .property(NumberField.builder().name("lastUpdatedAt").minimum(0).maximum(0).exclusiveMaximum(0).exclusiveMinimum(0).multipleOf(0).build())
                          .property(StringField.builder().name("lastUpdatedBy").build())
                          .property(StringField.builder().name("projectName").build())
                          .property(StringField.builder().name("projectVersion").build())
                          .property(NumberField.builder().name("schema").minimum(0).maximum(0).exclusiveMaximum(0).exclusiveMinimum(0).multipleOf(0).build())
                          .required(Arrays.asList("createdAt", "createdBy", "lastUpdatedAt", "lastUpdatedBy"))
                          .isFieldRequired(true)
                          .build())
            .property(StringField.builder().name("orderId").regex("^(.*)$").build())
            .property(IntegerField.builder().name("storeId").build())
            .property(StringField.builder().name("paymentId").regex("^(.*)$").build())
            .property(StringField.builder().name("parentId").regex("^(.*)$").build())
            .property(StringField.builder().name("externalId").regex("^(.*)$").build())
            .property(IntegerField.builder().name("editorId").build())
            .property(
                EnumField.builder().name("type").enumValues(Arrays.asList("REGULAR", "REPLACEMENT", "EXCHANGE", "REPOSITION", "ONLINE_EXCHANGE", "STORE_EXCHANGE"))
                         .defaultValue("REGULAR")
                         .build())
            .property(
                EnumField.builder().name("status").enumValues(Arrays.asList("RECEIVED", "VALIDATED", "PREPARING", "SHIPPED", "CLOSED", "CANCELLED")).defaultValue("RECEIVED")
                         .build())
            .property(
                EnumField.builder().name("fraudStatus").enumValues(Arrays.asList("NOT_APPLIED", "PENDING", "ACCEPTED", "REQUIRED_REVIEW", "REJECTED")).defaultValue("NOT_APPLIED")
                         .build())
            .property(EnumField.builder().name("stockStatus").enumValues(Arrays.asList("UNKNOWN", "NOT_APPLIED", "SYNCHRONIZED", "SKIPPED")).defaultValue("UNKNOWN").build())
            .property(EnumField.builder().name("paymentStatus").enumValues(Arrays.asList("NOT_APPLIED", "UNKNOWN", "REJECTED", "ACCEPTED")).defaultValue("NOT_APPLIED").build())
            .property(EnumField.builder().name("paymentSystem").enumValues(Arrays.asList("UNKNOWN", "ALPHA", "WCS")).defaultValue("UNKNOWN").build())
            .property(EnumField.builder().name("manualReviewStatus").enumValues(Arrays.asList("NA", "PENDING", "ACCEPTED", "REFUSED")).defaultValue("NA").build())
            .property(IntegerField.builder().name("customerId").build())
            .property(EnumField.builder().name("customerType").enumValues(Arrays.asList("REGISTERED", "GUEST")).defaultValue("REGISTERED").build())
            .property(EnumField.builder().name("guestAction").enumValues(Arrays.asList("NO_ACTION", "CONVERSION", "LINK_ACCOUNT")).defaultValue("NO_ACTION").build())
            .property(StringField.builder().name("locale").regex("^(.*)$").build())
            .property(BooleanField.builder().name("preorder").build())
            .property(BooleanField.builder().name("fastSint").build())
            .property(BooleanField.builder().name("pvpTaxesIncluded").build())
            .property(StringField.builder().name("placedDatetime").regex("^(.*)$").build())
            .property(StringField.builder().name("cancelledDatetime").regex("^(.*)$").build())
            .property(StringField.builder().name("lastUpdateDatetime").regex("^(.*)$").build())
            .property(StringField.builder().name("snapshotDatetime").regex("^(.*)$").build())
            .property(ObjectField
                          .builder()
                          .name("duty")
                          .property(ObjectField
                                        .builder()
                                        .name("amount")
                                        .property(IntegerField.builder().name("value").build())
                                        .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                        .property(IntegerField.builder().name("exponent").build())
                                        .required(Arrays.asList("value", "currency", "exponent"))
                                        .build())
                          .property(ObjectField
                                        .builder()
                                        .name("confirmedAmount")
                                        .property(IntegerField.builder().name("value").build())
                                        .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                        .property(IntegerField.builder().name("exponent").build())
                                        .required(Arrays.asList("value", "currency", "exponent"))
                                        .build())
                          .build())
            .property(ObjectField
                          .builder()
                          .name("billing")
                          .property(EnumField.builder().name("status").enumValues(Arrays.asList("NOT_APPLIED", "PENDING", "BILLED")).defaultValue("NOT_APPLIED").build())
                          .property(StringField.builder().name("addressId").regex("^(.*)$").build())
                          .required(Arrays.asList("status", "addressId"))
                          .build())
            .property(ObjectField
                          .builder()
                          .name("origin")
                          .property(StringField.builder().name("systemCode").regex("^(.*)$").build())
                          .property(EnumField.builder().name("systemType").enumValues(Arrays.asList("CHECKOUT", "MPS", "BACKOFFICE", "STORE")).defaultValue("CHECKOUT").build())
                          .property(StringField.builder().name("systemUser").regex("^(.*)$").build())
                          .property(StringField.builder().name("systemDeviceId").regex("^(.*)$").build())
                          .property(StringField.builder().name("deviceType").regex("^(.*)$").build())
                          .property(StringField.builder().name("deviceUserAgent").regex("^(.*)$").build())
                          .required("systemType")
                          .isFieldRequired(true)
                          .build())
            .property(ArrayField
                          .builder()
                          .name("orderItems")
                          .value(ObjectField
                                     .builder()
                                     .property(StringField.builder().name("id").regex("^(.*)$").build())
                                     .property(StringField.builder().name("partNumber").regex("^(.*)$").build())
                                     .property(EnumField.builder().name("type").defaultValue("PRODUCT")
                                                        .enumValues(Arrays.asList("PRODUCT", "VIRTUAL_GIFT_CARD", "PHYSICAL_GIFT_CARD", "SHIPPING_COST", "SERVICE")).build())
                                     .property(
                                         EnumField.builder().name("status").defaultValue("RECEIVED").enumValues(
                                                      Arrays.asList("RECEIVED", "PREPARING", "SHIPPED", "CLOSED", "CANCELLED"))
                                                  .build())
                                     .property(StringField.builder().name("statusReason").regex("^(.*)$").build())
                                     .property(StringField.builder().name("statusDatetime").regex("^(.*)$").build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("pvpAmount")
                                                   .property(IntegerField.builder().name("value").build())
                                                   .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                                   .property(IntegerField.builder().name("exponent").build())
                                                   .required(Arrays.asList("value", "currency", "exponent"))
                                                   .isFieldRequired(true)
                                                   .build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("taxesAmount")
                                                   .property(IntegerField.builder().name("value").build())
                                                   .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                                   .property(IntegerField.builder().name("exponent").build())
                                                   .required(Arrays.asList("value", "currency", "exponent"))
                                                   .build())
                                     .property(NumberField.builder().name("taxesPercentage").maximum(0).minimum(0).exclusiveMinimum(0).exclusiveMaximum(0).multipleOf(0).build())
                                     .property(StringField.builder().name("initialMinimumDate").regex("^(.*)$").build())
                                     .property(StringField.builder().name("initialMaximumDate").regex("^(.*)$").build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("adjustment")
                                                   .property(IntegerField.builder().name("value").build())
                                                   .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                                   .property(IntegerField.builder().name("exponent").build())
                                                   .required(Arrays.asList("value", "currency", "exponent"))
                                                   .build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("product")
                                                   .property(IntegerField.builder().name("catalogEntryId").build())
                                                   .property(EnumField.builder().name("stockMode")
                                                                      .enumValues(
                                                                          Arrays.asList("UNKNOWN", "NORMAL", "TRANSIT_PRESALE", "VIRTUALSTOCK_PRESALE", "SOD_PRESALE", "PREORDER"))
                                                                      .defaultValue("UNKNOWN").build())
                                                   .required(Arrays.asList("catalogEntryId", "stockMode"))
                                                   .build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("virtualGiftCard")
                                                   .property(IntegerField.builder().name("catalogEntryId").maximum(0).minimum(0).build())
                                                   .property(StringField.builder().name("senderName").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("message").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("style").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("receiverName").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("deliveryDate").regex("^(.*)$").build())
                                                   .build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("physicalGiftCard")
                                                   .property(IntegerField.builder().name("catalogEntryId").maximum(0).minimum(0).build())
                                                   .property(StringField.builder().name("senderName").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("message").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("receiverPhonePrefix").regex("^(.*)$").build())
                                                   .property(StringField.builder().name("receiverPhoneNumber").regex("^(.*)$").build())
                                                   .build())
                                     .required(Arrays.asList("id", "type", "pvpAmount", "promotions"))
                                     .isFieldRequired(false)
                                     .build())
                          .isFieldRequired(true)
                          .build())
            .requiredFields(
                Arrays.asList("_class", "_entity", "_metadata", "orderId", "storeId", "type", "status", "origin", "orderItems", "_metadata.createdAt", "_metadata.createdBy",
                              "_metadata.lastUpdatedAt", "_metadata.lastUpdatedBy", "billing.status", "billing.addressId", "origin.systemType"))
            .definitions(Arrays.asList(
                             ObjectField
                                 .builder()
                                 .name("amount")
                                 .property(IntegerField.builder().name("value").build())
                                 .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                 .property(IntegerField.builder().name("exponent").build())
                                 .required(Arrays.asList("value", "currency", "exponent"))
                                 .build(),
                             ObjectField
                                 .builder()
                                 .name("giftCard")
                                 .property(IntegerField.builder().name("catalogEntryId").maximum(0).minimum(0).build())
                                 .property(StringField.builder().name("senderName").regex("^(.*)$").build())
                                 .property(StringField.builder().name("message").regex("^(.*)$").build())
                                 .build()
                         )
            )
            .build();

  public static final Schema COLLECTIONS_SCHEMA =
      Schema.builder()
            .id("https://example.com/collections.schema.json")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
            .property(MapField.builder().name("mapOfStrings").mapType(
                StringField.builder().name("internalMapField").build()).isFieldRequired(true).build())
            .property(ArrayField.builder().name("arrayOfObjectsOfBasicTypes").value(
                ObjectField.builder()
                           .property(StringField.builder().name("stringOfObject").build())
                           .property(IntegerField.builder().name("numberOfObject").maximum(0).minimum(0).build())
                           .isFieldRequired(true)
                           .build()
            ).minItems(1).uniqueItems(false).isFieldRequired(true).build())
            .property(ObjectField.builder()
                                 .name("objectOfCollectionsOfBasicTypes")
                                 .property(ArrayField.builder().name("arrayOfStrings")
                                                     .value(StringField.builder().build())
                                                     .minItems(1)
                                                     .isFieldRequired(true)
                                                     .build())
                                 .property(MapField.builder()
                                                   .name("mapOfIntegers")
                                                   .mapType(IntegerField.builder().name("internalMapField").maximum(0).minimum(0).build())
                                                   .isFieldRequired(true)
                                                   .build())
                                 .property(StringField.builder().name("stringControl").build())
                                 .isFieldRequired(true)
                                 .build())
            .property(ObjectField.builder()
                                 .name("objectOfCollectionsOfObject")
                                 .property(StringField.builder().name("stringControl").build())
                                 .property(ArrayField.builder()
                                                     .name("arrayOfObjectsPerson")
                                                     .value(ObjectField.builder()
                                                                       .property(StringField.builder().name("namePerson").build())
                                                                       .property(IntegerField.builder().name("phonePerson").maximum(0).minimum(0).build())
                                                                       .build()
                                                     )
                                                     .isFieldRequired(true).build())
                                 .property(MapField.builder()
                                                   .name("mapOfObjectsDog")
                                                   .mapType(ObjectField.builder()
                                                                       .name("internalMapField")
                                                                       .property(StringField.builder().name("nameDog").build())
                                                                       .property(ObjectField.builder()
                                                                                            .name("vetData")
                                                                                            .property(IntegerField.builder().name("dogId").maximum(0).minimum(0).build())
                                                                                            .property(StringField.builder().name("breedName").build())
                                                                                            .build()
                                                                       ).build())
                                                   .isFieldRequired(true)
                                                   .build())
                                 .isFieldRequired(true)
                                 .build())
            .requiredFields(Arrays.asList("objectOfCollectionsOfBasicTypes", "objectOfCollectionsOfObject"))
            .build();

  public static final Schema NESTED_COLLECTIONS_SCHEMA =
      Schema.builder()
            .id("https://example.com/nested-collections.schema.json")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
            .property(ArrayField.builder().name("arrayOfMapsOfObjects").value(
                MapField.builder()
                        .mapType(ObjectField.builder().name("internalMapField")
                                            .property(StringField.builder().name("stringObject").build())
                                            .property(IntegerField.builder().name("numberObject").maximum(0).minimum(0).build()
                                            ).build())
                        .isFieldRequired(true).build()).minItems(1).uniqueItems(false).isFieldRequired(true).build())
            .property(ArrayField.builder().name("arrayOfArraysOfStrings").value(
                ArrayField.builder()
                          .value(StringField.builder().build())
                          .minItems(1)
                          .build()).isFieldRequired(true).build())
            .property(MapField.builder().name("mapOfArraysOfStrings").mapType(
                ArrayField.builder().name("internalMapField")
                          .value(StringField.builder().build())
                          .build()).isFieldRequired(true).build())
            .property(MapField.builder().name("mapOfMapsOfObjects").mapType(
                MapField.builder().name("internalMapField").mapType(
                    ObjectField.builder().name("internalMapField")
                               .property(StringField.builder().name("name4Object").build())
                               .property(IntegerField.builder().name("number4Object").maximum(0).minimum(0).build())
                               .build()
                ).build()).isFieldRequired(true).build())
            .property(MapField.builder().name("mapOfObjectsOfCollections").mapType(
                                  ObjectField.builder().name("internalMapField")
                                             .property(ArrayField.builder().name("arrayOfMapsOfObject").value(
                                                                     MapField.builder().mapType(
                                                                         ObjectField.builder().name("internalMapField")
                                                                                    .property(StringField.builder().name("stringControl").build())
                                                                                    .property(IntegerField.builder().name("numberControl").maximum(0).minimum(0).build())
                                                                                    .build()
                                                                     ).build())
                                                                 .isFieldRequired(true)
                                                                 .build()).build())
                              .isFieldRequired(true).build())
            .build();

  public static final Schema DEFINITIONS_COMPLEX_SCHEMA =
      Schema.builder()
            .id("")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
            .property(ObjectField.builder().name("objectOfDefinitions")
                                 .property(StringField.builder().name("stringControl").build())
                                 .property(ArrayField.builder().name("arrayOfStrings").value(StringField.builder().build())
                                                     .minItems(1).isFieldRequired(true).build())
                                 .property(MapField.builder().name("mapOfStrings").mapType(
                                                       StringField.builder().name("internalMapField").build())
                                                   .isFieldRequired(true).build())
                                 .required(List.of("stringControl", "arrayOfStrings")).isFieldRequired(true).build())
            .property(ArrayField.builder().name("arrayOfObjects").value(
                                    ObjectField.builder()
                                               .property(StringField.builder().name("stringOfObject").build())
                                               .property(IntegerField.builder().name("numberOfObject").maximum(0).minimum(0).build())
                                               .required(List.of("numberOfObject"))
                                               .build())
                                .isFieldRequired(true).build())
            .property(MapField.builder().name("mapOfObjects").mapType(
                ObjectField.builder().name("internalMapField")
                           .property(ArrayField.builder().name("arrayOfInternalObject").value(StringField.builder().build())
                                               .isFieldRequired(true).build())
                           .required(List.of("arrayOfInternalObject")).build()
            ).isFieldRequired(true).build())
            .property(MapField.builder().name("mapOfMaps").mapType(
                MapField.builder().name("internalMapField").mapType(
                    ObjectField.builder().name("internalMapField")
                               .property(StringField.builder().name("stringControlObject").build())
                               .property(ArrayField.builder().name("arrayOfArraysOfStrings").value(
                                   ArrayField.builder().value(
                                       StringField.builder().build()
                                   ).minItems(1).build()
                               ).isFieldRequired(true).build())
                               .required(List.of("stringControlObject")).build()
                ).build()
            ).isFieldRequired(true).build())
            .requiredFields(Arrays.asList("objectOfDefinitions", "objectOfDefinitions.stringControl", "objectOfDefinitions.arrayOfStrings"))
            .definitions(Arrays.asList(
                ObjectField.builder().name("objectDef")
                           .property(StringField.builder().name("stringOfObject").build())
                           .property(IntegerField.builder().name("numberOfObject").maximum(0).minimum(0).build())
                           .required(List.of("numberOfObject"))
                           .build(),
                MapField.builder().name("mapOfStringsDef").mapType(StringField.builder().name("internalMapField").build())
                        .build(),
                MapField.builder().name("mapOfObjectsDef").mapType(
                    ObjectField.builder().name("internalMapField")
                               .property(StringField.builder().name("stringControlObject").build())
                               .property(ArrayField.builder().name("arrayOfArraysOfStrings").value(
                                   ArrayField.builder().value(
                                       StringField.builder().build()
                                   ).minItems(1).build()
                               ).isFieldRequired(true).build())
                               .required(List.of("stringControlObject")).build()
                ).build(),
                ObjectField.builder().name("objectOfArraysDef")
                           .property(ArrayField.builder().name("arrayOfInternalObject").value(StringField.builder().build())
                                               .isFieldRequired(true).build())
                           .required(List.of("arrayOfInternalObject"))
                           .build(),
                ArrayField.builder().name("arrayOfStringsDef").value(StringField.builder().build())
                          .minItems(1).build()

            ))
            .build();

  protected JsonSchemaFixturesConstants() {
  }
}
