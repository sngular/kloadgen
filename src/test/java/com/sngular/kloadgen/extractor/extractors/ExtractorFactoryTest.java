package com.sngular.kloadgen.extractor.extractors;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.extractor.extractors.avro.AvroConfluentExtractor;
import com.sngular.kloadgen.extractor.extractors.avro.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.json.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.protobuff.ProtobuffExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.testutil.SchemaParseUtil;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.apache.avro.Schema;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class ExtractorFactoryTest {

    private AvroExtractor avroExtractor = new AvroExtractor();

    private static JsonExtractor jsonExtractor = new JsonExtractor();

    private static ProtobuffExtractor protobuffExtractor = new ProtobuffExtractor();

    @Captor
    private ArgumentCaptor<Schema> argumentCaptor = ArgumentCaptor.forClass(Schema.class);

    private final Properties properties = new Properties();

    private ExtractorFactory extractorFactory;

    private final FileHelper fileHelper = new FileHelper();

    private final Extractor<Schema> avroConfluentExtractor = new AvroConfluentExtractor();

    private MockedStatic<JMeterHelper> jMeterHelperMockedStatic;

    private MockedStatic<JMeterContextService> jMeterContextServiceMockedStatic;

    @BeforeEach
    public void init() {
        final File file = new File("src/test/resources");
        final String absolutePath = file.getAbsolutePath();
        JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
        final JMeterContext jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());
        JMeterUtils.setLocale(Locale.ENGLISH);

        jMeterHelperMockedStatic = Mockito.mockStatic(JMeterHelper.class);
        jMeterContextServiceMockedStatic = Mockito.mockStatic(JMeterContextService.class, Answers.RETURNS_DEEP_STUBS);
        argumentCaptor = ArgumentCaptor.forClass(Schema.class);
    }

    @AfterEach
    public void tearDown() {
        properties.clear();
        jMeterHelperMockedStatic.close();
        jMeterContextServiceMockedStatic.close();
    }

    @Test
    void flatPropertiesList() throws IOException {
        avroExtractor = Mockito.mock(AvroExtractor.class);
        ExtractorFactory.configExtractorFactory(avroExtractor, jsonExtractor, protobuffExtractor);

        final File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
        properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

        final ParsedSchema parsedSchema = SchemaParseUtil.getParsedSchema(testFile, "AVRO");
        final var baseParsedSchema = new BaseParsedSchema<>(ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema));

        jMeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
        jMeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

        final Schema schema = new Schema.Parser().parse(testFile);
        final List<FieldValueMapping> fieldValueMappingList = avroConfluentExtractor.processSchema(schema);

        when(avroExtractor.processSchema(argumentCaptor.capture(), isA(SchemaRegistryEnum.class))).thenReturn(fieldValueMappingList);
        final var result = SchemaExtractor.flatPropertiesList(schema.getName());

        Assertions.assertThat(result).isNotNull();
        assertThat(argumentCaptor.getValue()).isInstanceOf(Schema.class);
    }
}