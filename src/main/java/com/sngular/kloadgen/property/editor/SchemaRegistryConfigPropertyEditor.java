/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.property.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.PropertyMapping;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.threads.JMeterContextService;

@Slf4j
public class SchemaRegistryConfigPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JTextField schemaRegistryUrl = new JTextField();

  private final JButton testSchemaRepoBtn = new JButton("Test Registry");

  private final JPanel panel = new JPanel();

  public SchemaRegistryConfigPropertyEditor() {
    this.init();
  }

  public SchemaRegistryConfigPropertyEditor(final Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public SchemaRegistryConfigPropertyEditor(final PropertyDescriptor descriptor) {
    super(descriptor);
    this.init();
  }

  private void init() {
    panel.setLayout(new BorderLayout());

    panel.add(schemaRegistryUrl);

    panel.add(testSchemaRepoBtn, BorderLayout.AFTER_LINE_ENDS);
    this.testSchemaRepoBtn.addActionListener(this);
  }

  public final void setSchemaRegistryUrl(final String schemaUrl) {
    this.schemaRegistryUrl.setText(schemaUrl);
    super.setValue(schemaUrl);
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final String getAsText() {
    return checkPropertyOrVariable(this.schemaRegistryUrl.getText());
  }

  @Override
  public final void setDescriptor(final PropertyDescriptor propertyDescriptor) {
    super.setSource(propertyDescriptor);
  }

  @Override
  public final void clearGui() {
    this.schemaRegistryUrl.setText("");
  }

  @Override
  public final void setAsText(final String text) throws IllegalArgumentException {
    throw new UnsupportedOperationException("Operation not Supported:" + text);
  }

  @Override
  public final Object getValue() {
    return schemaRegistryUrl.getText();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }

  @Override
  public final void setValue(final Object value) {
    if (Objects.nonNull(value)) {
      this.schemaRegistryUrl.setText(value.toString());
      super.setValue(value.toString());
    }

  }

  @Override
  public final void actionPerformed(final ActionEvent actionEvent) {

    try {
      //Get current test GUI component
      final var testBeanGUI = (TestBeanGUI) GuiPackage.getInstance().getCurrentGui();
      final var customizer = TestBeanGUI.class.getDeclaredField(PropsKeysHelper.CUSTOMIZER);
      customizer.setAccessible(true);

      //From TestBeanGUI retrieve Bean Customizer as it includes all editors like ClassPropertyEditor, TableEditor
      final var testBeanCustomizer = (GenericTestBeanCustomizer) customizer.get(testBeanGUI);
      final var editors = GenericTestBeanCustomizer.class.getDeclaredField(PropsKeysHelper.EDITORS);
      editors.setAccessible(true);

      //Retrieve TableEditor and set all fields with default values to it
      final var propertyEditors = (PropertyEditor[]) editors.get(testBeanCustomizer);
      final Map<String, String> schemaProperties = new HashMap<>();
      for (PropertyEditor propertyEditor : propertyEditors) {
        if (propertyEditor instanceof TableEditor) {
          //noinspection unchecked
          schemaProperties.putAll(fromListToPropertiesMap((List<PropertyMapping>) propertyEditor.getValue()));
        } else if (propertyEditor instanceof SchemaRegistryNamePropertyEditor) {
          schemaProperties.put(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, checkPropertyOrVariable(propertyEditor.getAsText()));
        }
      }
      final Map<String, String> originals = new HashMap<>();

      final String schemaRegistryName = schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
      JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, schemaRegistryName);

      final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(schemaRegistryName);

      originals.put(schemaRegistryManager.getSchemaRegistryUrlKey(), checkPropertyOrVariable(schemaRegistryUrl.getText()));

      if (ProducerKeysHelper.FLAG_YES.equalsIgnoreCase(schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG))) {
        JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG, ProducerKeysHelper.FLAG_YES);
        if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equalsIgnoreCase(schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE);

          originals.put(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
          originals.put(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY) + ":"
                                                                         + schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY));
        }
      }

      schemaRegistryManager.setSchemaRegistryClient(getAsText(), originals);
      final var subjects = schemaRegistryManager.getAllSubjects();

      JMeterContextService.getContext().getProperties().setProperty(schemaRegistryManager.getSchemaRegistryUrlKey(), checkPropertyOrVariable(schemaRegistryUrl.getText()));
      JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS, StringUtils.join(subjects, ","));
      if (ProducerKeysHelper.FLAG_YES.equalsIgnoreCase(schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG))) {
        JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG, ProducerKeysHelper.FLAG_YES);
        if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equalsIgnoreCase(schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE);
          JMeterContextService.getContext().getProperties().setProperty(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
          JMeterContextService.getContext().getProperties().setProperty(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG,
                                                                        schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY) + ":"
                                                                        + schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY));
        } else if (SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_TYPE.equalsIgnoreCase(schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY))) {
          JMeterContextService.getContext().getProperties().setProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_TYPE);
          JMeterContextService.getContext().getProperties().setProperty(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CREDENTIALS_SOURCE, "STATIC_TOKEN");
          JMeterContextService.getContext().getProperties()
                              .setProperty(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_TOKEN_CONFIG, schemaProperties.get(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY));
        }
      }
      JOptionPane.showMessageDialog(null, "Successful contacting Schema Registry at : " + checkPropertyOrVariable(schemaRegistryUrl.getText())
                                          + "\n Number of subjects in the Registry : " + subjects.size(), "Successful connection to Schema Registry",
                                    JOptionPane.INFORMATION_MESSAGE);
    } catch (NoSuchFieldException | IllegalAccessException | NullPointerException | KLoadGenException e) {
      JOptionPane.showMessageDialog(null, "Failed retrieve schema properties: " + e.getMessage(), "ERROR: Failed to retrieve properties!", JOptionPane.ERROR_MESSAGE);
      log.error(e.getMessage(), e);
    }

  }

  private Map<String, String> fromListToPropertiesMap(final List<PropertyMapping> schemaProperties) {
    final Map<String, String> propertiesMap = new HashMap<>();
    for (PropertyMapping property : schemaProperties) {
      propertiesMap.put(property.getPropertyName(), checkPropertyOrVariable(property.getPropertyValue()));
    }
    return propertiesMap;
  }

  private String checkPropertyOrVariable(final String textToCheck) {
    final String result;
    if (textToCheck.matches("\\$\\{__P\\(.*\\)}")) {
      result = JMeterContextService.getContext().getProperties().getProperty(textToCheck.substring(6, textToCheck.length() - 2));
    } else if (textToCheck.matches("\\$\\{\\w*}")) {
      result = JMeterContextService.getContext().getVariables().get(textToCheck.substring(2, textToCheck.length() - 1));
    } else {
      result = textToCheck;
    }
    return result;
  }

}
