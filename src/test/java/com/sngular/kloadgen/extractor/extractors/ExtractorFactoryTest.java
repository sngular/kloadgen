package com.sngular.kloadgen.extractor.extractors;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.extractor.extractors.avro.AvroConfluentExtractor;
import com.sngular.kloadgen.extractor.extractors.avro.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.json.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.protobuf.ProtobufExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.avro.Schema;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
class ExtractorFactoryTest {

  @Captor
  private ArgumentCaptor<Schema> argumentCaptor = ArgumentCaptor.forClass(Schema.class);

  private final Properties properties = new Properties();

  private final Extractor<Schema> avroConfluentExtractor = new AvroConfluentExtractor();

  private MockedStatic<JMeterHelper> jmeterHelperMockedStatic;

  private MockedStatic<JMeterContextService> jmeterContextServiceMockedStatic;

  @BeforeEach
  public void init() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);

    jmeterHelperMockedStatic = Mockito.mockStatic(JMeterHelper.class);
    jmeterContextServiceMockedStatic = Mockito.mockStatic(JMeterContextService.class, Answers.RETURNS_DEEP_STUBS);
    argumentCaptor = ArgumentCaptor.forClass(Schema.class);
  }

  @AfterEach
  public void tearDown() {
    properties.clear();
    jmeterHelperMockedStatic.close();
    jmeterContextServiceMockedStatic.close();
  }

  @Test
  void flatPropertiesList() throws IOException {
    final AvroExtractor avroExtractor = Mockito.mock(AvroExtractor.class);
    ExtractorFactory.configExtractorFactory(avroExtractor, new JsonExtractor(), new ProtobufExtractor());

    final File testFile = new FileHelper().getFile("/avro-files/embedded-avros-example-test.avsc");
    properties.setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, SchemaRegistryEnum.CONFLUENT.toString());

    final ParsedSchema parsedSchema = new ParsedSchema(testFile, "AVRO");
    final var baseParsedSchema = new BaseParsedSchema<>(ConfluentAbstractParsedSchemaMetadata.parse(parsedSchema));

    jmeterHelperMockedStatic.when(() -> JMeterHelper.getParsedSchema(Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(baseParsedSchema);
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);

    final Schema schema = new Schema.Parser().parse(testFile);
    final List<FieldValueMapping> fieldValueMappingList = avroConfluentExtractor.processSchema(schema);

    Mockito.when(avroExtractor.processSchema(new ParsedSchema(argumentCaptor.capture(), ""), ArgumentMatchers.isA(SchemaRegistryEnum.class))).thenReturn(fieldValueMappingList);
    final var result = SchemaExtractor.flatPropertiesList(schema.getName());

    Assertions.assertThat(result).isNotNull();
  }
}