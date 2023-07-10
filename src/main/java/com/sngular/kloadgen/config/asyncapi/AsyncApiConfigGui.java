/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.asyncapi;

import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.property.editor.FileSubjectPropertyEditor;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AsyncApiConfigGui extends AbstractConfigGui {

    private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

    private JPanel mainPanel;
    private JTabbedPane asyncApiTabs;
    private JTable brokerInfo;
    private JTextField textField1;
    private JTable schemaRegistry;
    private JTable schemaFields;
    private JTextField asyncApiFileTextField;
    private JButton fileButton;
    private JTableHeader jTableHeaderKafka;
    private JTableHeader jTableHeaderRegistry;
    private JTableHeader jTableHeaderSchema;

    private final SchemaExtractor asyncApiExtractor = new AsyncApiExtractorImpl();


    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        var testElement = new AsyncApiTestElement();
        modifyTestElement(testElement);
        return testElement;
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(-1, -1));
        mainPanel.putClientProperty("html.disable", Boolean.FALSE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "AsyncApi Module", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel fileChoosingPanel = new JPanel();
        fileChoosingPanel.setLayout(new GridBagLayout());
        fileChoosingPanel.setName("AsyncApi File");
        var gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.03;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        mainPanel.add(fileChoosingPanel, gridBagConstraints);
        asyncApiFileTextField = new JTextField();
        asyncApiFileTextField.setPreferredSize(new Dimension(249, 30));
        asyncApiFileTextField.setText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        fileChoosingPanel.add(asyncApiFileTextField, gridBagConstraints);
        fileButton = new JButton();
        fileButton.setText("Open File");
        fileButton.addActionListener(this::actionFileChooser);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        fileChoosingPanel.add(fileButton, gridBagConstraints);
        final JLabel label1 = new JLabel();
        label1.setText("AsyncApi File");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        fileChoosingPanel.add(label1, gridBagConstraints);
        asyncApiTabs = new JTabbedPane();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        mainPanel.add(asyncApiTabs, gridBagConstraints);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        asyncApiTabs.addTab("Broker", panel3);
        brokerInfo = new JTable();
        brokerInfo.setIntercellSpacing(new Dimension(2, 1));
        brokerInfo.putClientProperty("JTable.autoStartsEdit", Boolean.TRUE);
        brokerInfo.putClientProperty("Table.isFileList", Boolean.TRUE);
        jTableHeaderKafka = new JTableHeader();
        jTableHeaderKafka.add(getComponentProperty("Property"));
        jTableHeaderKafka.add(getComponentProperty("Value"));
        brokerInfo.setTableHeader(jTableHeaderKafka);
        panel3.add(brokerInfo, BorderLayout.CENTER);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel4.setMinimumSize(new Dimension(45, 45));
        panel4.setPreferredSize(new Dimension(200, 45));
        asyncApiTabs.addTab("Registry", panel4);
        final JLabel label2 = new JLabel();
        label2.setText("Schema Registry URL");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        panel4.add(label2, gridBagConstraints);
        textField1 = new JTextField();
        textField1.setPreferredSize(new Dimension(249, 30));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(textField1, gridBagConstraints);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        panel4.add(panel5, gridBagConstraints);
        final JLabel label3 = new JLabel();
        label3.setText("Security Options");
        panel5.add(label3);
        schemaRegistry = new JTable();
        jTableHeaderRegistry = new JTableHeader();
        jTableHeaderRegistry.add(getComponentProperty("Property"));
        jTableHeaderRegistry.add(getComponentProperty("Value"));
        schemaRegistry.setTableHeader(jTableHeaderRegistry);
        panel5.add(schemaRegistry);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        asyncApiTabs.addTab("Schema", panel6);
        schemaFields = new JTable();
        jTableHeaderSchema = new JTableHeader();
        jTableHeaderSchema.add(getComponentProperty("Field Name"));
        jTableHeaderSchema.add(getComponentProperty("Field Type"));
        jTableHeaderSchema.add(getComponentProperty("Field Size"));
        jTableHeaderSchema.add(getComponentProperty("Field Value"));
        schemaFields.setTableHeader(jTableHeaderSchema);
        schemaFields.setSurrendersFocusOnKeystroke(false);
        panel6.add(schemaFields);
        fileChoosingPanel.setNextFocusableComponent(asyncApiTabs);
    }

    @NotNull
    private static Component getComponentProperty(String Property) {
        return new Component() {
            @Override
            public String getName() {
                return Property;
            }
        };
    }

    public final void actionFileChooser(final ActionEvent event) {

        final int returnValue = fileChooser.showDialog(mainPanel, JMeterUtils.getResString("file_visualizer_open"));

        if (JFileChooser.APPROVE_OPTION == returnValue) {
            final File subjectName = Objects.requireNonNull(fileChooser.getSelectedFile());
            asyncApiFileTextField.setText(subjectName.getAbsolutePath());
            try {

                brokerInfo. = asyncApiExtractor.schemaTypesList(subjectName, schemaType);
                subjectNameComboBox.removeAllItems();
                subjectNameComboBox.addItem(parserSchema.name());
                subjectNameComboBox.setSelectedItem(parserSchema.name());
            } catch (final IOException e) {
                JOptionPane.showMessageDialog(panel, "Can't read a file : " + e.getMessage(), ERROR_FAILED_TO_RETRIEVE_PROPERTIES,
                        JOptionPane.ERROR_MESSAGE);
                log.error(e.getMessage(), e);
            }
            subjectNameComboBox.addFocusListener(new FileSubjectPropertyEditor.ComboFiller());
        }
    }
}
