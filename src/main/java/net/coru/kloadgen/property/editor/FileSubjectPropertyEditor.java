/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS;

import io.confluent.kafka.schemaregistry.ParsedSchema;
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

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.AutoCompletion;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
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

  private JComboBox<String> schemaTypeComboBox;

  private JComboBox<String> subjectNameComboBox;

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final JPanel panel = new JPanel();

  private PropertyDescriptor propertyDescriptor;

  private final SchemaExtractor schemaExtractor = new SchemaExtractorImpl();

  private static ParsedSchema parserSchema;

  private final JButton openFileDialogButton = new JButton(JMeterUtils.getResString("file_visualizer_open"));

  public FileSubjectPropertyEditor() {
    this.init();
  }

  public FileSubjectPropertyEditor(Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public FileSubjectPropertyEditor(PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.propertyDescriptor = propertyDescriptor;
    this.init();
  }

  private void init() {
    schemaTypeComboBox = new JComboBox<>();
    schemaTypeComboBox.setEditable(false);
    schemaTypeComboBox.insertItemAt("AVRO", 0);
    schemaTypeComboBox.insertItemAt("JSON-Schema", 1);
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

  public void actionFileChooser(ActionEvent event) {

    int returnValue = fileChooser.showDialog(panel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      File subjectName = Objects.requireNonNull(fileChooser.getSelectedFile());
      try {
        String schemaType = schemaTypeComboBox.getSelectedItem().toString();
        parserSchema = schemaExtractor.schemaTypesList(subjectName, schemaType);
        subjectNameComboBox.removeAllItems();
        subjectNameComboBox.addItem(parserSchema.name());
        subjectNameComboBox.setSelectedItem(parserSchema.name());
      } catch (IOException e) {
        JOptionPane.showMessageDialog(panel, "Can't read a file : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
            JOptionPane.ERROR_MESSAGE);
        log.error(e.getMessage(), e);
      }
      subjectNameComboBox.addFocusListener(new ComboFiller());
    }
  }

  public ParsedSchema getSelectedSchema(String name) {return parserSchema;}

  public List<FieldValueMapping> getAttributeList(ParsedSchema selectedSchema) {
    Schema schemaObj = (Schema) selectedSchema.rawSchema();
    if(schemaObj.getType().equals(Schema.Type.UNION) && selectedSchema.schemaType().equalsIgnoreCase("AVRO")) {
      Schema lastElement = schemaObj.getTypes().get(schemaObj.getTypes().size() -1);
      selectedSchema = new AvroSchema(lastElement.toString());
    }
    if(Objects.nonNull(selectedSchema)) {
      return schemaExtractor.flatPropertiesList(selectedSchema);
    }
    return new ArrayList<>();
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (subjectNameComboBox.getItemCount() != 0) {
      String schemaType =  schemaTypeComboBox.getSelectedItem().toString();
      String selectedItem = (String) subjectNameComboBox.getSelectedItem();
      ParsedSchema selectedSchema = getSelectedSchema(selectedItem);
      List<FieldValueMapping> attributeList = getAttributeList(selectedSchema);

      if (!attributeList.isEmpty()) {
        try {
          //Get current test GUI component
          TestBeanGUI testBeanGUI = (TestBeanGUI) GuiPackage.getInstance().getCurrentGui();
          Field customizer = TestBeanGUI.class.getDeclaredField(PropsKeysHelper.CUSTOMIZER);
          customizer.setAccessible(true);

          //From TestBeanGUI retrieve Bean Customizer as it includes all editors like ClassPropertyEditor, TableEditor
          GenericTestBeanCustomizer testBeanCustomizer = (GenericTestBeanCustomizer) customizer.get(testBeanGUI);
          Field editors = GenericTestBeanCustomizer.class.getDeclaredField(PropsKeysHelper.EDITORS);
          editors.setAccessible(true);

          //Retrieve TableEditor and set all fields with default values to it
          PropertyEditor[] propertyEditors = (PropertyEditor[]) editors.get(testBeanCustomizer);
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
              .showMessageDialog(panel, "Failed to retrieve schema : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
                  JOptionPane.ERROR_MESSAGE);
          log.error(e.getMessage(), e);
        } catch (AvroRuntimeException ex) {
          JOptionPane
              .showMessageDialog(panel, "Failed to process schema : " + ex.getMessage(), "ERROR: Failed to retrieve properties!",
                  JOptionPane.ERROR_MESSAGE);
          log.error(ex.getMessage(), ex);
        }
      } else {
        JOptionPane
            .showMessageDialog(panel, "No schema has been loaded, we cannot extract properties", "ERROR: Failed to retrieve properties!",
                JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  @Override
  public void clearGui() {
    // Not implementation required
  }

  @Override
  public void setDescriptor(PropertyDescriptor descriptor) {
    propertyDescriptor = descriptor;
  }

  @Override
  public String getAsText() {
    return Objects.requireNonNull(this.subjectNameComboBox.getSelectedItem()).toString();
  }

  @Override
  public Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    this.subjectNameComboBox.setSelectedItem(text);
  }

  @Override
  public void setValue(Object value) {
      if (value != null) {
        this.subjectNameComboBox.setSelectedItem(value);
      } else {
        this.subjectNameComboBox.setSelectedItem("");
      }
  }

  @Override
  public Object getValue() {
    return this.subjectNameComboBox.getSelectedItem();
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }

  class ComboFiller implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {
      String subjects = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_SUBJECTS);
      subjectNameComboBox.setModel(new DefaultComboBoxModel<>(subjects.split(",")));
    }

    @Override
    public void focusLost(FocusEvent e) {
      // Override but not used. Implementation not needed.
    }
  }

}
