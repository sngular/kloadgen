/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.property.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.Objects;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;

@Slf4j
public class SchemaConverterPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  private PropertyDescriptor propertyDescriptor;

  private String schemaAsString;

  public SchemaConverterPropertyEditor() {
  }

  public SchemaConverterPropertyEditor(final Object source) {
    super(source);
    this.setValue(source);
  }

  public SchemaConverterPropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.propertyDescriptor = propertyDescriptor;
  }

  @Override
  public final void actionPerformed(final ActionEvent event) {
    throw new NotImplementedException("Not implementation is required");
  }

  @Override
  public final void clearGui() {
  }

  @Override
  public final void setDescriptor(final PropertyDescriptor descriptor) {
    propertyDescriptor = descriptor;
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final String getAsText() {
    return schemaAsString;
  }

  @Override
  public final void setAsText(final String value) throws IllegalArgumentException {
    propertyDescriptor.setValue("schemaAsString", value);
    schemaAsString = value;
  }

  @Override
  public final void setValue(final Object value) {
    if (Objects.nonNull(value)) {
      setAsText(value.toString());
    }
  }

  @Override
  public final Object getValue() {
    return getAsText();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }

}
