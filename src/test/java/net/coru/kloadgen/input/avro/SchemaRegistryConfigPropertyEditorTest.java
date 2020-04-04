package net.coru.kloadgen.input.avro;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
class SchemaRegistryConfigPropertyEditorTest {

  private SchemaRegistryConfigPropertyEditor propertyEditor;

  private JMeterContext jmcx;

  @BeforeEach
  public void setUp() {
    jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    propertyEditor = new SchemaRegistryConfigPropertyEditor();
    JMeterUtils.setLocale(Locale.ENGLISH);
    JMeterUtils.getProperties("jmeter.properties");
  }

  @Test
  public void testPropertyEditorInit() {
    assertThat(propertyEditor.getCustomEditor()).isInstanceOf(JPanel.class);

    JPanel panel = (JPanel) propertyEditor.getCustomEditor();

    assertThat(panel.getComponent(0)).isInstanceOfAny(JTextField.class);
    assertThat(panel.getComponent(1)).isInstanceOfAny(JButton.class);
  }

  @Test
  public void testPropertyEditorSetValue() {
    propertyEditor.setValue("http://localhost:8081");
    assertThat(propertyEditor.getValue()).isEqualTo("http://localhost:8081");
  }

}