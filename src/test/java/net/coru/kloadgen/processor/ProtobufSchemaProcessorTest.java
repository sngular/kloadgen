package net.coru.kloadgen.processor;

import com.google.protobuf.Descriptors;
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

public class ProtobufSchemaProcessorTest {

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
    void textProtoBufSchemaProcessor() throws KLoadGenException, IOException {
        File testFile = fileHelper.getFile("/proto-files/embeddedTypeTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        fieldValueMappingList.forEach(field -> System.out.println(field +"\r\n"));
        ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
        protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile,"Protobuf"), new SchemaMetadata(1,1,""), fieldValueMappingList);
        EnrichedRecord message = protobufSchemaProcessor.next();
        assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
        assertThat(message.getGenericRecord()).isNotNull();
    }

    @Test
    void testProtoBufEnumSchemaProcessor() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/enumTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        fieldValueMappingList.get(0).setFieldValuesList("HOME, WORK");
        ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
        protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile,"Protobuf"), new SchemaMetadata(1,1,""), fieldValueMappingList);
        EnrichedRecord message = protobufSchemaProcessor.next();
        DynamicMessage genericRecord = (DynamicMessage) message.getGenericRecord();
        Map<Descriptors.FieldDescriptor, Object> map = genericRecord.getAllFields();
        List<String> assertKeys = new ArrayList<>();
        map.forEach((key, value) -> assertKeys.add(key.getFullName()));
        assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
        assertThat(message.getGenericRecord()).isNotNull();
        assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.phoneTypes", "tutorial.Person.phoneTypesArray", "tutorial.Person.phoneTypesMap");
    }
    @Test
    void testProtoBufEasyTestProcessor() throws IOException {
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
        assertThat(message).isNotNull()
                .isInstanceOf(EnrichedRecord.class)
                .extracting(EnrichedRecord::getGenericRecord)
                .isNotNull();
        assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Address.street", "tutorial.Address.number", "tutorial.Address.zipcode");
        assertThat(assertValues).hasSize(3);
        assertThat(assertValues.get(0)).isInstanceOf(String.class);
        List<Object> integerList = (List<Object>) assertValues.get(1);
        assertThat(integerList.get(0)).isInstanceOf(Integer.class);
        assertThat(assertValues.get(2)).isInstanceOf(Long.class);

    }

    @Test
    void testProtoBufMapTestProcessor() throws IOException {
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

        assertThat(message).isNotNull()
                .isInstanceOf(EnrichedRecord.class)
                .extracting(EnrichedRecord::getGenericRecord)
                .isNotNull();
        assertThat(assertKeys).hasSize(3).containsExactlyInAnyOrder("tutorial.Person.name","tutorial.Person.addresses","tutorial.Person.addressesNoDot");
        assertThat(assertValues).hasSize(3);
        List<Object> objectList = (List<Object>) assertValues.get(0);
        DynamicMessage dynamicMessage = (DynamicMessage) objectList.get(0);
        String personName = (String) dynamicMessage.getField(dynamicMessage.getDescriptorForType().findFieldByName("value"));
        assertThat(personName).isEqualTo("Pablo");
    }
    
}
