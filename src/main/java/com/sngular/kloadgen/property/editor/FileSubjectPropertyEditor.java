/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.property.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.extractor.extractors.ExtractorFactory;
import com.sngular.kloadgen.extractor.extractors.ExtractorRegistry;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.util.PropsKeysHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.util.JMeterUtils;


@Slf4j
public class FileSubjectPropertyEditor extends PropertyEditorSupport implements TestBeanPropertyEditor, ClearGui {

  public static final String ERROR_FAILED_TO_RETRIEVE_PROPERTIES = "ERROR: Failed to retrieve properties!";

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final JPanel panel = new JPanel();

  private final JButton openFileDialogButton = new JButton(JMeterUtils.getResString("file_visualizer_open"));

  private JComboBox<String> schemaTypeComboBox;

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
    panel.setLayout(new BorderLayout());
    openFileDialogButton.addActionListener(this::actionFileChooser);
    panel.add(openFileDialogButton, BorderLayout.LINE_END);
    panel.add(schemaTypeComboBox);
  }

  public final void actionFileChooser(final ActionEvent event) {

    final int returnValue = fileChooser.showDialog(panel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      final File schemaFile = Objects.requireNonNull(fileChooser.getSelectedFile());
      try {
        final String schemaType = schemaTypeComboBox.getSelectedItem().toString();
        final ExtractorRegistry extractor = ExtractorFactory.getExtractor(schemaType);
        final String fileContent = SchemaExtractor.readSchemaFile(schemaFile.getPath());
        final ParsedSchema parserSchema = (ParsedSchema) extractor.processSchema(fileContent);
        final List<FieldValueMapping> schemaFieldList = extractor.processSchema(parserSchema, SchemaRegistryEnum.CONFLUENT);
        buildTable(schemaFieldList, fileContent, schemaType);
      } catch (final IOException e) {
        JOptionPane.showMessageDialog(panel, "Can't read a file : " + e.getMessage(), ERROR_FAILED_TO_RETRIEVE_PROPERTIES,
                                    JOptionPane.ERROR_MESSAGE);
        log.error(e.getMessage(), e);
      }
    }
  }

  public final List<FieldValueMapping> getAttributeList(final ParsedSchema selectedSchema) {
    final List<FieldValueMapping> result = new ArrayList<>();
    if (Objects.nonNull(selectedSchema)) {
      result.addAll(SchemaExtractor.flatPropertiesList(selectedSchema));
    }
    return result;
  }

  public final void buildTable(final List<FieldValueMapping> attributeList, final String fileContent, final String schemaType) {

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
            propertyEditor.setValue(fileContent);
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

  @Override
  public void clearGui() {
    // Not implementation required
  }

  @Override
  public final void setDescriptor(final PropertyDescriptor propertyDescriptor) {
    super.setSource(propertyDescriptor);
  }


  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }
}
