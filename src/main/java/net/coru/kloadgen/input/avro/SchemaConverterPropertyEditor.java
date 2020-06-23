package net.coru.kloadgen.input.avro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.extractor.SchemaExtractor;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;

@Slf4j
public class SchemaConverterPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private PropertyDescriptor propertyDescriptor;

  private final SchemaExtractor schemaExtractor = new SchemaExtractor();

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
    return schemaAsString;
  }

  @Override
  public void setAsText(String value) throws IllegalArgumentException {
    schemaAsString = value;
  }

  @Override
  public void setValue(Object value) {
    if (Objects.nonNull(value)) {
      schemaAsString = value.toString();
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
