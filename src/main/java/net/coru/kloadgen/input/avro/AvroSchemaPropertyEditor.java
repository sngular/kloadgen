package net.coru.kloadgen.input.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;

@Slf4j
public class AvroSchemaPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

    private final JTextField schemaUrlTextField = new JTextField();

    private final JButton testSchemaRepoBtn = new JButton("Test Registry");

    private final JPanel panel = new JPanel();

    private PropertyDescriptor propertyDescriptor;

    public AvroSchemaPropertyEditor() {
        this.init();
    }

    private void init() {
        panel.setLayout(new BorderLayout());
        panel.add(schemaUrlTextField);
        panel.add(testSchemaRepoBtn, BorderLayout.AFTER_LINE_ENDS);
        this.testSchemaRepoBtn.addActionListener(this);
    }

    public AvroSchemaPropertyEditor(Object source) {
        super(source);
        this.init();
        this.setValue(source);
    }

    public AvroSchemaPropertyEditor(PropertyDescriptor descriptor) {
        super(descriptor);
        this.propertyDescriptor = descriptor;
        this.init();
    }

    @Override
    public String getAsText() {
        return this.schemaUrlTextField.getText();
    }

    @Override
    public Component getCustomEditor() {
        return this.panel;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.schemaUrlTextField.setText(text);
        this.schemaUrlTextField.setCaretPosition(0);
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            this.schemaUrlTextField.setText(value.toString());
            this.schemaUrlTextField.setCaretPosition(0);
        } else {
            this.schemaUrlTextField.setText("");
        }
    }

    @Override
    public Object getValue() {
        return this.schemaUrlTextField.getText();
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    //Load class button action listener
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(getAsText(), 1000);
        try {
            schemaRegistryClient.getAllSubjects();
        } catch (IOException | RestClientException e) {
           log.error(e.getMessage(), e);
        }
    }

    @Override
    public void setDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    @Override
    public void clearGui() {
        this.schemaUrlTextField.setText("");
    }
}
