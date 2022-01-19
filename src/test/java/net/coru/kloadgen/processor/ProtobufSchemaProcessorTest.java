package net.coru.kloadgen.processor;

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
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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
    void textEmbeddedTypeTestSchemaProcessor() throws KLoadGenException, IOException, DescriptorValidationException {
        File testFile = fileHelper.getFile("/proto-files/embeddedTypeTest.proto");
        List<FieldValueMapping> fieldValueMappingList = List.of(
            new FieldValueMapping("Person.phones[1:].addressesPhone[1:].id[1]", "string-array", 0, "Pablo"));
        ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
        protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile,"Protobuf"), new SchemaMetadata(1,1,""), fieldValueMappingList);
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
        assertThat(assertKeys).hasSize(1).containsExactlyInAnyOrder( "tutorial.Person.phones");
        assertThat(idField).isEqualTo("[Pablo]");
    }

    @Test
    void testProtoBufEnumSchemaProcessor() throws IOException, DescriptorValidationException {
        File testFile = fileHelper.getFile("/proto-files/enumTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        fieldValueMappingList.get(0).setFieldValuesList("HOME, WORK");
        ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
        protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile,"Protobuf"), new SchemaMetadata(1,1,""), fieldValueMappingList);
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
        assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.phoneTypes", "tutorial.Person.phoneTypesArray", "tutorial.Person.phoneTypesMap");
    }
    @Test
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
    void testProtoBufMapTestProcessor() throws IOException, DescriptorValidationException {
        File testFile = fileHelper.getFile("/proto-files/mapTest.proto");
        List<FieldValueMapping> fieldValueMappingList = asList(
                new FieldValueMapping("Person.name[:]", "string-map", 0, "Pablo"),
                new FieldValueMapping("Person.addresses[:].street", "string", 0, "Sor Joaquina"),
                new FieldValueMapping("Person.addresses[:].number", "int", 0, "2"),
                new FieldValueMapping("Person.addresses[:].zipcode", "int", 0, "15011"),
                new FieldValueMapping("Person.addressesNoDot[:].street", "string", 0, "Sor Joaquina"),
                new FieldValueMapping("Person.addressesNoDot[:].number", "int", 0, "6"),
                new FieldValueMapping("Person.addressesNoDot[:].zipcode", "int", 0, "15011"));
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
        assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.name","tutorial.Person.addresses","tutorial.Person.addressesNoDot");
        assertThat(assertValues).hasSize(3);
        assertThat(personName).isEqualTo("Pablo");
        assertThat(street).isInstanceOf(String.class).isEqualTo("Sor Joaquina");
        assertThat(number).isInstanceOf(Integer.class).isEqualTo(2);
        assertThat(zipcode).isInstanceOf(Integer.class).isEqualTo(15011);
    }

    @Test
    void testProtoBufComplexTestProcessor() throws IOException, DescriptorValidationException {
        File testFile = fileHelper.getFile("/proto-files/complexTest.proto");
        List<FieldValueMapping> fieldValueMappingList = asList(
                new FieldValueMapping("Test.phone_types[].phone", "long", 0, ""),
                new FieldValueMapping("Test.phone_types[].principal", "boolean", 0, ""),
                new FieldValueMapping("Test.name", "string", 0, ""),
                new FieldValueMapping("Test.age", "int", 0, ""),
                new FieldValueMapping("Test.address[].street[]", "string-array", 0, ""),
                new FieldValueMapping("Test.address[].number_street", "int", 0, ""),
                new FieldValueMapping("Test.pets[:].pet_name", "string", 0, ""),
                new FieldValueMapping("Test.pets[:].pet_age", "int", 0, ""),
                new FieldValueMapping("Test.pets[:].owner", "string", 0, ""),
                new FieldValueMapping("Test.descriptors[:]", "string-map", 0, ""),
                new FieldValueMapping("Test.dates[]", "string-array", 0, ""),
                new FieldValueMapping("Test.response", "string", 0, ""),
                new FieldValueMapping("Test.presents[:].options[]", "string-array", 0, ""));
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
        assertThat(assertKeys).hasSize(8).containsExactlyInAnyOrder("tutorial.Test.phone_types","tutorial.Test.age","tutorial.Test.address","tutorial.Test.pets", "tutorial.Test.descriptors", "tutorial.Test.dates","tutorial.Test.response","tutorial.Test.presents");
        assertThat(assertValues).hasSize(8).isNotNull();
        assertThat(assertValues.get(1)).isInstanceOf(Integer.class);
        assertThat(assertValues.get(6)).isInstanceOf(String.class);
    }

    private Object getSubFieldForMapTestProcessor(DynamicMessage dynamicMessage, String field) {
        DynamicMessage subDynamicMessage = (DynamicMessage) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
        return subDynamicMessage.getField(subDynamicMessage.getDescriptorForType().findFieldByName(field));
    }

    private String getPersonNameForMapTestProcessor(List<Object> assertValues) {
        List<Object> objectList = (List<Object>) assertValues.get(0);
        DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
        return (String) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
    }

    private String getIdFieldForEmbeddedTypeTest(List<Object> assertValues) {
        List<Object> objectList = (List<Object>) assertValues.get(0);
        DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
        DynamicMessage firstMap = (DynamicMessage) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
        List<Object> secondMap = (List<Object>) firstMap.getField(firstMap.getDescriptorForType().findFieldByName("addressesPhone"));
        DynamicMessage secondMapAsDynamicField = (DynamicMessage) secondMap.get(0);
        Object thirdArray = secondMapAsDynamicField.getField(secondMapAsDynamicField.getDescriptorForType().findFieldByName("value"));
        String idField= ((DynamicMessage) thirdArray).getField(((DynamicMessage) thirdArray).getDescriptorForType().findFieldByName("id")).toString();
        return idField;
    }

}
