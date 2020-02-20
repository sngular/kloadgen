package net.coru.kloadgen.input.avro;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Locale;
import net.coru.kloadgen.config.avroserialized.AvroSerializedConfigElement;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
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
class AvroSubjectPropertyEditorTest {

  @BeforeEach
  public void setUp() {
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  public void iterationStart(@Wiremock WireMockServer server) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    AvroSerializedConfigElement avroSerializedConfigElement = new AvroSerializedConfigElement("AvroSubject",
        "http://localhost:" + server.port(), Collections.emptyList(), null);
    PropertyUtils.setSimpleProperty(avroSerializedConfigElement, "schemaRegistryUrl", "http://localhost:" + server.port());
    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    avroSerializedConfigElement.iterationStart(null);
    assertThat(variables).isNotNull();
  }
}