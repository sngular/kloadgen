/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import static org.reflections.scanners.Scanners.SubTypes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.kafka.common.serialization.Deserializer;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

@Slf4j
public class ValueDeserializerPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  private JComboBox<String> deserializerComboBox;

  public ValueDeserializerPropertyEditor() {
    this.init();
  }

  private void init() {

    fillDeserializer(new JComboBox<>());
    panel.setLayout(new BorderLayout());
    panel.add(deserializerComboBox);
    deserializerComboBox.addActionListener(this);
  }

  private void fillDeserializer(JComboBox<String> objectJComboBox) {
    deserializerComboBox = objectJComboBox;
    Reflections reflections = new Reflections(
        new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forClass(Deserializer.class))
            .filterInputsBy(new FilterBuilder()
                                .includePackage("net.coru.kloadgen.serializer")
                                .includePackage("io.confluent.kafka.serializers"))
            .setScanners(SubTypes));

    ReflectionUtils.extractDeserializers(deserializerComboBox, reflections, Deserializer.class);

  }

  public ValueDeserializerPropertyEditor(Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public ValueDeserializerPropertyEditor(PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    // Not implementation required
  }

  @Override
  public void clearGui() {
    // Not implementation required
  }

  @Override
  public void setDescriptor(PropertyDescriptor descriptor) {
    super.setSource(descriptor);
  }

  @Override
  public String getAsText() {
    return Objects.requireNonNull(this.deserializerComboBox.getSelectedItem()).toString();
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    this.deserializerComboBox.setSelectedItem(text);
  }

  @Override
  public Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public Object getValue() {
    return this.deserializerComboBox.getSelectedItem();
  }

  @Override
  public void setValue(Object value) {
    this.deserializerComboBox.setSelectedItem(Objects.requireNonNullElse(value, 0));
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }
}
