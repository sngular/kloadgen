/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keysimple;

import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_VALUE;
import static net.coru.kloadgen.util.PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY;

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

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class KeySimpleConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String keyValue;

  private String keyType;

  private String keySerializerConfiguration;

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {

    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(KEY_VALUE, keyValue);
    variables.putObject(KEY_TYPE, keyType);
    variables.putObject(KEY_SERIALIZER_CLASS_PROPERTY, keySerializerConfiguration);
    variables.putObject(SIMPLE_KEYED_MESSAGE_KEY, Boolean.TRUE);
  }

}
