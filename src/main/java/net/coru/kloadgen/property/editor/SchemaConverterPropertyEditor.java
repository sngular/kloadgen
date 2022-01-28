/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

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

  private PropertyDescriptor propertyDescriptor;

  private final JPanel panel = new JPanel();

  private String schemaAsString;

  public SchemaConverterPropertyEditor() {
  }

  public SchemaConverterPropertyEditor(Object source) {
    super(source);
    this.setValue(source);
  }

  public SchemaConverterPropertyEditor(PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.propertyDescriptor = propertyDescriptor;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    throw new NotImplementedException("Not implementation is required");
  }

  @Override
  public void clearGui() {}

  @Override
  public Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public void setDescriptor(PropertyDescriptor descriptor) {
    propertyDescriptor = descriptor;
  }

  @Override
  public String getAsText() {
    return schemaAsString;
  }

  @Override
  public void setAsText(String value) throws IllegalArgumentException {
    propertyDescriptor.setValue("schemaAsString" , value);
    schemaAsString = value;
  }

  @Override
  public void setValue(Object value) {
    if (Objects.nonNull(value)) {
      setAsText(value.toString());
    }
  }

  @Override
  public Object getValue() {
    return schemaAsString;
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }

}
