package net.coru.kloadgen.config.avroserialized;

import static net.coru.kloadgen.util.ProducerKeys.SAMPLE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Collections;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
  @Disabled
  public void iterationStart() {

    AvroSerializedConfigElement avroSerializedConfigElement = new AvroSerializedConfigElement("AvroSubject",
        "http://localhost:8081", Collections.emptyList(), null);
    avroSerializedConfigElement.iterationStart(null);
    assertThat(JMeterContextService.getContext().getVariables().getObject(SAMPLE_ENTITY)).isNotNull();

  }

}