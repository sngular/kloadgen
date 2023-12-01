/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.sampler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.model.PropertyMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AsyncApiSamplerTest {

  private static AsyncApiFile asyncApiFileNode;

  private final FileHelper fileHelper = new FileHelper();

  private final Properties properties = new Properties();

  private final KafkaProducer kafkaProducer = Mockito.mock(KafkaProducer.class);

  private final Entry entry = new Entry();

  private final ObjectMapper om = new ObjectMapper(new YAMLFactory());

  private JMeterContext jmcx;

  private AsyncApiSampler sampler = new AsyncApiSampler();

  //@Captor
  //private ArgumentCaptor<Schema> argumentCaptor = ArgumentCaptor.forClass(Schema.class);

  private MockedStatic<JMeterContextService> jmeterContextServiceMockedStatic;

  @BeforeEach
  public final void setUp() throws IOException {
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
    sampler = new AsyncapiSamplerWrapper(kafkaProducer);
    final var testFile = fileHelper.getContent("/asyncapi/event-api.yml");
    final JsonNode openApi = om.readTree(testFile);
    asyncApiFileNode = new AsyncApiExtractorImpl().processNode(openApi);
    jmeterContextServiceMockedStatic = Mockito.mockStatic(JMeterContextService.class, Answers.RETURNS_DEEP_STUBS);

  }

  @AfterEach
  public void tearDown() {
    properties.clear();
    kafkaProducer.close();
    jmeterContextServiceMockedStatic.close();
  }

  /*@Test
  void testAsyncApiSampleBasic() {
    sampler.setAsyncApiSchemaName(asyncApiFileNode.getAsyncApiFileNode().path("channels").toString().split("\"")[1]);
    sampler.setBrokerConfiguration(getMappings());
    sampler.setSchemaFieldConfiguration(getListFieldValueMapping());
    sampler.setAsyncApiFileNode(asyncApiFileNode.getAsyncApiFileNode());

    Mockito.when(kafkaProducer.send(new ProducerRecord<>("user_signup", Mockito.any()))).thenCallRealMethod();
    jmeterContextServiceMockedStatic.when(() -> JMeterContextService.getContext().getProperties()).thenReturn(properties);
    //Aquí no sé como relacionar lo que hace el when con el método sample
    SampleResult samp = sampler.sample(entry);

    Assertions.assertThat(samp).isNotNull()
              .isEqualTo(" ");
  }*/

  public static List<PropertyMapping> getMappings() {

    List<PropertyMapping> mappings = new ArrayList<>();
    JsonNode serversNode = asyncApiFileNode.getAsyncApiFileNode().path("servers");
    if (serversNode != null) {
      Iterator<JsonNode> serverNodes = serversNode.elements();
      while (serverNodes.hasNext()) {
        JsonNode serverNode = serverNodes.next();
        String serverUrl = serverNode.get("url").asText();
        String serverProtocol = serverNode.get("protocol").asText();
        String serverDescription = serverNode.get("description").asText();

        mappings.add(new PropertyMapping("url", serverUrl));
        mappings.add(new PropertyMapping("protocol", serverProtocol));
        mappings.add(new PropertyMapping("description", serverDescription));
      }
    }
    return mappings;
  }

  public static List<FieldValueMapping> getListFieldValueMapping() {

    int i = 0;
    String field = null;
    List<FieldValueMapping> fieldValueMappings = new ArrayList<>();
    JsonNode valueFields = asyncApiFileNode.getAsyncApiFileNode().path("components")
                                           .path("schemas")
                                           .path("userSignedUpPayload")
                                           .path("properties");
    if (valueFields != null) {
      Iterator<JsonNode> fieldValuesNodes = valueFields.elements();
      while (fieldValuesNodes.hasNext()) {
        JsonNode fieldValuesNode = fieldValuesNodes.next();
        String type = fieldValuesNode.get("type").toString().replaceAll("\"", "");
        type = type.replaceAll("'", "");
        String value = fieldValuesNode.get("description").toString().replaceAll("\"", "");
        value = "[" + value.replaceAll("'", "") + "]";

        switch (i) {
          case 0:
            field = "firstName";
            break;
          case 1:
            field = "lastName";
            break;
          case 2:
            field = "email";
            break;
          case 3:
            field = "date";
            break;
          default:
            break;
        }

        fieldValueMappings.add(new FieldValueMapping(field, type, 0, value, null, null, null));

        i++;
      }
    }
    return fieldValueMappings;
  }


}