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
import org.apache.kafka.common.serialization.Serializer;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@Slf4j
public class KeySerializerPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  private JComboBox<String> serializerComboBox;

  public KeySerializerPropertyEditor() {
    this.init();
  }

  public KeySerializerPropertyEditor(Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public KeySerializerPropertyEditor(PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  private void init() {

    fillSerializer(new JComboBox<>());
    panel.setLayout(new BorderLayout());
    panel.add(serializerComboBox);
    serializerComboBox.addActionListener(this);
  }

  private void fillSerializer(JComboBox<String> objectJComboBox) {
    serializerComboBox = objectJComboBox;
    Reflections reflections = new Reflections(
        new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forClass(Serializer.class))
            .setScanners(SubTypes));
    ReflectionUtils.extractSerializers(serializerComboBox, reflections, Serializer.class);
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
    return Objects.requireNonNull(this.serializerComboBox.getSelectedItem()).toString();
  }

  @Override
  public Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    this.serializerComboBox.setSelectedItem(text);
  }

  @Override
  public void setValue(Object value) {
    this.serializerComboBox.setSelectedItem(Objects.requireNonNullElse(value, 0));
  }

  @Override
  public Object getValue() {
    return this.serializerComboBox.getSelectedItem();
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }
}
