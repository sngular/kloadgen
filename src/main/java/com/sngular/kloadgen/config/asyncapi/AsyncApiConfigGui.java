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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import com.sngular.kloadgen.extractor.model.AsyncApiAbstract;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;
import com.sngular.kloadgen.sampler.AsyncApiSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public final class AsyncApiConfigGui extends AbstractSamplerGui {

  private static final Logger log = LoggingManager.getLoggerForClass();

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
    mainPanel.add(createAsyncApiTabs(), BorderLayout.SOUTH);
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
      final File apiFile = Objects.requireNonNull(fileChooser.getSelectedFile());
      asyncApiFileTextField.setText(apiFile.getAbsolutePath());
      final var asyncApiFile = asyncApiExtractor.processFile(apiFile);
      asyncApiFile.getApiServerList().forEach(serverComboBox::addItem);
      fillTable(brokerFieldModel, asyncApiExtractor.getBrokerData(asyncApiFile));
      fillTable(schemaRegistryFieldModel, asyncApiExtractor.getSchemaRegistryData(asyncApiFile));
      asyncApiExtractor.getSchemaDataMap(asyncApiFile).values().forEach(topicComboBox::addItem);
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

  private <T extends AsyncApiAbstract> Object[] dataToRow(final T data) {
    return data.getProperties();
  }

  private JPanel createBrokerPanel() {
    final JPanel brokerPanel = new JPanel();
    brokerPanel.setLayout(new BorderLayout(0, 0));
    brokerPanel.add(new JLabeledTextField("Broker Server"));
    serverComboBox = new JComboBox<>();
    serverComboBox.setRenderer(new ApiServerRenderer());
    brokerPanel.add(serverComboBox);
    brokerPanel.add(new JTable(brokerFieldModel), BorderLayout.CENTER);
    return brokerPanel;
  }

  private JPanel createRegistryTab() {
    final JPanel registryUrlPanel = new JPanel();
    registryUrlPanel.setLayout(new GridBagLayout());
    registryUrlPanel.setMinimumSize(new Dimension(45, 45));
    registryUrlPanel.setPreferredSize(new Dimension(800, 600));
    final JLabel label2 = new JLabel();
    label2.setText("Schema Registry URL");
    var gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    registryUrlPanel.add(label2, gridBagConstraints);
    this.registryUrl = new JTextField();
    this.registryUrl.setPreferredSize(new Dimension(249, 30));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    registryUrlPanel.add(this.registryUrl, gridBagConstraints);
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    registryUrlPanel.add(panel5, gridBagConstraints);
    final JLabel label3 = new JLabel();
    label3.setText("Security Options");
    panel5.add(label3);
    panel5.add(new JTable(schemaRegistryFieldModel));
    return registryUrlPanel;
  }

  private JPanel createSchemaTab() {
    final JPanel schemaTab = new JPanel();
    schemaTab.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    topicComboBox = new JComboBox<>();
    topicComboBox.setRenderer(new ApiSchemaRenderer());
    schemaTab.add(topicComboBox);
    schemaTab.add(new JTable(schemaFieldModel));
    return schemaTab;
  }

  private static Component getComponentProperty(final String property) {
    return new Component() {
      @Override
      public String getName() {
        return property;
      }
    };
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
