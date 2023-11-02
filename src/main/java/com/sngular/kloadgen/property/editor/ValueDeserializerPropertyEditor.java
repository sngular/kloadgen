/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.property.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;
import org.apache.kafka.common.serialization.Deserializer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

@Slf4j
public class ValueDeserializerPropertyEditor extends PropertyEditorSupport implements ActionListener, TestBeanPropertyEditor, ClearGui {

  private final JPanel panel = new JPanel();

  private JComboBox<String> deserializerComboBox;

  public ValueDeserializerPropertyEditor() {
    this.init();
  }

  public ValueDeserializerPropertyEditor(final Object source) {
    super(source);
    this.init();
    this.setValue(source);
  }

  public ValueDeserializerPropertyEditor(final PropertyDescriptor propertyDescriptor) {
    super(propertyDescriptor);
    this.init();
  }

  private void init() {

    fillDeserializer(new JComboBox<>());
    panel.setLayout(new BorderLayout());
    panel.add(deserializerComboBox);
    deserializerComboBox.addActionListener(this);
  }

  private void fillDeserializer(final JComboBox<String> objectJComboBox) {
    deserializerComboBox = objectJComboBox;
    final var reflections = new Reflections(
        new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forClass(Deserializer.class))
            .filterInputsBy(new FilterBuilder()
                                .includePackage(SerDesPackageValue.COM_SNGULAR_KLOADGEN_SERIALIZER)
                                .includePackage(SerDesPackageValue.IO_CONFLUENT_KAFKA_SERIALIZERS)
                                .includePackage(SerDesPackageValue.IO_APICURIO_REGISTRY_SERDE_AVRO_AVRO_KAFKA_DESERIALIZER)
                                .includePackage(SerDesPackageValue.IO_APICURIO_REGISTRY_SERDE_JSONSCHEMA_JSON_SCHEMA_KAFKA_DESERIALIZER)
                                .includePackage(SerDesPackageValue.IO_APICURIO_REGISTRY_SERDE_PROTOBUF_PROTOBUF_KAFKA_DESERIALIZER))
            .setScanners(Scanners.SubTypes));

    ReflectionUtils.extractDeserializers(deserializerComboBox, reflections, Deserializer.class);

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    // Not implementation required
  }

  @Override
  public void clearGui() {
    // Not implementation required
  }

  @Override
  public final void setDescriptor(final PropertyDescriptor descriptor) {
    super.setSource(descriptor);
  }

  @Override
  public final String getAsText() {
    return Objects.requireNonNull(this.deserializerComboBox.getSelectedItem()).toString();
  }

  @Override
  public final void setAsText(final String text) throws IllegalArgumentException {
    this.deserializerComboBox.setSelectedItem(text);
  }

  @Override
  public final Component getCustomEditor() {
    return this.panel;
  }

  @Override
  public final Object getValue() {
    return this.deserializerComboBox.getSelectedItem();
  }

  @Override
  public final void setValue(final Object value) {
    this.deserializerComboBox.setSelectedItem(Objects.requireNonNullElse(value, 0));
  }

  @Override
  public final boolean supportsCustomEditor() {
    return true;
  }
}
