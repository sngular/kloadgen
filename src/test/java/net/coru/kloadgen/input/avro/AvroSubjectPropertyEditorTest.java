package net.coru.kloadgen.input.avro;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import net.coru.kloadgen.config.avroserialized.AvroSerializedConfigElement;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.tomakehurst.wiremock.WireMockServer;

@ExtendWith(MockitoExtension.class)
class AvroSubjectPropertyEditorTest {

  private AvroSerializedConfigElement  avroSerializedConfigElement;

  WireMockServer wireMockServer;

  @BeforeEach
  public void setup () {
    wireMockServer = new WireMockServer(8081);
    wireMockServer.start();
  }

  @Test
  public void iterationStart() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    avroSerializedConfigElement = new AvroSerializedConfigElement();
    PropertyUtils.setSimpleProperty(avroSerializedConfigElement, "schemaRegistryUrl", "http://localhost:8081");
    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    avroSerializedConfigElement.iterationStart(null);
    assertThat(variables).isNotNull();
  }
}