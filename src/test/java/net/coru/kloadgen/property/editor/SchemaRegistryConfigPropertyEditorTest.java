/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.coru.kloadgen.config.schemaregistry.SchemaRegistryConfigElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchemaRegistryConfigPropertyEditorTest {

  private SchemaRegistryConfigPropertyEditor propertyEditor;

  @BeforeEach
  public void setUp() throws IntrospectionException {
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    PropertyDescriptor propertyDescriptor = new PropertyDescriptor("schemaRegistryUrl", SchemaRegistryConfigElement.class);
    propertyEditor = new SchemaRegistryConfigPropertyEditor(propertyDescriptor);
    JMeterUtils.setLocale(Locale.ENGLISH);
    JMeterUtils.getProperties("jmeter.properties");
  }

  @Test
  @DisplayName("Should Schema Registry Property Editor Initialize successfully")
  void testPropertyEditorInit() {
    assertThat(propertyEditor.getCustomEditor()).isInstanceOf(JPanel.class);

    JPanel panel = (JPanel) propertyEditor.getCustomEditor();

    assertThat(panel.getComponent(0)).isInstanceOfAny(JTextField.class);
    assertThat(panel.getComponent(1)).isInstanceOfAny(JButton.class);
  }

  @Test
  @DisplayName("Should Schema Registry Property Editor get the right value")
  void testPropertyEditorSetValue() {
    propertyEditor.setValue("http://localhost:8081");
    assertThat(propertyEditor.getValue()).isEqualTo("http://localhost:8081");
  }

}