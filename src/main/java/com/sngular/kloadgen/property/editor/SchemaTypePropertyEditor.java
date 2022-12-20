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
public class SchemaTypePropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  public SchemaTypePropertyEditor() {
  }

  public SchemaTypePropertyEditor(final Object source) {
    super(source);
    this.setValue(source);
  }

  public SchemaTypePropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
  }

  @Override
  public final void actionPerformed(final ActionEvent event) {
    throw new NotImplementedException("Not implementation is required");
  }

  @Override
  public final void clearGui() {}

  @Override
  public final void setDescriptor(final PropertyDescriptor descriptor) {
    setSource(descriptor);
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final String getAsText() {
    return super.getAsText();
  }

  @Override
  public final void setAsText(final String value) throws IllegalArgumentException {
    super.setValue(value);
  }

  @Override
  public final void setValue(final Object value) {
    if (Objects.nonNull(value)) {
      super.setValue(value);
    }
  }

  @Override
  public final Object getValue() {
    return super.getValue();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }

}
