package com.sngular.kloadgen.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.collections.MapUtils;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtobufSchemaProcessorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final SchemaExtractor schemaExtractor = new SchemaExtractor();

  private final BaseSchemaMetadata<ConfluentSchemaMetadata> confluentBaseSchemaMetadata = new BaseSchemaMetadata<>(
      ConfluentSchemaMetadata.parse(new io.confluent.kafka.schemaregistry.client.SchemaMetadata(1, 1, "")));

  @BeforeEach
  public void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
    JMeterContextService.getContext().getProperties().put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, "CONFLUENT");

  }

  @Test
  @DisplayName("Be able to process embedded schema")
  void textEmbeddedTypeTestSchemaProcessor() throws KLoadGenException, IOException {
    final File testFile = fileHelper.getFile("/proto-files/embeddedTypeTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = List.of(
        FieldValueMapping.builder().fieldName("phones.addressesPhone[1:].id[1]").fieldType("string-array").fieldValueList("Pablo").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("phones.phoneType").fieldType("enum").fieldValueList("[MOBILE, HOME, WORK]").required(true).isAncestorRequired(true).build());
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();

    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });
    final String idField = getIdFieldForEmbeddedTypeTest(assertValues);
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(assertKeys).hasSize(1).containsExactlyInAnyOrder("tutorial.Person.phones");
    Assertions.assertThat(idField).isEqualTo("[Pablo]");
  }

  private String getIdFieldForEmbeddedTypeTest(final List<Object> assertValues) {
    final DynamicMessage dynamicMessage = (DynamicMessage) assertValues.get(0);
    final DynamicMessage firstMap = (DynamicMessage) ((List) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("addressesPhone"))).get(0);
    final DynamicMessage secondMapAsDynamicField = (DynamicMessage) firstMap.getField(firstMap.getDescriptorForType().findFieldByName("value"));
    return secondMapAsDynamicField.getField(secondMapAsDynamicField.getDescriptorForType().findFieldByName("id")).toString();
  }

  @Test
  @DisplayName("Be able to process complex types like StringValue or Int32Value and get values by default")
  void testProtobufGoogleTypes() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/googleTypesTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = List.of(
        FieldValueMapping.builder().fieldName("id").fieldType("Int32Value").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("occurrence_id").fieldType("StringValue").fieldValueList("Isabel").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("load_number").fieldType("Int32Value").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("date").fieldType("DateValue").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("timeofday").fieldType("TimeOfDateValue").required(true).isAncestorRequired(true).build());
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });
    final DynamicMessage dynamicMessage = (DynamicMessage) assertValues.get(1);
    final String secondValue = (String) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(secondValue).isEqualTo("Isabel");
    Assertions.assertThat(assertKeys).hasSize(5).containsExactlyInAnyOrder("abc.Incident.id",
                                                                           "abc.Incident.occurrence_id",
                                                                           "abc.Incident.load_number",
                                                                           "abc.Incident.date",
                                                                           "abc.Incident.timeofday");
  }

  @Test
  @DisplayName("Be able to process enum in the schema")
  void testProtoBufEnumSchemaProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/enumTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(new ParsedSchema(testFile, "PROTOBUF"));
    fieldValueMappingList.get(0).setFieldValuesList("HOME, WORK");
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });
    final String firstValue = assertValues.get(0).toString();
    final List<Object> secondValue = (List<Object>) assertValues.get(1);
    final List<Object> thirdValueMap = (List<Object>) assertValues.get(2);
    final DynamicMessage dynamicMessage = (DynamicMessage) thirdValueMap.get(0);
    final Object thirdValue = dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    Assertions.assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    Assertions.assertThat(message.getGenericRecord()).isNotNull();
    Assertions.assertThat(firstValue)
              .isNotNull()
              .isIn("HOME", "WORK", "MOBILE");
    Assertions.assertThat(secondValue.get(0).toString())
              .isNotNull()
              .isIn("HOME", "WORK", "MOBILE");
    Assertions.assertThat(thirdValue.toString())
              .isNotNull()
              .isIn("HOME", "WORK", "MOBILE");
    Assertions.assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.phoneTypes",
                                                                           "tutorial.Person.phoneTypesArray",
                                                                           "tutorial.Person.phoneTypesMap");
  }

  @Test
  @DisplayName("Be able to process easy schema")
  void testProtoBufEasyTestProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/easyTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(new ParsedSchema(testFile, "PROTOBUF"));
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });
    final List<Object> integerList = (List<Object>) assertValues.get(1);
    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();
    Assertions.assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Address.street", "tutorial.Address.number", "tutorial.Address.zipcode");
    Assertions.assertThat(assertValues).hasSize(3);
    Assertions.assertThat(assertValues.get(0)).isInstanceOf(String.class);
    Assertions.assertThat(integerList.get(0)).isInstanceOf(Integer.class);
    Assertions.assertThat(assertValues.get(2)).isInstanceOf(Long.class);

  }

  @Test
  @DisplayName("Be able to process oneOf fields")
  void testProtoBufOneOfProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/oneOfTest.proto");

    final List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(new ParsedSchema(testFile, "PROTOBUF"));
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });
    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();
    Assertions.assertThat(assertKeys)
              .hasSize(2)
              .containsAnyOf("tutorial.Address.type", "tutorial.Address.optionInt", "tutorial.Address.optionLong", "tutorial.Address.optionString")
              .element(0)
              .isEqualTo("tutorial.Address.type");
    Assertions.assertThat(assertValues).hasSize(2);

  }

  @Test
  @DisplayName("Be able to process map in schema")
  void testProtoBufMapTestProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/mapTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("name[:]").fieldType("string-map").fieldValueList("Pablo").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addresses[:].street").fieldType("string").fieldValueList("Sor Joaquina").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addresses[:].number").fieldType("int").fieldValueList("2").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addresses[:].zipcode").fieldType("int").fieldValueList("15011").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addressesNoDot[:].street").fieldType("string").fieldValueList("Sor Joaquina").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addressesNoDot[:].number").fieldType("int").fieldValueList("6").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("addressesNoDot[:].zipcode").fieldType("int").fieldValueList("15011").required(true).isAncestorRequired(true).build());
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });
    final String personName = getPersonNameForMapTestProcessor(assertValues);
    final List<Object> objectList = (List<Object>) assertValues.get(1);
    final DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
    final Object street = getSubFieldForMapTestProcessor(dynamicMessage, "street");
    final Object number = getSubFieldForMapTestProcessor(dynamicMessage, "number");
    final Object zipcode = getSubFieldForMapTestProcessor(dynamicMessage, "zipcode");

    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();
    Assertions.assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.name",
                                                                           "tutorial.Person.addresses",
                                                                           "tutorial.Person.addressesNoDot");
    Assertions.assertThat(assertValues).hasSize(3);
    Assertions.assertThat(personName).isEqualTo("Pablo");
    Assertions.assertThat(street).isInstanceOf(String.class).isEqualTo("Sor Joaquina");
    Assertions.assertThat(number).isInstanceOf(Integer.class).isEqualTo(2);
    Assertions.assertThat(zipcode).isInstanceOf(Integer.class).isEqualTo(15011);
  }

  private String getPersonNameForMapTestProcessor(final List<Object> assertValues) {
    final List<Object> objectList = (List<Object>) assertValues.get(0);
    final DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
    return (String) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
  }

  private Object getSubFieldForMapTestProcessor(final DynamicMessage dynamicMessage, final String field) {
    final DynamicMessage subDynamicMessage = (DynamicMessage) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    return subDynamicMessage.getField(subDynamicMessage.getDescriptorForType().findFieldByName(field));
  }

  @Test
  @DisplayName("Be able to process complex schema")
  void testProtoBufComplexTestProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/complexTest.proto");
    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry("CONFLUENT");
    schemaRegistryManager.setSchemaRegistryClient("http://localhost:8080", MapUtils.EMPTY_MAP);

    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
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
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()),
            confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });

    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();
    Assertions.assertThat(assertKeys).hasSize(9)
              .containsExactlyInAnyOrder("tutorial.Test.phone_types",
                                         "tutorial.Test.name",
                                         "tutorial.Test.age",
                                         "tutorial.Test.address",
                                         "tutorial.Test.pets",
                                         "tutorial.Test.descriptors",
                                         "tutorial.Test.dates",
                                         "tutorial.Test.response",
                                         "tutorial.Test.presents");
    Assertions.assertThat(assertValues).hasSize(9).isNotNull();
    Assertions.assertThat(assertValues.get(2)).isInstanceOf(Integer.class);
    Assertions.assertThat(assertValues.get(7)).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("Be able to process provided complex schema")
  void testProtoBufProvidedComplexTestProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/providedTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
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
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, new ParsedSchema(testFile,
            SchemaTypeEnum.PROTOBUF.name()), confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });

    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();
    Assertions.assertThat(assertKeys).hasSize(23)
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
    Assertions.assertThat(assertValues).isNotNull().hasSize(23);
  }

  @Test
  void testFailing() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/deveTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("load_type").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.carrier_identifier.type").fieldType("enum")
                         .fieldValueList("[CARRIER_IDENTIFIER_TYPE_UNSPECIFIED, CARRIER_IDENTIFIER_TYPE_DOT_NUMBER]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.carrier_identifier.value").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.shipment_identifiers[].type").fieldType("enum").fieldValueList("[SHIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, "
                                                                                                                       + "SHIPMENT_IDENTIFIER_TYPE_BILL_OF_LADING, "
                                                                                                                       + "SHIPMENT_IDENTIFIER_TYPE_ORDER]")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.shipment_identifiers[].value").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.equipment_identifiers[].type").fieldType("enum").fieldValueList("[EQUIPMENT_IDENTIFIER_TYPE_UNSPECIFIED, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_MOBILE_PHONE_NUMBER, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_VEHICLE_ID, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_LICENSE_PLATE, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_SENSITECH_DEVICE_ID, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_EMERSON_DEVICE_ID, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_TIVE_DEVICE_ID, "
                                                                                                                        + "EQUIPMENT_IDENTIFIER_TYPE_CONTAINER_ID]").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.equipment_identifiers[].value").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("shipment.attributes[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.timestamp").fieldType(".google.protobuf.Timestamp").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.stop_number").fieldType("int").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.status_code").fieldType("enum")
                         .fieldValueList("[STATUS_UPDATE_CODE_UNSPECIFIED, STATUS_UPDATE_CODE_DISPATCHED, STATUS_UPDATE_CODE_IN_TRANSIT, "
                                         + "STATUS_UPDATE_CODE_AT_STOP, STATUS_UPDATE_CODE_COMPLETED, STATUS_UPDATE_CODE_TRACKING_FAILED, "
                                         + "STATUS_UPDATE_CODE_INFO, STATUS_UPDATE_CODE_DELETED]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_status_update.status_reason").fieldType("enum")
                         .fieldValueList("[STATUS_UPDATE_REASON_UNSPECIFIED, STATUS_UPDATE_REASON_PENDING_TRACKING_METHOD, "
                                         + "STATUS_UPDATE_REASON_SCHEDULED, STATUS_UPDATE_REASON_PENDING_APPROVAL, "
                                         + "STATUS_UPDATE_REASON_ACQUIRING_LOCATION, STATUS_UPDATE_REASON_PENDING_CARRIER, STATUS_UPDATE_REASON_IN_MOTION, "
                                         + "STATUS_UPDATE_REASON_IDLE, STATUS_UPDATE_REASON_APPROVAL_DENIED, STATUS_UPDATE_REASON_TIMED_OUT, STATUS_UPDATE_REASON_CANCELED, "
                                         + "STATUS_UPDATE_REASON_DEPARTED_FINAL_STOP, STATUS_UPDATE_REASON_ARRIVED_FINAL_STOP, "
                                         + "STATUS_UPDATE_REASON_ARRIVED_FAILED_TO_ACQUIRE_LOCATION, STATUS_UPDATE_REASON_INFO]")
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
                         .fieldValueList("[STOP_STATUS_CODE_UNSPECIFIED, STOP_STATUS_CODE_UNKNOWN, STOP_STATUS_CODE_EN_ROUTE, "
                                         + "STOP_STATUS_CODE_ARRIVED, STOP_STATUS_CODE_DEPARTED]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.start_date_time").fieldType(".google.protobuf.Timestamp")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_estimate.estimated_arrival_window.end_date_time").fieldType(".google.protobuf.Timestamp")
                         .required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_estimate.last_calculated_date_time").fieldType(".google.protobuf.Timestamp").required(true)
                         .isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].arrival_code").fieldType("enum")
                         .fieldValueList("[ARRIVAL_CODE_UNSPECIFIED, ARRIVAL_CODE_UNKNOWN, ARRIVAL_CODE_EARLY, ARRIVAL_CODE_ON_TIME, "
                                         + "ARRIVAL_CODE_LATE]").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("latest_stop_statuses[].additional_appointment_window_statuses[]").fieldType("string-array").required(true).isAncestorRequired(true)
                         .build()
    );

    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF,
                                          new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()), confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });

    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();

    Assertions.assertThat(assertKeys).hasSize(4);

  }

  @Test
  @DisplayName("Be able to process provided schema with not nested type")
  void testProtoBufProvidedWithNotNestedTypeProcessor() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/issue311Test.proto");
    final List<FieldValueMapping> fieldValueMappingList = Arrays.asList(
        FieldValueMapping.builder().fieldName("order_id").fieldType("int").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("order_number").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("customer_account.billing_party.party_id").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("customer_account.billing_party.address.address_line_one").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("customer_account.billing_party.address.address_line_two").fieldType("string").required(true).isAncestorRequired(true).build());
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF,
                                          new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()), confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });

    Assertions.assertThat(message).isNotNull()
              .isInstanceOf(EnrichedRecord.class)
              .extracting(EnrichedRecord::getGenericRecord)
              .isNotNull();
    Assertions.assertThat(assertKeys).hasSize(3)
              .containsExactlyInAnyOrder("demo.Order.order_id",
                                         "demo.Order.order_number",
                                         "demo.Order.customer_account");
    Assertions.assertThat(assertValues).hasSize(3).isNotNull();
    Assertions.assertThat(assertValues.get(0)).isInstanceOf(Integer.class);
    Assertions.assertThat(assertValues.get(1)).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("Be able to process Date and TimeOfDay types")
  void testDateTimeTypes() throws IOException {
    final File testFile = fileHelper.getFile("/proto-files/dateTimeTest.proto");
    final List<FieldValueMapping> fieldValueMappingList = List.of(
        FieldValueMapping.builder().fieldName("incident_date").fieldType(".google.type.Date").fieldValueList("2022-05-30").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("incident_time").fieldType(".google.type.TimeOfDay").fieldValueList("14:20:30-05:00").required(true).isAncestorRequired(true)
                         .build());
    final SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF,
                                          new ParsedSchema(testFile, SchemaTypeEnum.PROTOBUF.name()), confluentBaseSchemaMetadata, fieldValueMappingList);
    final EnrichedRecord message = (EnrichedRecord) protobufSchemaProcessor.next();
    final DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
    final Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
    final List<String> assertKeys = new ArrayList<>();
    final List<Object> assertValues = new ArrayList<>();
    map.forEach((key, value) -> {
      assertKeys.add(key.getFullName());
      assertValues.add(value);
    });

    final DynamicMessage firstDynamicMessage = (DynamicMessage) assertValues.get(0);
    final Integer year = (Integer) firstDynamicMessage.getField(firstDynamicMessage.getDescriptorForType().findFieldByName("year"));
    final Integer month = (Integer) firstDynamicMessage.getField(firstDynamicMessage.getDescriptorForType().findFieldByName("month"));
    final Integer day = (Integer) firstDynamicMessage.getField(firstDynamicMessage.getDescriptorForType().findFieldByName("day"));

    final DynamicMessage secondDynamicMessage = (DynamicMessage) assertValues.get(1);
    final Integer hours = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("hours"));
    final Integer minutes = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("minutes"));
    final Integer seconds = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("seconds"));
    final Integer nanos = (Integer) secondDynamicMessage.getField(secondDynamicMessage.getDescriptorForType().findFieldByName("nanos"));

    Assertions.assertThat(year).isEqualTo(2022);
    Assertions.assertThat(month).isEqualTo(5);
    Assertions.assertThat(day).isEqualTo(30);
    Assertions.assertThat(hours).isEqualTo(9);
    Assertions.assertThat(minutes).isEqualTo(20);
    Assertions.assertThat(seconds).isEqualTo(30);
    Assertions.assertThat(nanos).isZero();
    Assertions.assertThat(assertKeys).hasSize(2).containsExactlyInAnyOrder("abc.Incident.incident_date",
                                                                           "abc.Incident.incident_time");
  }
}
