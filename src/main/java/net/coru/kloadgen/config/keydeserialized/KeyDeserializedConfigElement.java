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

package net.coru.kloadgen.config.keydeserialized;

import static net.coru.kloadgen.util.ProducerKeysHelper.KEY_NAME_STRATEGY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_DESERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.KEY_SCHEMA_TYPE;
import static net.coru.kloadgen.util.PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
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
public class KeyDeserializedConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private List<FieldValueMapping> keySchemaProperties;

  private String keySchemaType;

  private String keyDeserializerConfiguration;

  private String keyNameStrategy;

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {

    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(KEY_SCHEMA_PROPERTIES , keySchemaProperties);
    variables.putObject(KEY_SCHEMA_TYPE , keySchemaType);
    variables.putObject(KEY_DESERIALIZER_CLASS_PROPERTY , keyDeserializerConfiguration);
    variables.putObject(KEY_NAME_STRATEGY , keyNameStrategy);
    variables.putObject(SCHEMA_KEYED_MESSAGE_KEY , Boolean.TRUE);
  }

}
