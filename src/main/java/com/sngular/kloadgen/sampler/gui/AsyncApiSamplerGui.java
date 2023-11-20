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

package com.sngular.kloadgen.sampler.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

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

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.model.PropertyMapping;
import com.sngular.kloadgen.sampler.AsyncApiSampler;
import com.sngular.kloadgen.sampler.SamplerUtil;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.kafka.clients.producer.ProducerConfig;

public final class AsyncApiSamplerGui extends AbstractSamplerGui {

  private final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

  private final ApiExtractor asyncApiExtractor = new AsyncApiExtractorImpl();

  private JPanel mainPanel;

  private JTextField asyncApiFileTextField;

  private final DefaultTableModel schemaFieldModel = new DefaultTableModel(new String[]{"Field Name", "Field Type", "Field Length", "Required", "Field Values List"}, 20);

  private final DefaultTableModel brokerFieldModel = new DefaultTableModel(new String[] {"Property name", "Property Value"}, 20);

  private final DefaultTableModel schemaRegistryFieldModel = new DefaultTableModel(new String[] {"Property name", "Property Value"}, 20);

  private JComboBox<AsyncApiServer> serverComboBox;

  private JComboBox<AsyncApiSchema> topicComboBox;

  private JComboBox<AsyncApiSR> registryComboBox;

  private AsyncApiFile asyncApiFile;

  public AsyncApiSamplerGui() {
    init();
  }

  @Override
  public String getStaticLabel() {
    return "AsyncApi Sampler";
  }

  @Override
  public void clearGui() {
    super.clearGui();
    asyncApiFileTextField.setText("");
    schemaFieldModel.setNumRows(0);
    brokerFieldModel.setNumRows(0);
    // registryUrl.setText("");
  }

  @Override
  public String getLabelResource() {
    return "Asyncapi";
  }

  @Override
  public TestElement createTestElement() {
    return new AsyncApiSampler();
  }

  @Override
  public void modifyTestElement(final TestElement element) {
    super.configureTestElement(element);
    if (element instanceof AsyncApiSampler asyncApiSampler) {
      if (Objects.nonNull(asyncApiFile)) {
        asyncApiSampler.setAsyncApiFileNode(asyncApiFile.getAsyncApiFileNode());
        if (serverComboBox.getItemCount() > 0) {
          asyncApiSampler.setAsyncApiServerName(((AsyncApiServer) serverComboBox.getSelectedItem()).getName());
          asyncApiSampler.setBrokerConfiguration(mapVectorConf(brokerFieldModel.getDataVector()));
        }
        final var selectedSchema = (AsyncApiSchema) topicComboBox.getSelectedItem();
        if (Objects.nonNull(selectedSchema)) {
          asyncApiSampler.setAsyncApiSchemaName(selectedSchema.getTopicName());
          asyncApiSampler.setSchemaFieldConfiguration(mapVector(schemaFieldModel.getDataVector()));
        }
      }
      // asyncApiSampler.setAsyncApiRegistry((AsyncApiSR) registryComboBox.getSelectedItem());
    }
    configureTestElement(element);
  }

  private List<PropertyMapping> mapVectorConf(final Vector<Vector> dataVector) {
    final var mapResult = new ArrayList<PropertyMapping>();
    for (final Vector v : dataVector) {
      mapResult.add(PropertyMapping
                        .builder()
                        .propertyName(v.get(0).toString())
                        .propertyValue(v.get(1).toString())
                        .build());
    }
    return mapResult;
  }

  private List<FieldValueMapping> mapVector(final Vector<Vector> dataVector) {
    final var mapResult = new ArrayList<FieldValueMapping>();
    for (Vector v : dataVector) {
      mapResult.add(FieldValueMapping
                        .builder()
                        .fieldName(v.get(0).toString())
                        .fieldType(v.get(1).toString())
                        .valueLength(Integer.parseInt(v.get(2).toString()))
                        .fieldValueList(v.get(4).toString())
                        .required((Boolean) v.get(3))
                        .build());
    }
    return mapResult;
  }

  @Override
  public void configure(final TestElement element) {
    super.configure(element);
    if (element instanceof AsyncApiSampler) {
      asyncApiFile = ((AsyncApiSampler) element).getAsyncApiFileNode();
      if (Objects.nonNull(asyncApiFile)) {
        populateData((AsyncApiSampler) element);
      }
    }
  }

  private void actionFileChooser(final ActionEvent event) {

    final int returnValue = fileChooser.showDialog(mainPanel, JMeterUtils.getResString("file_visualizer_open"));

    if (JFileChooser.APPROVE_OPTION == returnValue) {
      try {
        final File apiFile = Objects.requireNonNull(fileChooser.getSelectedFile());
        asyncApiFileTextField.setText(apiFile.getAbsolutePath());
        asyncApiFile = asyncApiExtractor.processFile(apiFile);
        populateData();
      } catch (final KLoadGenException ex) {
        JOptionPane.showMessageDialog(mainPanel, "Error has occurred: " + ex.getMessage(), "Weird Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void populateData(final AsyncApiSampler element) {

    serverComboBox.removeAllItems();
    asyncApiFile.getApiServerMap().forEach((name, value) -> serverComboBox.addItem(value));
    serverComboBox.setSelectedIndex(0);
    final Collection<Pair<String, String>> propertyList = new ArrayList<>();
    element.getBrokerConfiguration()
           .forEach(propertyMapping ->
                        propertyList.add(Pair.of(propertyMapping.getPropertyName(), propertyMapping.getPropertyValue())));
    fillTable(brokerFieldModel, propertyList);
    /*final var schemaRegistryList = asyncApiExtractor.getSchemaRegistryData(asyncApiFile);
    if (!schemaRegistryList.isEmpty()) {
      fillTable(schemaRegistryFieldModel, prepareSRServer(schemaRegistryList.get(0)));
    }*/
    topicComboBox.removeAllItems();
    asyncApiExtractor.getSchemaDataMap(asyncApiFile).values().forEach(topicComboBox::addItem);
    topicComboBox.setSelectedIndex(0);
    fillTable(schemaFieldModel, element.getSchemaFieldConfiguration());
  }

  private void populateData() {
    if (Objects.nonNull(asyncApiFile)) {
      asyncApiFile.getApiServerMap().forEach((name, value) -> serverComboBox.addItem(value));
      serverComboBox.setSelectedIndex(0);
      fillTable(brokerFieldModel, prepareServer((AsyncApiServer) serverComboBox.getSelectedItem()));
      final var schemaRegistryList = asyncApiExtractor.getSchemaRegistryData(asyncApiFile);
      if (!schemaRegistryList.isEmpty()) {
        fillTable(schemaRegistryFieldModel, prepareSRServer(schemaRegistryList.get(0)));
      }
      asyncApiExtractor.getSchemaDataMap(asyncApiFile).values().forEach(topicComboBox::addItem);
      topicComboBox.setSelectedIndex(0);
    }
  }

  private Collection<Pair<String, String>> prepareSRServer(final AsyncApiSR schemaRegistryData) {
    final var srPropoMap = schemaRegistryData.getPropertiesMap();
    final var propertyList = new ArrayList<Pair<String, String>>();
    propertyList.add(Pair.of(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL, srPropoMap.get("url")));
    propertyList.add(Pair.of(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME, srPropoMap.get("type")));
    SchemaRegistryProperties.DEFAULTS.forEach(jmeterproperty -> propertyList.add(Pair.of(jmeterproperty.getName(), jmeterproperty.getPropertyValue())));
    return propertyList;
  }

  private Collection<Pair<String, String>> prepareServer(final AsyncApiServer asyncApiServer) {
    final var propertyList = new ArrayList<Pair<String, String>>();
    if (Objects.nonNull(asyncApiServer)) {
      final var serverProps = asyncApiServer.getPropertiesMap();
      propertyList.add(Pair.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverProps.get("url")));
      CollectionUtils.select(SamplerUtil
                                 .getCommonProducerDefaultParameters(),
                             property -> !property.getName().equalsIgnoreCase(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))
          .forEach(jmeterproperty -> extractArgument(jmeterproperty, propertyList));
    }
    return propertyList;
  }

  private static void extractArgument(final JMeterProperty jmeterproperty, final ArrayList<Pair<String, String>> propertyList) {
    final var argument = (Argument) jmeterproperty.getObjectValue();
    propertyList.add(Pair.of(argument.getPropertyAsString("Argument.name"), argument.getPropertyAsString("Argument.value")));
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
    // tabbedPanel.addTab("Registry", createRegistryTab());
    tabbedPanel.addTab("Schema", createSchemaTab());
    return tabbedPanel;
  }

  private void fillTable(final DefaultTableModel schemaFields, final Collection<Pair<String, String>> schemaData) {
    if (Objects.nonNull(schemaData)) {
      final var count = schemaFields.getRowCount();
      for (var i = 0; i < count; i++) {
        schemaFields.removeRow(0);
      }
      schemaData.forEach(data -> schemaFields.addRow(dataToRow(data)));
    }
  }

  private void fillTable(final DefaultTableModel schemaFields, final Object[] schemaData) {
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

  private void fillTable(final DefaultTableModel schemaFields, final List<FieldValueMapping> schemaData) {
    if (Objects.nonNull(schemaData)) {
      final var count = schemaFields.getRowCount();
      for (var i = 0; i < count; i++) {
        schemaFields.removeRow(0);
      }
      for (var data : schemaData) {
        schemaFields.addRow(data.getProperties());
      }
    }
  }

  private Object[] dataToRow(final Pair<String, String> data) {
    return new Object[] {data.getKey(), data.getValue()};
  }

  private JPanel createBrokerPanel() {
    final JPanel brokerPanel = new JPanel();
    brokerPanel.setLayout(new BorderLayout(0, 0));
    brokerPanel.add(new JLabeledTextField("Broker Server"));
    serverComboBox = new JComboBox<>();
    serverComboBox.setRenderer(new ApiServerRenderer());
    serverComboBox.addActionListener(this::serverChooseActionListener);
    brokerPanel.add(serverComboBox, BorderLayout.NORTH);
    brokerPanel.add(new JScrollPane(new JTable(brokerFieldModel)), BorderLayout.CENTER);
    return brokerPanel;
  }


  private JPanel createRegistryTab() {
    final JPanel registryUrlPanel = new JPanel();
    registryUrlPanel.setLayout(new BorderLayout(0, 0));
    registryUrlPanel.add(new JLabeledTextField("Schema Registry URL"));
    registryComboBox = new JComboBox<>();
    registryComboBox.setRenderer(new AsyncApiSRRenderer());
    registryComboBox.addActionListener(this::registryComboActionListener);
    registryUrlPanel.add(this.registryComboBox, BorderLayout.NORTH);
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
    final var schemaTable = new JTable(schemaFieldModel);
    schemaTab.add(new JScrollPane(schemaTable), BorderLayout.CENTER);
    return schemaTab;
  }

  private void topicComboActionListener(final ActionEvent event) {
    final JComboBox cb = (JComboBox) event.getSource();
    final var selectedSchema = (AsyncApiSchema) cb.getSelectedItem();
    if (Objects.nonNull(selectedSchema)) {
      fillTable(schemaFieldModel, selectedSchema.getProperties());
    }
  }

  private void registryComboActionListener(final ActionEvent actionEvent) {
  }

  private void serverChooseActionListener(final ActionEvent actionEvent) {
    final JComboBox serverChoose = (JComboBox) actionEvent.getSource();
    if (Objects.nonNull(serverChoose)) {
      fillTable(brokerFieldModel, prepareServer((AsyncApiServer) serverChoose.getSelectedItem()));
    }
  }
}
