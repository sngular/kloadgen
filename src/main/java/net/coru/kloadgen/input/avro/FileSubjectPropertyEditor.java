package net.coru.kloadgen.input.avro;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS;
import static org.apache.avro.Schema.Type.RECORD;

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
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.AutoCompletion;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.commons.collections4.IterableUtils;
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

  private JComboBox<String> subjectNameComboBox;

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final JPanel panel = new JPanel();

  private PropertyDescriptor propertyDescriptor;

  private final SchemaExtractor schemaExtractor = new SchemaExtractor();

  private static Schema parserSchema;

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
    subjectNameComboBox = new JComboBox<>();
    subjectNameComboBox.setEditable(true);
    panel.setLayout(new BorderLayout());
    openFileDialogButton.addActionListener(this::actionFileChooser);
    panel.add(openFileDialogButton, BorderLayout.LINE_END);
    panel.add(subjectNameComboBox);
    AutoCompletion.enable(subjectNameComboBox);
    this.subjectNameComboBox.addActionListener(this);
  }
  public void actionFileChooser(ActionEvent event) {

    int returnValue = fileChooser.showDialog(panel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      File subjectName = Objects.requireNonNull(fileChooser.getSelectedFile());
      try {
        parserSchema = schemaExtractor.schemaTypesList(subjectName);
        subjectNameComboBox.removeAllItems();
        if (Type.UNION == parserSchema.getType()) {
          Iterable<Schema> schemaList = IterableUtils.filteredIterable(parserSchema.getTypes(), t -> t.getType() == RECORD);
          for (Schema types : schemaList) {
            subjectNameComboBox.addItem(types.getName());
          }
        } else {
          subjectNameComboBox.addItem(parserSchema.getName());
        }
      } catch (IOException e) {
        JOptionPane.showMessageDialog(panel, "Can't read a file : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
            JOptionPane.ERROR_MESSAGE);
        log.error(e.getMessage(), e);
      }
      subjectNameComboBox.addFocusListener(new ComboFiller());
    }
  }

  public Schema getSelectedSchema(String name) {
    if (Type.UNION == parserSchema.getType()) {
      return getRecordUnion(parserSchema.getTypes(), name);
    } else {
      return parserSchema;
    }
  }

  private Schema getRecordUnion(List<Schema> types, String name) {
    Schema unionSchema = null;
    for (Schema schema : types) {
      if (schema.getName().equalsIgnoreCase(name)) {
        unionSchema = schema;
      }
    }
    return unionSchema;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (subjectNameComboBox.getItemCount() != 0) {

      String selectedItem = (String) subjectNameComboBox.getSelectedItem();
      Schema selectedSchema = getSelectedSchema(selectedItem);

      if (Objects.nonNull(selectedSchema)) {
        try {
          List<FieldValueMapping> attributeList = schemaExtractor.flatPropertiesList(selectedSchema);
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
            }
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          JOptionPane
              .showMessageDialog(panel, "Failed retrieve schema properties : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
                  JOptionPane.ERROR_MESSAGE);
          log.error(e.getMessage(), e);
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
