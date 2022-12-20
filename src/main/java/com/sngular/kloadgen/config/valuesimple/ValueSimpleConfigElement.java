/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.valuesimple;

import com.sngular.kloadgen.util.PropsKeysHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class ValueSimpleConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String messageValue;

  private String messageType;

  private String messageSerializerProperty;

  @Override
  public void iterationStart(final LoopIterationEvent loopIterationEvent) {

    final JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(PropsKeysHelper.MESSAGE_VALUE, messageValue);
    variables.putObject(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY, messageSerializerProperty);
    variables.putObject(PropsKeysHelper.MESSAGE_TYPE, messageType);
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_TYPE, "NoSchema");
  }

}
