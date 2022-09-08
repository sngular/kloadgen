/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.valuesimple;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public final class ValueSimpleConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String valueSchemaProperties;

  private String schema = "NoSchema";

  private String valueSubjectName = "";

  private String valueSerializerConfiguration;

  @Override
  public void iterationStart(final LoopIterationEvent loopIterationEvent) {

    final JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(PropsKeysHelper.VALUE_SUBJECT_NAME, valueSubjectName);
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_TYPE, schema);
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, getValueSchemaProperties());
    variables.putObject(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY, getValueSerializerConfiguration());
    variables.putObject(PropsKeysHelper.SIMPLE_VALUED_MESSAGE_KEY, Boolean.TRUE);
  }

  public String getValueSchemaProperties() {
    return getPropertyAsString("valueSchemaProperties");
  }

  public String getValueSerializerConfiguration() {
    return getPropertyAsString("valueSerializerConfiguration");
  }

  public void setValueSchemaProperties(final String value) {
    setProperty("valueSchemaProperties", value);
    this.valueSchemaProperties = value;
  }

  public void setValueSerializerConfiguration(final String valueSerializerConfiguration) {
    setProperty("valueSerializerConfiguration", valueSerializerConfiguration);
    this.valueSerializerConfiguration = valueSerializerConfiguration;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final ValueSimpleConfigElement that = (ValueSimpleConfigElement) o;
    return Objects.equals(getValueSchemaProperties(), that.getValueSchemaProperties())
           && Objects.equals(getValueSerializerConfiguration(), that.getValueSerializerConfiguration());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getValueSchemaProperties(), getValueSerializerConfiguration());
  }
}
