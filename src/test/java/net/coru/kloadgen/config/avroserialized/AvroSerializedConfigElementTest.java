package net.coru.kloadgen.config.avroserialized;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Collections;
import net.coru.kloadgen.util.PropsKeys;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvroSerializedConfigElementTest {

  WireMockServer wireMockServer;

  @BeforeEach
  public void setUp() {
    wireMockServer = new WireMockServer(8081);
    wireMockServer.start();
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
  }

  @Test
  public void avroSerializedConfigTest() {

    AvroSerializedConfigElement avroSerializedConfigElement = new AvroSerializedConfigElement();
    avroSerializedConfigElement.setAvroSubject("AvroSubject");
    avroSerializedConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
    avroSerializedConfigElement.setSchemaProperties(Collections.emptyList());
    avroSerializedConfigElement.setSchemaRegistryUrl("http://localhost:8081");
    avroSerializedConfigElement.iterationStart(null);
    Object object = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
    DocumentContext doc = JsonPath.parse(object.toString());
    com.revinate.assertj.json.JsonPathAssert.assertThat(doc).jsonPathAsInteger("$.messageId").isGreaterThan(0);
  }

  @Test
  void iterationStart() {
  }
}