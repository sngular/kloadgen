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
import java.beans.PropertyEditorSupport;
import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.kafka.common.serialization.Serializer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@Slf4j
public class KeySerializerPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  private JComboBox<String> serializerComboBox;

  public KeySerializerPropertyEditor() {
    this.init();
  }

  public KeySerializerPropertyEditor(final Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public KeySerializerPropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  private void init() {

    fillSerializer(new JComboBox<>());
    panel.setLayout(new BorderLayout());
    panel.add(serializerComboBox);
    serializerComboBox.addActionListener(this);
  }

  private void fillSerializer(final JComboBox<String> objectJComboBox) {
    serializerComboBox = objectJComboBox;
    final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forClass(Serializer.class)).setScanners(Scanners.SubTypes));
    ReflectionUtils.extractSerializers(serializerComboBox, reflections, Serializer.class);
  }

  @Override
  public final void actionPerformed(final ActionEvent event) {
    // Not implementation required
  }

  @Override
  public void clearGui() {
    // Not implementation required
  }

  @Override
  public final void setDescriptor(final PropertyDescriptor descriptor) {
    super.setSource(descriptor);
  }

  @Override
  public final String getAsText() {
    return Objects.requireNonNull(this.serializerComboBox.getSelectedItem()).toString();
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final void setAsText(final String text) throws IllegalArgumentException {
    this.serializerComboBox.setSelectedItem(text);
  }

  @Override
  public final void setValue(final Object value) {
    this.serializerComboBox.setSelectedItem(Objects.requireNonNullElse(value, 0));
  }

  @Override
  public final Object getValue() {
    return this.serializerComboBox.getSelectedItem();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }
}
