package net.coru.kloadgen.config.avroserialized;

import static net.coru.kloadgen.util.ProducerKeys.SAMPLE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Collections;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
class AvroSerializedConfigElementTest {

  @BeforeEach
  public void setUp() {
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
  }

  @Test
  public void iterationStart( @Wiremock WireMockServer server) {

    AvroSerializedConfigElement avroSerializedConfigElement = new AvroSerializedConfigElement("avrosubject",
        "http://localhost:" + server.port(), Collections.emptyList(), null);
    avroSerializedConfigElement.iterationStart(null);
    assertThat(JMeterContextService.getContext().getVariables().getObject(SAMPLE_ENTITY)).isNotNull();

  }

}