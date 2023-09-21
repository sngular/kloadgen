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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import com.sngular.kloadgen.extractor.model.AsyncApiAbstract;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.sampler.AsyncApiSampler;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

public final class AsyncApiConfigGui extends AbstractSamplerGui {

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final ApiExtractor asyncApiExtractor = new AsyncApiExtractorImpl();

  private JPanel mainPanel;

  private JTextField asyncApiFileTextField;

  private JTextField registryUrl;

  private final DefaultTableModel schemaFieldModel = new DefaultTableModel(new String[]{"Field Name", "Field Type", "Field Length", "Field Values List"}, 20);

  private final DefaultTableModel brokerFieldModel = new DefaultTableModel(new String[] {"Property name", "Property Value"}, 20);

  private final DefaultTableModel schemaRegistryFieldModel = new DefaultTableModel(new String[] {"Property name", "Property Value"}, 20);

  private JComboBox<AsyncApiServer> serverComboBox;

  private JComboBox<AsyncApiSchema> topicComboBox;

  public AsyncApiConfigGui() {
    super();
    init();
  }

  @Override
  public String getStaticLabel() {
    return "AsyncApi Sampler";
  }

  private void init() {
    setLayout(new BorderLayout());
    setBorder(makeBorder());
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.putClientProperty("html.disable", Boolean.FALSE);
    mainPanel.setBorder(
        BorderFactory
          .createTitledBorder(BorderFactory.createLoweredBevelBorder(), "AsyncApi Module", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));

    mainPanel.add(createAsyncApiFileSelectPanel(), BorderLayout.NORTH);
    mainPanel.add(createAsyncApiTabs(), BorderLayout.CENTER);
    add(mainPanel);
  }

  private JPanel createAsyncApiFileSelectPanel() {
    final JPanel fileChoosingPanel = new JPanel();
    fileChoosingPanel.setLayout(new GridBagLayout());
    fileChoosingPanel.setName("AsyncApi File");
    var gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weighty = 0.03;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    asyncApiFileTextField = new JTextField();
    asyncApiFileTextField.setPreferredSize(new Dimension(249, 30));
    asyncApiFileTextField.setText("");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    fileChoosingPanel.add(asyncApiFileTextField, gridBagConstraints);
    final JButton fileButton = new JButton();
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
    return fileChoosingPanel;
  }

  private JTabbedPane createAsyncApiTabs() {
    final var tabbedPanel = new JTabbedPane();
    tabbedPanel.addTab("Broker", createBrokerPanel());
    tabbedPanel.addTab("Registry", createRegistryTab());
    tabbedPanel.addTab("Schema", createSchemaTab());
    return tabbedPanel;
  }

  public void actionFileChooser(final ActionEvent event) {

    final int returnValue = fileChooser.showDialog(mainPanel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      try {
        final File apiFile = Objects.requireNonNull(fileChooser.getSelectedFile());
        asyncApiFileTextField.setText(apiFile.getAbsolutePath());
        final var asyncApiFile = asyncApiExtractor.processFile(apiFile);
        asyncApiFile.getApiServerList().forEach(serverComboBox::addItem);
        fillTable(brokerFieldModel, asyncApiExtractor.getBrokerData(asyncApiFile));
        fillTable(schemaRegistryFieldModel, asyncApiExtractor.getSchemaRegistryData(asyncApiFile));
        asyncApiExtractor.getSchemaDataMap(asyncApiFile).values().forEach(topicComboBox::addItem);
      } catch (KLoadGenException ex) {
        JOptionPane.showMessageDialog(mainPanel, "Error has occurred: " + ex.getMessage(), "Weird Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private <T extends AsyncApiAbstract> void fillTable(final DefaultTableModel schemaFields, final List<T> schemaData) {
    if (Objects.nonNull(schemaData)) {
      final var count = schemaFields.getRowCount();
      for (var i = 0; i < count; i++) {
        schemaFields.removeRow(0);
      }
      schemaData.forEach(data -> schemaFields.addRow(dataToRow(data)));
    }
  }

  private <T extends AsyncApiAbstract> void fillTable(final DefaultTableModel schemaFields, final Object[] schemaData) {
    if (Objects.nonNull(schemaData)) {
      final var count = schemaFields.getRowCount();
      for (var i = 0; i < count; i++) {
        schemaFields.removeRow(0);
      }
      for (var data : schemaData) {
        schemaFields.addRow(((FieldValueMapping) data).getProperties());
      }
    }
  }

  private <T extends AsyncApiAbstract> Object[] dataToRow(final T data) {
    return data.getProperties();
  }

  private JPanel createBrokerPanel() {
    final JPanel brokerPanel = new JPanel();
    brokerPanel.setLayout(new BorderLayout(0, 0));
    brokerPanel.add(new JLabeledTextField("Broker Server"));
    serverComboBox = new JComboBox<>();
    serverComboBox.setRenderer(new ApiServerRenderer());
    brokerPanel.add(serverComboBox, BorderLayout.NORTH);
    brokerPanel.add(new JScrollPane(new JTable(brokerFieldModel)), BorderLayout.CENTER);
    return brokerPanel;
  }

  private JPanel createRegistryTab() {
    final JPanel registryUrlPanel = new JPanel();
    registryUrlPanel.setLayout(new BorderLayout(0, 0));
    registryUrlPanel.add(new JLabeledTextField("Schema Registry URL"));
    this.registryUrl = new JTextField();
    this.registryUrl.setPreferredSize(new Dimension(249, 30));
    registryUrlPanel.add(this.registryUrl, BorderLayout.NORTH);
    registryUrlPanel.add(new JScrollPane(new JTable(schemaRegistryFieldModel)), BorderLayout.CENTER);
    return registryUrlPanel;
  }

  private JPanel createSchemaTab() {
    final JPanel schemaTab = new JPanel();
    schemaTab.setLayout(new BorderLayout(0, 0));
    schemaTab.add(new JLabeledTextField("Schema Configuration"));
    topicComboBox = new JComboBox<>();
    topicComboBox.setRenderer(new ApiSchemaRenderer());
    topicComboBox.addActionListener(this::topicComboActionListener);
    schemaTab.add(topicComboBox, BorderLayout.NORTH);
    schemaTab.add(new JScrollPane(new JTable(schemaFieldModel)), BorderLayout.CENTER);
    return schemaTab;
  }

  private void topicComboActionListener(final ActionEvent event) {
    final JComboBox cb = (JComboBox)event.getSource();
    final var selectedSchema = (AsyncApiSchema) cb.getSelectedItem();
    fillTable(schemaFieldModel, selectedSchema.getProperties());
  }

  @Override
  public void clearGui() {
    super.clearGui();
    asyncApiFileTextField.setText("");
    registryUrl.setText("");
  }

  @Override
  public String getLabelResource() {
    return "Asyncapi";
  }

  @Override
  public TestElement createTestElement() {
    final var testElement = new AsyncApiSampler();
    modifyTestElement(testElement);
    return testElement;
  }

  @Override
  public void modifyTestElement(TestElement element) {

  }
}
