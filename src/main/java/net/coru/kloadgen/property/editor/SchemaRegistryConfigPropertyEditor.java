/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.PropertyMapping;
import net.coru.kloadgen.util.PropsKeysHelper;
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

  private void init() {
    panel.setLayout(new BorderLayout());

    panel.add(schemaRegistryUrl);

    panel.add(testSchemaRepoBtn, BorderLayout.AFTER_LINE_ENDS);
    this.testSchemaRepoBtn.addActionListener(this);
  }

  public SchemaRegistryConfigPropertyEditor(Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public SchemaRegistryConfigPropertyEditor(PropertyDescriptor descriptor) {
    super(descriptor);
    this.init();
  }

  public void setSchemaRegistryUrl(String schemaUrl) {
    this.schemaRegistryUrl.setText(schemaUrl);
    super.setValue(schemaUrl);
  }

  @Override
  public Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public String getAsText() {
    return checkPropertyOrVariable(this.schemaRegistryUrl.getText());
  }

  @Override
  public void setDescriptor(PropertyDescriptor propertyDescriptor) {
    super.setSource(propertyDescriptor);
  }

  @Override
  public void clearGui() {
    this.schemaRegistryUrl.setText("");
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    throw new UnsupportedOperationException("Operation not Supported:" + text);
  }

  @SneakyThrows
  @Override
  public Object getValue() {
    return schemaRegistryUrl.getText();
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }

  @SneakyThrows
  @Override
  public void setValue(Object value) {
    if (Objects.nonNull(value)) {
      this.schemaRegistryUrl.setText(value.toString());
      super.setValue(value.toString());
    }

  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {

    try {
      //Get current test GUI component
      TestBeanGUI testBeanGUI = (TestBeanGUI) GuiPackage.getInstance().getCurrentGui();
      Field customizer = TestBeanGUI.class.getDeclaredField(PropsKeysHelper.CUSTOMIZER);
      customizer.setAccessible(true);

      //From TestBeanGUI retrieve Bean Customizer as it includes all editors like ClassPropertyEditor, TableEditor
      GenericTestBeanCustomizer testBeanCustomizer = (GenericTestBeanCustomizer) customizer.get(testBeanGUI);
      Field editors = GenericTestBeanCustomizer.class.getDeclaredField(PropsKeysHelper.EDITORS);
      editors.setAccessible(true);

      //Retrieve TableEditor and set all fields with default values to it
      PropertyEditor[] propertyEditors = (PropertyEditor[]) editors.get(testBeanCustomizer);
      Map<String, String> schemaProperties = new HashMap<>();
      for (PropertyEditor propertyEditor : propertyEditors) {
        if (propertyEditor instanceof TableEditor) {
          //noinspection unchecked
          schemaProperties = fromListToPropertiesMap((List<PropertyMapping>) propertyEditor.getValue());
        }
      }
      Map<String, String> originals = new HashMap<>();

      originals.put(SCHEMA_REGISTRY_URL_CONFIG, checkPropertyOrVariable(schemaRegistryUrl.getText()));
      if (FLAG_YES.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_FLAG))) {
        JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_AUTH_FLAG, FLAG_YES);
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_KEY))) {
          JMeterContextService.getContext().getProperties()
                              .setProperty(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BASIC_TYPE);

          originals.put(BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
          originals.put(USER_INFO_CONFIG, schemaProperties.get(SCHEMA_REGISTRY_USERNAME_KEY) + ":" + schemaProperties.get(SCHEMA_REGISTRY_PASSWORD_KEY));
        }
      }
      SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(List.of(getAsText()), 1000, List.of(new AvroSchemaProvider(), new JsonSchemaProvider()),
                                                                                 originals);

      List<String> subjects = new ArrayList<>(schemaRegistryClient.getAllSubjects());
      JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_URL, checkPropertyOrVariable(schemaRegistryUrl.getText()));
      JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_SUBJECTS, StringUtils.join(subjects, ","));
      if (FLAG_YES.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_FLAG))) {
        JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_AUTH_FLAG, FLAG_YES);
        if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_KEY))) {
          JMeterContextService.getContext().getProperties()
                              .setProperty(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BASIC_TYPE);
          JMeterContextService.getContext().getProperties().setProperty(BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
          JMeterContextService.getContext().getProperties().setProperty(USER_INFO_CONFIG,
                                                                        schemaProperties.get(SCHEMA_REGISTRY_USERNAME_KEY) + ":" +
                                                                        schemaProperties.get(SCHEMA_REGISTRY_PASSWORD_KEY));
        } else if (SCHEMA_REGISTRY_AUTH_BEARER_TYPE.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_KEY))) {
          JMeterContextService.getContext().getProperties()
                              .setProperty(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BEARER_TYPE);
          JMeterContextService.getContext().getProperties().setProperty(BEARER_AUTH_CREDENTIALS_SOURCE, "STATIC_TOKEN");
          JMeterContextService.getContext().getProperties()
                              .setProperty(BEARER_AUTH_TOKEN_CONFIG, schemaProperties.get(SCHEMA_REGISTRY_AUTH_BEARER_KEY));
        }
      }
      JOptionPane.showMessageDialog(null, "Successful contacting Schema Registry at : " + checkPropertyOrVariable(schemaRegistryUrl.getText()) +
                                          "\n Number of subjects in the Registry : " + subjects.size(), "Successful connection to Schema Registry",
                                    JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException | RestClientException | NoSuchFieldException | IllegalAccessException | NullPointerException e) {
      JOptionPane
          .showMessageDialog(null, "Failed retrieve schema properties: " + e.getMessage(), "ERROR: Failed to retrieve properties!",
                             JOptionPane.ERROR_MESSAGE);
      log.error(e.getMessage(), e);
    }

  }

  private Map<String, String> fromListToPropertiesMap(List<PropertyMapping> schemaProperties) {
    Map<String, String> propertiesMap = new HashMap<>();
    for (PropertyMapping property : schemaProperties) {
      propertiesMap.put(property.getPropertyName(), checkPropertyOrVariable(property.getPropertyValue()));
    }
    return propertiesMap;
  }

  private String checkPropertyOrVariable(String textToCheck) {
    if (textToCheck.matches("\\$\\{__P\\(.*\\)}")) {
      return JMeterContextService.getContext().getProperties().getProperty(textToCheck.substring(6, textToCheck.length() - 2));
    } else if (textToCheck.matches("\\$\\{\\w*}")) {
      return JMeterContextService.getContext().getVariables().get(textToCheck.substring(2, textToCheck.length() - 1));
    } else {
      return textToCheck;
    }
  }

}
