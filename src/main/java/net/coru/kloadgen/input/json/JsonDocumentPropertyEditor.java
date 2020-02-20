package net.coru.kloadgen.input.json;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.input.avro.SchemaExtractor;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.ProducerKeysHelper;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

@Slf4j
public class JsonDocumentPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JTextField subjectNameTextField = new JTextField();

  private final JButton loadClassBtn = new JButton("Process Json");

  private final JPanel panel = new JPanel();

  private PropertyDescriptor propertyDescriptor;

  private SchemaExtractor schemaExtractor = new SchemaExtractor();

  private JMeterVariables variables;

  public JsonDocumentPropertyEditor() {
    this.init();
  }

  public JsonDocumentPropertyEditor(Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public JsonDocumentPropertyEditor(PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.propertyDescriptor = propertyDescriptor;
    this.init();
  }

  private void init() {

    panel.setLayout(new BorderLayout());
    panel.add(subjectNameTextField);
    panel.add(loadClassBtn, BorderLayout.AFTER_LINE_ENDS);
    this.loadClassBtn.addActionListener(this);
    variables = JMeterContextService.getContext().getVariables();
  }
  @Override
  public void actionPerformed(ActionEvent event) {
    String schemaUrl = variables.get(ProducerKeysHelper.SCHEMA_REGISTRY_URL);
    String subjectName = this.subjectNameTextField.getText();

    try {
      List<FieldValueMapping> attributeList = schemaExtractor.flatPropertiesList(schemaUrl, subjectName);

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
      for (PropertyEditor propertyEditor : propertyEditors){
        if (propertyEditor instanceof TableEditor){
          propertyEditor.setValue(attributeList);
        }
      }
    }catch (IOException | RestClientException | NoSuchFieldException | IllegalAccessException e) {
      JOptionPane.showMessageDialog(null, "Failed retrieve schema properties : " + e.getMessage(), "ERROR: Failed to retrieve properties!" , JOptionPane.ERROR_MESSAGE);
      log.error(e.getMessage(), e);
    }
  }



  @Override
  public void clearGui() {

  }

  @Override
  public void setDescriptor(PropertyDescriptor descriptor) {

  }

  @Override
  public String getAsText() {
    return this.subjectNameTextField.getText();
  }

  @Override
  public Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    this.subjectNameTextField.setText(text);
    this.subjectNameTextField.setCaretPosition(0);
  }

  @Override
  public void setValue(Object value) {
    if (value != null) {
      this.subjectNameTextField.setText(value.toString());
      this.subjectNameTextField.setCaretPosition(0);
    } else {
      this.subjectNameTextField.setText("");
    }

  }

  @Override
  public Object getValue() {
    return this.subjectNameTextField.getText();
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }
}
