/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.keysimple;

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
public final class KeySimpleConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String keyValue;

  private String keyType;

  private String keySerializerConfiguration;

  @Override
  public void iterationStart(final LoopIterationEvent loopIterationEvent) {

    final JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(PropsKeysHelper.KEY_VALUE, keyValue);
    variables.putObject(PropsKeysHelper.KEY_TYPE, keyType);
    variables.putObject(PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY, keySerializerConfiguration);
    variables.putObject(PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY, Boolean.TRUE);
  }
}
