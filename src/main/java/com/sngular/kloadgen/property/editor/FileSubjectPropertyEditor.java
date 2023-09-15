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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.util.AutoCompletion;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.apache.commons.compress.utils.Lists;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;

@Slf4j
public class FileSubjectPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  public static final String ERROR_FAILED_TO_RETRIEVE_PROPERTIES = "ERROR: Failed to retrieve properties!";

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final JPanel panel = new JPanel();

  private final JButton openFileDialogButton = new JButton(JMeterUtils.getResString("file_visualizer_open"));

  private List<String> parserSchema;

  private JComboBox<String> schemaTypeComboBox;

  private JComboBox<String> subjectNameComboBox;

  public FileSubjectPropertyEditor() {
    this.init();
  }

  public FileSubjectPropertyEditor(final Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public FileSubjectPropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  private void init() {
    schemaTypeComboBox = new JComboBox<>();
    schemaTypeComboBox.setEditable(false);
    schemaTypeComboBox.insertItemAt(SchemaTypeEnum.AVRO.name(), 0);
    schemaTypeComboBox.insertItemAt(SchemaTypeEnum.JSON.name(), 1);
    schemaTypeComboBox.insertItemAt(SchemaTypeEnum.PROTOBUF.name(), 2);
    schemaTypeComboBox.setSelectedIndex(0);
    subjectNameComboBox = new JComboBox<>();
    subjectNameComboBox.setEditable(true);
    panel.setLayout(new BorderLayout());
    openFileDialogButton.addActionListener(this::actionFileChooser);
    panel.add(openFileDialogButton, BorderLayout.LINE_END);
    panel.add(subjectNameComboBox);
    panel.add(schemaTypeComboBox);
    AutoCompletion.enable(subjectNameComboBox);
    this.subjectNameComboBox.addActionListener(this);
  }

  public final void actionFileChooser(final ActionEvent event) {

    final int returnValue = fileChooser.showDialog(panel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      final File subjectName = Objects.requireNonNull(fileChooser.getSelectedFile());
      try {
        final String schemaType = schemaTypeComboBox.getSelectedItem().toString();
        parserSchema = SchemaExtractor.schemaTypesList(subjectName, schemaType, "CONFLUENT"); //TODO CHANGE
        subjectNameComboBox.removeAllItems();
        subjectNameComboBox.addItem(parserSchema.get(0));
        subjectNameComboBox.setSelectedItem(parserSchema.get(0));
      } catch (final IOException e) {
        JOptionPane.showMessageDialog(panel, "Can't read a file : " + e.getMessage(), ERROR_FAILED_TO_RETRIEVE_PROPERTIES,
                                      JOptionPane.ERROR_MESSAGE);
        log.error(e.getMessage(), e);
      }
      subjectNameComboBox.addFocusListener(new ComboFiller());
    }
  }

  public final String getSelectedSchema(final String name) {
    return parserSchema.indexOf(name) > 0 ? parserSchema.get(parserSchema.indexOf(name)) : null;
  }

  public final List<FieldValueMapping> getAttributeList(final ParsedSchema selectedSchema) {
    final List<FieldValueMapping> result = new ArrayList<>();
    if (Objects.nonNull(selectedSchema)) {
      result.addAll(SchemaExtractor.flatPropertiesList(selectedSchema));
    }
    return result;
  }

  @Override
  public final void actionPerformed(final ActionEvent event) {

    if (subjectNameComboBox.getItemCount() != 0) {
      final String schemaType = schemaTypeComboBox.getSelectedItem().toString();
      final String selectedItem = (String) subjectNameComboBox.getSelectedItem();
      final String selectedSchema = getSelectedSchema(selectedItem);
      final List<FieldValueMapping> attributeList = Lists.newArrayList();

      if (!attributeList.isEmpty()) {
        try {
          //Get current test GUI component
          final TestBeanGUI testBeanGUI = (TestBeanGUI) GuiPackage.getInstance().getCurrentGui();
          final Field customizer = TestBeanGUI.class.getDeclaredField(PropsKeysHelper.CUSTOMIZER);
          customizer.setAccessible(true);

          //From TestBeanGUI retrieve Bean Customizer as it includes all editors like ClassPropertyEditor, TableEditor
          final GenericTestBeanCustomizer testBeanCustomizer = (GenericTestBeanCustomizer) customizer.get(testBeanGUI);
          final Field editors = GenericTestBeanCustomizer.class.getDeclaredField(PropsKeysHelper.EDITORS);
          editors.setAccessible(true);

          //Retrieve TableEditor and set all fields with default values to it
          final PropertyEditor[] propertyEditors = (PropertyEditor[]) editors.get(testBeanCustomizer);
          for (PropertyEditor propertyEditor : propertyEditors) {
            if (propertyEditor instanceof TableEditor) {
              propertyEditor.setValue(attributeList);
            } else if (propertyEditor instanceof SchemaConverterPropertyEditor) {
              propertyEditor.setValue(selectedSchema);
            } else if (propertyEditor instanceof SchemaTypePropertyEditor) {
              propertyEditor.setValue(schemaType);
            }
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          JOptionPane
              .showMessageDialog(panel, "Failed to retrieve schema : " + e.getMessage(), ERROR_FAILED_TO_RETRIEVE_PROPERTIES,
                                 JOptionPane.ERROR_MESSAGE);
          log.error(e.getMessage(), e);
        } catch (final AvroRuntimeException ex) {
          JOptionPane
              .showMessageDialog(panel, "Failed to process schema : " + ex.getMessage(), ERROR_FAILED_TO_RETRIEVE_PROPERTIES,
                                 JOptionPane.ERROR_MESSAGE);
          log.error(ex.getMessage(), ex);
        }
      } else {
        JOptionPane
            .showMessageDialog(panel, "No schema has been loaded, we cannot extract properties", ERROR_FAILED_TO_RETRIEVE_PROPERTIES,
                               JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  @Override
  public void clearGui() {
    // Not implementation required
  }

  @Override
  public final void setDescriptor(final PropertyDescriptor propertyDescriptor) {
    super.setSource(propertyDescriptor);
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
    this.subjectNameComboBox.setSelectedItem(text);
    super.setValue(text);
  }

  @Override
  public final void setValue(final Object value) {
    this.subjectNameComboBox.setSelectedItem(Objects.requireNonNullElse(value, ""));
    super.setValue(value);
  }

  @Override
  public final Object getValue() {
    return this.subjectNameComboBox.getSelectedItem();
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }

  class ComboFiller implements FocusListener {

    @Override
    public void focusGained(final FocusEvent e) {
      final String subjects = JMeterContextService.getContext().getProperties().getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS);
      subjectNameComboBox.setModel(new DefaultComboBoxModel<>(subjects.split(",")));
    }

    @Override
    public void focusLost(final FocusEvent e) {
      // Override but not used. Implementation not needed.
    }
  }

}
