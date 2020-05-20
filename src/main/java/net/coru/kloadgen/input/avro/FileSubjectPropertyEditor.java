package net.coru.kloadgen.input.avro;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SUBJECTS;
import static org.apache.avro.Schema.Type.RECORD;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
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

@Slf4j
public class FileSubjectPropertyEditor  extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private JComboBox<String> subjectNameComboBox;

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final JPanel panel = new JPanel();

  private PropertyDescriptor propertyDescriptor;

  private final SchemaExtractor schemaExtractor = new SchemaExtractor();

  private static Schema parserSchema;

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
    panel.add(fileChooser);
    panel.add(subjectNameComboBox, BorderLayout.AFTER_LAST_LINE);
    this.fileChooser.addActionListener(this::actionFileChooser);
    this.subjectNameComboBox.addActionListener(this);
  }

  public void actionFileChooser(ActionEvent event) {
    File subjectName = Objects.requireNonNull(this.fileChooser.getSelectedFile());
    try {
      parserSchema = schemaExtractor.schemaTypesList(subjectName);//Devolver lista de Strings
      subjectNameComboBox.removeAllItems();
      if(Type.UNION == parserSchema.getType()) {
        Iterable<Schema> schemaList = IterableUtils.filteredIterable(parserSchema.getTypes(), t -> t.getType() == RECORD);
        for (Schema types : schemaList) {
          subjectNameComboBox.addItem(types.getName());
        }
      }else {
        subjectNameComboBox.addItem(parserSchema.getName());
      }
    } catch (IOException | RestClientException e) {
      JOptionPane.showMessageDialog(null, "Can't read a file : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
          JOptionPane.ERROR_MESSAGE);
      log.error(e.getMessage(), e);
    }
     subjectNameComboBox.addFocusListener(new ComboFiller());
  }

  public org.apache.avro.Schema getSelectedSchema(String name) {
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

      String selectedItem = subjectNameComboBox.getSelectedItem().toString();
      Schema selectedSchema = getSelectedSchema(selectedItem);
      File subjectName = Objects.requireNonNull(this.fileChooser.getSelectedFile());

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
          }
        }
      } catch (IOException | RestClientException | NoSuchFieldException | IllegalAccessException e) {
        JOptionPane.showMessageDialog(null, "Failed retrieve schema properties : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
            JOptionPane.ERROR_MESSAGE);
        log.error(e.getMessage(), e);
      }

    }
  }

  @Override
  public void clearGui() {
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
