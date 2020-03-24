package net.coru.kloadgen.input.avro;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.config.schemaregistry.SchemaRegistryConfig;
import net.coru.kloadgen.util.SpringUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.jmeter.threads.JMeterContextService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.coru.kloadgen.util.SchemaRegistryKeys.*;

@Slf4j
public class SchemaRegistryConfigPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

    private final JLabel schemaUrlLabel = new JLabel("Schema Url");
    private final JTextField schemaRegistryUrl = new JTextField(5);

    private final JLabel userNameLabel = new JLabel("Username");
    private final JTextField userName = new JTextField(2);

    private final JLabel passwordLabel = new JLabel("Password");
    private final JTextField password = new JPasswordField(2);

    private final JButton testSchemaRepoBtn = new JButton("Test Registry");

    private final JPanel panel = new JPanel();

    private PropertyDescriptor propertyDescriptor;

    ObjectMapper mapper = new ObjectMapper();

    public SchemaRegistryConfigPropertyEditor() {
        this.init();
    }

    private void init() {
        panel.setLayout(new BorderLayout());
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3,1));

        formPanel.add(schemaUrlLabel);
        schemaUrlLabel.setLabelFor(schemaRegistryUrl);
        formPanel.add(schemaRegistryUrl);

        formPanel.add(userNameLabel);
        userNameLabel.setLabelFor(userName);
        formPanel.add(userName);

        formPanel.add(passwordLabel);
        passwordLabel.setLabelFor(password);
        formPanel.add(password);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(testSchemaRepoBtn, BorderLayout.CENTER);
        this.testSchemaRepoBtn.addActionListener(this);
    }

    public SchemaRegistryConfigPropertyEditor(Object source) {
        super(source);
        this.init();
        this.setValue(source);
    }

    public SchemaRegistryConfigPropertyEditor(PropertyDescriptor descriptor) {
        super(descriptor);
        this.propertyDescriptor = descriptor;
        this.init();
    }

    @Override
    public String getAsText() {
        return this.schemaRegistryUrl.getText();
    }

    @Override
    public Component getCustomEditor() {
        return this.panel;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
       throw new UnsupportedOperationException("Operation not Supported:" + text);
    }

    @SneakyThrows
    @Override
    public void setValue(Object value) {
        String schemaConfig = (String) value;
        if (StringUtils.isNotEmpty(schemaConfig)) {
            if (schemaConfig.startsWith("{")) {
                SchemaRegistryConfig schemaRegistryConfig = mapper.readerFor(SchemaRegistryConfig.class).readValue((String) value);
                this.schemaRegistryUrl.setText(schemaRegistryConfig.getSchemaRegistryUrl());
                this.schemaRegistryUrl.setCaretPosition(0);
                this.userName.setText(schemaRegistryConfig.getUsername());
                this.password.setText(schemaRegistryConfig.getPassword());
                JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_URL, schemaRegistryConfig.getSchemaRegistryUrl());
                if (! SCHEMA_REGISTRY_USERNAME_DEFAULT.equalsIgnoreCase(userName.toString())) {
                    JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_USERNAME_KEY, schemaRegistryConfig.getUsername());
                    JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_PASSWORD_KEY, schemaRegistryConfig.getPassword());
                }
            }
        }
    }

    public void setSchemaRegistryUrl(String schemaUrl) {
        this.schemaRegistryUrl.setText(schemaUrl);
    }

    public void setUserName(String userName) {
        this.userName.setText(userName);
    }

    public void setPassword(String password) {
        this.password.setText(password);
    }
    @SneakyThrows
    @Override
    public Object getValue() {

        return mapper.writeValueAsString(SchemaRegistryConfig.builder()
                .schemaRegistryUrl(schemaRegistryUrl.getText())
                .username(userName.getText())
                .password(password.getText())
                .build());
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Map<String, String> originals = new HashMap<>();
        if (!SCHEMA_REGISTRY_USERNAME_DEFAULT.equalsIgnoreCase(userName.toString())) {
            originals.put("basic.auth.credentials.source", "USER_INFO");
            originals.put("schema.registry.basic.auth.user.info", userName.getText() + ":" + password.getText());
        }
        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(getAsText(), 1000, originals);
        try {
            schemaRegistryClient.getAllSubjects();
            JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_URL, schemaRegistryUrl.getText());
            JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_USERNAME_KEY, userName.getText());
            JMeterContextService.getContext().getProperties().setProperty(SCHEMA_REGISTRY_PASSWORD_KEY, password.getText());

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
        this.schemaRegistryUrl.setText("");
    }
}
