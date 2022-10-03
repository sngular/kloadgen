/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keysimple;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.util.JMeterHelper;
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
public final class KeySimpleConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String keyValue;
  private String schema = "NoSchema";
  private String keyType;
  private String keySerializerConfiguration;

  @Override
  public void iterationStart(final LoopIterationEvent loopIterationEvent) {

    final JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(PropsKeysHelper.KEY_VALUE, getKeyValue());
    variables.putObject(PropsKeysHelper.KEY_TYPE, getKeyType());
    variables.putObject(PropsKeysHelper.KEY_SCHEMA_TYPE, schema);
    variables.putObject(PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY, getKeySerializerConfiguration());
    variables.putObject(PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY, Boolean.TRUE);
  }

  public String getKeyValue() {
    return getPropertyAsString("keyValue");
  }

  public String getKeyType() {
    return getPropertyAsString("keyType");
  }

  public String getKeySerializerConfiguration() {
    return getPropertyAsString("keySerializerConfiguration");
  }

  public void setKeyValue(final String keyValue) {
    setProperty("keyValue", JMeterHelper.checkPropertyOrVariable(keyValue));
    this.keyValue = keyValue;
  }

  public void setKeyType(final String keyType) {
    setProperty("keyType", keyType);
    this.keyType = keyType;
  }

  public void setKeySerializerConfiguration(final String keySerializerConfiguration) {
    setProperty("keySerializerConfiguration", keySerializerConfiguration);
    this.keySerializerConfiguration = keySerializerConfiguration;
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
    final KeySimpleConfigElement that = (KeySimpleConfigElement) o;
    return Objects.equals(getKeyValue(), that.getKeyValue()) && Objects.equals(getKeyType(), that.getKeyType())
           && Objects.equals(getKeySerializerConfiguration(), that.getKeySerializerConfiguration());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getKeyValue(), getKeyType(), getKeySerializerConfiguration());
  }
}
