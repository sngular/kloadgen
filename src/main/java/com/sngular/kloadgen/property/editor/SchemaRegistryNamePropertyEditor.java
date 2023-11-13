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

@Slf4j
public class SchemaRegistryNamePropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  private JComboBox<String> schemaRegistryName;

  public SchemaRegistryNamePropertyEditor() {
    this.init();
  }

  public SchemaRegistryNamePropertyEditor(final Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public SchemaRegistryNamePropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  private void init() {
    this.schemaRegistryName = new JComboBox<>();
    this.schemaRegistryName.addItem("Apicurio");
    this.schemaRegistryName.addItem("Confluent");
    panel.setLayout(new BorderLayout());
    panel.add(schemaRegistryName);
    schemaRegistryName.addActionListener(this);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
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
    return Objects.requireNonNull(this.schemaRegistryName.getSelectedItem()).toString();
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final void setAsText(final String text) throws IllegalArgumentException {
    this.schemaRegistryName.setSelectedItem(text);
  }

  @Override
  public final void setValue(final Object value) {
    this.schemaRegistryName.setSelectedItem(Objects.requireNonNullElse(value, 0));
  }

  @Override
  public final Object getValue() {
    return this.schemaRegistryName.getSelectedItem();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }
}
