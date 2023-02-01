package com.sngular.kloadgen.sampler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.extractor.impl.SchemaExtractorImpl;
import com.sngular.kloadgen.loadgen.impl.AvroSRLoadGenerator;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SamplerUtilTest {

  private final FileHelper fileHelper = new FileHelper();

  private final SchemaExtractor extractor = new SchemaExtractorImpl();

  private static Stream<Object> parametersForConfigureValueGeneratorTest() {
    return Stream.of("localhost:8081", "");
  }

  @BeforeEach
  public final void setUp() throws IOException {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(getVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  public JMeterVariables getVariables() throws IOException {
    final File testFile = fileHelper.getFile("/avro-files/avros-example-with-sub-entity-array-test.avsc");
    final ParsedSchema parsedSchema = extractor.schemaTypesList(testFile, "AVRO");
    var variables = new JMeterVariables();
    variables.put(PropsKeysHelper.KEY_SCHEMA_TYPE, "avro");
    variables.put(PropsKeysHelper.VALUE_SUBJECT_NAME, "test");
    variables.put(PropsKeysHelper.VALUE_SCHEMA, String.valueOf(parsedSchema));
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, Arrays.asList(
      FieldValueMapping.builder().fieldName("subEntity.anotherLevel.subEntityIntArray[2]").fieldType("int-array").valueLength(0).fieldValueList("[1]").required(true)
                       .isAncestorRequired(true).build(),
      FieldValueMapping.builder().fieldName("topLevelIntArray[3]").fieldType("int-array").valueLength(0).fieldValueList("[2]").required(true).isAncestorRequired(true).build())
    );
    return variables;
  }

  @ParameterizedTest
  @MethodSource("parametersForConfigureValueGeneratorTest")
  void configureValueGeneratorTest(String jmeterProps) {
    Properties props = new Properties();

    props.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, jmeterProps);

    var result = SamplerUtil.configureValueGenerator(props);
    Assertions.assertThat(result).isInstanceOf(AvroSRLoadGenerator.class);
  }
}
