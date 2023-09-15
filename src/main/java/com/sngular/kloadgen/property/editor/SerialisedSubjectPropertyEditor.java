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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.util.AutoCompletion;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.threads.JMeterContextService;

@Slf4j
public class SerialisedSubjectPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JButton loadClassBtn = new JButton("Load Subject");

  private final JPanel panel = new JPanel();

  private JComboBox<String> subjectNameComboBox;

  public SerialisedSubjectPropertyEditor() {
    this.init();
  }

  public SerialisedSubjectPropertyEditor(final Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public SerialisedSubjectPropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  private void init() {
    subjectNameComboBox = new JComboBox<>();
    panel.setLayout(new BorderLayout());
    panel.add(subjectNameComboBox);
    panel.add(loadClassBtn, BorderLayout.AFTER_LINE_ENDS);
    AutoCompletion.enable(subjectNameComboBox);
    this.loadClassBtn.addActionListener(this);
  }

  @Override
  public final void actionPerformed(final ActionEvent event) {
    final var subjectName = Objects.requireNonNull(this.subjectNameComboBox.getSelectedItem()).toString();

    try {
      final var attributeList = SchemaExtractor.flatPropertiesList(subjectName);

      final var testBeanGUI = (TestBeanGUI) GuiPackage.getInstance().getCurrentGui();
      final var customizer = TestBeanGUI.class.getDeclaredField(PropsKeysHelper.CUSTOMIZER);
      customizer.setAccessible(true);

      final var testBeanCustomizer = (GenericTestBeanCustomizer) customizer.get(testBeanGUI);
      final var editors = GenericTestBeanCustomizer.class.getDeclaredField(PropsKeysHelper.EDITORS);
      editors.setAccessible(true);

      final var propertyEditors = (PropertyEditor[]) editors.get(testBeanCustomizer);
      for (PropertyEditor propertyEditor : propertyEditors) {
        if (propertyEditor instanceof TableEditor) {
          final var tableEditor = (TableEditor) propertyEditor;
          if (tableEditor.getValue() instanceof List) {
            propertyEditor.setValue(mergeValue((List<FieldValueMapping>) tableEditor.getValue(), attributeList.getRight()));
          }
        } else if (propertyEditor instanceof SchemaTypePropertyEditor) {
          propertyEditor.setValue(attributeList.getKey());
        }
      }
      JOptionPane.showMessageDialog(null, "Successful retrieving of subject : " + subjectName, "Successful retrieving properties",
                                    JOptionPane.INFORMATION_MESSAGE);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      JOptionPane.showMessageDialog(null, "Failed retrieve schema properties : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
                                    JOptionPane.ERROR_MESSAGE);
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public final void clearGui() {

  }

  @Override
  public final void setDescriptor(final PropertyDescriptor descriptor) {
    super.setSource(descriptor);
  }

  @Override
  public final String getAsText() {
    return Objects.requireNonNull(this.subjectNameComboBox.getSelectedItem()).toString();
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final void setAsText(final String text) throws IllegalArgumentException {
    if (this.subjectNameComboBox.getModel().getSize() == 0) {
      this.subjectNameComboBox.addItem(text);
    }
    this.subjectNameComboBox.setSelectedItem(text);
  }

  @Override
  public final void setValue(final Object value) {
    final var subjects = JMeterContextService.getContext().getProperties().getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS);
    if (Objects.nonNull(subjects)) {
      final var subjectsList = subjects.split(",");
      subjectNameComboBox.setModel(new DefaultComboBoxModel<>(subjectsList));
    }
    if (value != null) {
      if (this.subjectNameComboBox.getModel().getSize() == 0) {
        this.subjectNameComboBox.addItem((String) value);
      }
      this.subjectNameComboBox.setSelectedItem(value);
    } else {
      this.subjectNameComboBox.setSelectedItem("");
    }

  }

  @Override
  public final Object getValue() {
    return this.subjectNameComboBox.getSelectedItem();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }

  final List<FieldValueMapping> mergeValue(final List<FieldValueMapping> tableEditorValue, final List<FieldValueMapping> attributeList) {
    final var result = new ArrayList<FieldValueMapping>();
    for (final var value : attributeList) {
      final var fFieldValueMapping = IterableUtils.find(tableEditorValue, fieldValueMapping -> compare(value).evaluate(fieldValueMapping));
      if (Objects.isNull(fFieldValueMapping)) {
        result.add(value);
      } else {
        result.add(fFieldValueMapping);
      }
    }
    return result;
  }

  private Predicate<FieldValueMapping> compare(final FieldValueMapping fieldValue2) {
    return fieldValue -> fieldValue.getFieldName().equalsIgnoreCase(fieldValue2.getFieldName())
                         && fieldValue.getFieldType().equalsIgnoreCase(fieldValue2.getFieldType())
                        && fieldValue.getRequired().equals(fieldValue2.getRequired());
  }

}
