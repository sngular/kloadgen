package net.coru.kloadgen.extractor.parser.fixture;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import net.coru.kloadgen.model.json.ArrayField;
import net.coru.kloadgen.model.json.BooleanField;
import net.coru.kloadgen.model.json.DateField;
import net.coru.kloadgen.model.json.EnumField;
import net.coru.kloadgen.model.json.IntegerField;
import net.coru.kloadgen.model.json.NumberField;
import net.coru.kloadgen.model.json.ObjectField;
import net.coru.kloadgen.model.json.Schema;
import net.coru.kloadgen.model.json.StringField;

public class JsonSchemaFixturesConstants {

  public static final Schema SIMPLE_SCHEMA =
      Schema.builder()
            .id("https://example.com/person.schema.json")
            .name("http://json-schema.org/draft-07/schema#")
            .type("object")
            .property(StringField.builder().name("firstName").build())
            .property(StringField.builder().name("lastName").build())
            .property(IntegerField.builder().name("age").build())
            .build();

  public static final Schema SIMPLE_SCHEMA_NUMBER =
      Schema.builder()
            .id("https://example.com/geographical-location.schema.json")
            .name("http://json-schema.org/draft-07/schema#")
            .type("object")
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
            .property(ArrayField.builder().name("fruits").value(StringField.builder().build()).build())
            .property(ArrayField
                          .builder()
                          .name("vegetables")
                          .value(ObjectField
                                     .builder()
                                     .property(StringField.builder().name("veggieName").build())
                                     .property(BooleanField.builder().name("veggieLike").build())
                                     .build())
                          .build())
            .description(ObjectField
                             .builder()
                             .name("veggie")
                             .property(StringField.builder().name("veggieName").build())
                             .property(BooleanField.builder().name("veggieLike").build())
                             .build())
            .build();

  public static final Schema COMPLEX_SCHEMA =
      Schema.builder()
            .id("")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
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
                                                   .build())
                                        .build())
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
                          .build())
            .property(EnumField.builder().name("_entity").enumValues(singletonList("AddressSnapshot")).defaultValue("AddressSnapshot").build())
            .property(EnumField.builder().name("_class").enumValues(singletonList("AddressSnapshot")).defaultValue("AddressSnapshot").build())
            .build();

  public static final Schema MEDIUM_COMPLEX_SCHEMA =
      Schema.builder()
            .id("")
            .name("http://json-schema.org/draft-04/schema#")
            .type("object")
            .property(EnumField.builder().name("_class").enumValues(singletonList("OrderPublicDetailDocument")).defaultValue("OrderPublicDetailDocument").build())
            .property(EnumField.builder().name("_entity").enumValues(singletonList("OrderPublicDetailDocument")).defaultValue("OrderPublicDetailDocument").build())
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
                          .build())
            .property(StringField.builder().name("orderId").regex("^(.*)$").build())
            .property(IntegerField.builder().name("storeId").build())
            .property(StringField.builder().name("paymentId").regex("^(.*)$").build())
            .property(StringField.builder().name("parentId").regex("^(.*)$").build())
            .property(StringField.builder().name("externalId").regex("^(.*)$").build())
            .property(IntegerField.builder().name("editorId").build())
            .property(
                EnumField.builder().name("type").enumValues(asList("REGULAR", "REPLACEMENT", "EXCHANGE", "REPOSITION", "ONLINE_EXCHANGE", "STORE_EXCHANGE")).defaultValue("REGULAR")
                         .build())
            .property(
                EnumField.builder().name("status").enumValues(asList("RECEIVED", "VALIDATED", "PREPARING", "SHIPPED", "CLOSED", "CANCELLED")).defaultValue("RECEIVED").build())
            .property(
                EnumField.builder().name("fraudStatus").enumValues(asList("NOT_APPLIED", "PENDING", "ACCEPTED", "REQUIRED_REVIEW", "REJECTED")).defaultValue("NOT_APPLIED").build())
            .property(EnumField.builder().name("stockStatus").enumValues(asList("UNKNOWN", "NOT_APPLIED", "SYNCHRONIZED", "SKIPPED")).defaultValue("UNKNOWN").build())
            .property(EnumField.builder().name("paymentStatus").enumValues(asList("NOT_APPLIED", "UNKNOWN", "REJECTED", "ACCEPTED")).defaultValue("NOT_APPLIED").build())
            .property(EnumField.builder().name("paymentSystem").enumValues(asList("UNKNOWN", "ALPHA", "WCS")).defaultValue("UNKNOWN").build())
            .property(EnumField.builder().name("manualReviewStatus").enumValues(asList("NA", "PENDING", "ACCEPTED", "REFUSED")).defaultValue("NA").build())
            .property(IntegerField.builder().name("customerId").build())
            .property(EnumField.builder().name("customerType").enumValues(asList("REGISTERED", "GUEST")).defaultValue("REGISTERED").build())
            .property(EnumField.builder().name("guestAction").enumValues(asList("NO_ACTION", "CONVERSION", "LINK_ACCOUNT")).defaultValue("NO_ACTION").build())
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
                                        .build())
                          .property(ObjectField
                                        .builder()
                                        .name("confirmedAmount")
                                        .property(IntegerField.builder().name("value").build())
                                        .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                        .property(IntegerField.builder().name("exponent").build())
                                        .build())
                          .build())
            .property(ObjectField
                          .builder()
                          .name("billing")
                          .property(EnumField.builder().name("status").enumValues(asList("NOT_APPLIED", "PENDING", "BILLED")).defaultValue("NOT_APPLIED").build())
                          .property(StringField.builder().name("addressId").regex("^(.*)$").build())
                          .build())
            .property(ObjectField
                          .builder()
                          .name("origin")
                          .property(StringField.builder().name("systemCode").regex("^(.*)$").build())
                          .property(EnumField.builder().name("systemType").enumValues(asList("CHECKOUT", "MPS", "BACKOFFICE", "STORE")).defaultValue("CHECKOUT").build())
                          .property(StringField.builder().name("systemUser").regex("^(.*)$").build())
                          .property(StringField.builder().name("systemDeviceId").regex("^(.*)$").build())
                          .property(StringField.builder().name("deviceType").regex("^(.*)$").build())
                          .property(StringField.builder().name("deviceUserAgent").regex("^(.*)$").build())
                          .build())
            .property(ArrayField
                          .builder()
                          .name("orderItems")
                          .value(ObjectField
                                     .builder()
                                     .property(StringField.builder().name("id").regex("^(.*)$").build())
                                     .property(StringField.builder().name("partNumber").regex("^(.*)$").build())
                                     .property(EnumField.builder().name("type").defaultValue("PRODUCT")
                                                        .enumValues(asList("PRODUCT", "VIRTUAL_GIFT_CARD", "PHYSICAL_GIFT_CARD", "SHIPPING_COST", "SERVICE")).build())
                                     .property(
                                         EnumField.builder().name("status").defaultValue("RECEIVED").enumValues(asList("RECEIVED", "PREPARING", "SHIPPED", "CLOSED", "CANCELLED"))
                                                  .build())
                                     .property(StringField.builder().name("statusReason").regex("^(.*)$").build())
                                     .property(StringField.builder().name("statusDatetime").regex("^(.*)$").build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("pvpAmount")
                                                   .property(IntegerField.builder().name("value").build())
                                                   .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                                   .property(IntegerField.builder().name("exponent").build())
                                                   .build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("taxesAmount")
                                                   .property(IntegerField.builder().name("value").build())
                                                   .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                                   .property(IntegerField.builder().name("exponent").build())
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
                                                   .build())
                                     .property(ObjectField
                                                   .builder()
                                                   .name("product")
                                                   .property(IntegerField.builder().name("catalogEntryId").build())
                                                   .property(EnumField.builder().name("stockMode")
                                                                      .enumValues(asList("UNKNOWN", "NORMAL", "TRANSIT_PRESALE", "VIRTUALSTOCK_PRESALE", "SOD_PRESALE", "PREORDER"))
                                                                      .defaultValue("UNKNOWN").build())
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
                                     .build())
                          .build())
            .requiredFields(asList("_class", "_entity", "_metadata", "orderId", "storeId", "type", "status", "origin", "orderItems"))
            .definitions(asList(
                             ObjectField
                                 .builder()
                                 .name("amount")
                                 .property(IntegerField.builder().name("value").build())
                                 .property(StringField.builder().name("currency").regex("^(.*)$").build())
                                 .property(IntegerField.builder().name("exponent").build())
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
}
