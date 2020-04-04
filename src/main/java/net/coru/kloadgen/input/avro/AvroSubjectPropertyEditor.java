package net.coru.kloadgen.input.avro;

import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_SUBJECTS;

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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.threads.JMeterContextService;

@Slf4j
public class AvroSubjectPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private JComboBox<String> subjectNameComboBox;

  private JButton loadClassBtn = new JButton("Load Subject");

  private JPanel panel = new JPanel();

  private PropertyDescriptor propertyDescriptor;

  private SchemaExtractor schemaExtractor = new SchemaExtractor();

  public AvroSubjectPropertyEditor() {
    this.init();
  }

  public AvroSubjectPropertyEditor(Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public AvroSubjectPropertyEditor(PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.propertyDescriptor = propertyDescriptor;
    this.init();
  }

  private void init() {
    subjectNameComboBox = new JComboBox<>();
    subjectNameComboBox.addFocusListener(new ComboFiller());
    panel.setLayout(new BorderLayout());
    panel.add(subjectNameComboBox);
    panel.add(loadClassBtn, BorderLayout.AFTER_LINE_ENDS);
    this.loadClassBtn.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    String subjectName = Objects.requireNonNull(this.subjectNameComboBox.getSelectedItem()).toString();

    try {
      List<FieldValueMapping> attributeList = schemaExtractor.flatPropertiesList(subjectName);

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
      JOptionPane.showMessageDialog(null, "Successful retrieving of subject : " + subjectName, "Successful retrieving properties",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException | RestClientException | NoSuchFieldException | IllegalAccessException e) {
      JOptionPane.showMessageDialog(null, "Failed retrieve schema properties : " + e.getMessage(), "ERROR: Failed to retrieve properties!",
          JOptionPane.ERROR_MESSAGE);
      log.error(e.getMessage(), e);
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

    }
  }
}
