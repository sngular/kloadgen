package com.sngular.kloadgen.extractor.asyncapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

class AsyncApiExtractorImplTest {

  private final ObjectMapper om = new ObjectMapper(new YAMLFactory());

  private final FileHelper fileHelper = new FileHelper();

  @BeforeEach
  public final void setUp() {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should extract asyncapi file")
  void testExtractFile() throws IOException {
    final File testFile = fileHelper.getFile("/asyncapi/event-api.yml");
    final AsyncApiFile asapfle = new AsyncApiExtractorImpl().processFile(testFile);

    org.junit.jupiter.api.Assertions.assertNotNull(asapfle);
    org.junit.jupiter.api.Assertions.assertEquals("{user_signedup=user_signedup}", asapfle.getApiSchemaList().toString());
    org.junit.jupiter.api.Assertions.assertEquals("{production=AsyncApiServer(name=production, url=mqtt://test.mosquitto.org, protocol=mqtt, description=Test MQTT broker)}",
                                                  asapfle.getApiServerMap().toString());
  }

  @Test
  @DisplayName("Should extract basic asyncapi schema")
  void testExtractAsyncapiSchema() throws IOException {
    final File testFile = fileHelper.getFile("/asyncapi/event-api.yml");
    final JsonNode openApi = om.readTree(testFile);
    final AsyncApiFile asapfle = new AsyncApiExtractorImpl().processNode(openApi);
    final JsonNode propertiesSchema = asapfle.getAsyncApiFileNode().path("components").path("schemas").path("userSignedUpPayload").path("properties");
    Assertions.assertThat(asapfle).isNotNull();
    Assertions.assertThat(propertiesSchema)
              .hasSize(4)
              .isSubsetOf(openApi.get("components").get("schemas").get("userSignedUpPayload").get("properties"));
  }

}