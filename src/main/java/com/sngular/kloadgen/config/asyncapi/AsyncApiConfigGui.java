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

import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.JTableHeader;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.jetbrains.annotations.NotNull;

public class AsyncApiConfigGui extends AbstractSamplerGui {

  public static final String ERROR_FAILED_TO_RETRIEVE_PROPERTIES = "ERROR: Failed to retrieve properties!";

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final ApiExtractor asyncApiExtractor = new AsyncApiExtractorImpl();

  private JPanel mainPanel;

  private JTable brokerInfo;

  private JTextField asyncApiFileTextField;

  private JTextField registryUrl;

  private JTable schemaRegistry;

  private JTable schemaFields;

  private JButton fileButton;

  private JTableHeader jTableHeaderKafka;

  private JTableHeader jTableHeaderRegistry;

  private JTableHeader jTableHeaderSchema;

  protected AsyncApiConfigGui() {
    init();
  }

  private void init() {
    setLayout(new BorderLayout());
    setBorder(makeBorder());
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    mainPanel.setPreferredSize(new Dimension(-1, -1));
    mainPanel.putClientProperty("html.disable", Boolean.FALSE);
    mainPanel.setBorder(
      BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "AsyncApi Module", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));

    mainPanel.add("asyncapi.file.selector", createAsyncApiFileSelectPanel());
    mainPanel.add("asyncapi.config.options", createAsyncApiTabs());
  }

  @NotNull
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
    return fileChoosingPanel;
  }

  @NotNull
  private JTabbedPane createAsyncApiTabs() {
    var tabbedPanel = new JTabbedPane();
    tabbedPanel.addTab("Broker", createBrokerPanel());
    tabbedPanel.addTab("Registry", createRegistryTab());
    tabbedPanel.addTab("Schema", createSchemaTab());
    return tabbedPanel;
  }

  public final void actionFileChooser(final ActionEvent event) {

    final int returnValue = fileChooser.showDialog(mainPanel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      final File apiFile = Objects.requireNonNull(fileChooser.getSelectedFile());
      asyncApiFileTextField.setText(apiFile.getAbsolutePath());
      var asyncApiFile = asyncApiExtractor.processFile(apiFile);

      fillTable(brokerInfo, asyncApiExtractor.getBrokerData(asyncApiFile));
      fillTable(schemaRegistry, asyncApiExtractor.getSchemaRegistryData(asyncApiFile));
      fillTable(schemaFields, asyncApiExtractor.getSchemaData(asyncApiFile));
    }
  }

  private <T> void fillTable(final JTable schemaFields, final List<T> schemaData) {

  }

  @NotNull
  private JPanel createBrokerPanel() {
    final JPanel brokerPanel = new JPanel();
    brokerPanel.setLayout(new BorderLayout(0, 0));
    var label = new JLabeledTextField("")
    brokerInfo = new JTable();
    brokerInfo.setIntercellSpacing(new Dimension(2, 1));
    brokerInfo.putClientProperty("JTable.autoStartsEdit", Boolean.TRUE);
    brokerInfo.putClientProperty("Table.isFileList", Boolean.TRUE);
    jTableHeaderKafka = new JTableHeader();
    jTableHeaderKafka.add(getComponentProperty("Property"));
    jTableHeaderKafka.add(getComponentProperty("Value"));
    brokerInfo.setTableHeader(jTableHeaderKafka);
    brokerPanel.add(brokerInfo, BorderLayout.CENTER);
    return brokerPanel;
  }

  @NotNull
  private JPanel createRegistryTab() {
    GridBagConstraints gridBagConstraints;
    final JPanel registryUrlPanel = new JPanel();
    registryUrlPanel.setLayout(new GridBagLayout());
    registryUrlPanel.setMinimumSize(new Dimension(45, 45));
    registryUrlPanel.setPreferredSize(new Dimension(200, 45));
    final JLabel label2 = new JLabel();
    label2.setText("Schema Registry URL");
    gridBagConstraints = new GridBagConstraints();
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
    schemaRegistry = new JTable();
    jTableHeaderRegistry = new JTableHeader();
    jTableHeaderRegistry.add(getComponentProperty("Property"));
    jTableHeaderRegistry.add(getComponentProperty("Value"));
    schemaRegistry.setTableHeader(jTableHeaderRegistry);
    panel5.add(schemaRegistry);
    return registryUrlPanel;
  }

  @NotNull
  private JPanel createSchemaTab() {
    final JPanel schemaTab = new JPanel();
    schemaTab.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    schemaFields = new JTable();
    jTableHeaderSchema = new JTableHeader();
    jTableHeaderSchema.add(getComponentProperty("Field Name"));
    jTableHeaderSchema.add(getComponentProperty("Field Type"));
    jTableHeaderSchema.add(getComponentProperty("Field Size"));
    jTableHeaderSchema.add(getComponentProperty("Field Value"));
    schemaFields.setTableHeader(jTableHeaderSchema);
    schemaFields.setSurrendersFocusOnKeystroke(false);
    schemaTab.add(schemaFields);
    return schemaTab;
  }

  @NotNull
  private static Component getComponentProperty(final String Property) {
    return new Component() {
      @Override
      public String getName() {
        return Property;
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
}
