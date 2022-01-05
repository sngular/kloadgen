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
import org.apache.avro.generic.GenericRecord;
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
    void testExternalImportsProcessor() throws IOException {
        File testFile = fileHelper.getFile("/proto-files/externalTypesTest.proto");
        List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "PROTOBUF"));
        ProtobufSchemaProcessor protobufSchemaProcessor = new ProtobufSchemaProcessor();
        protobufSchemaProcessor.processSchema(schemaExtractor.schemaTypesList(testFile,"Protobuf"), new SchemaMetadata(1,1,""), fieldValueMappingList);
        EnrichedRecord message = protobufSchemaProcessor.next();
        System.out.println(message);
    }
}
