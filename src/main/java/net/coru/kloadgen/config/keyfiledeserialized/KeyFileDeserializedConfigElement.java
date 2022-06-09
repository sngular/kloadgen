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

package net.coru.kloadgen.config.keyfiledeserialized;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.PropsKeysHelper;
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
public class KeyFileDeserializedConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String keySubjectName;

  private List<FieldValueMapping> keySchemaProperties;

  private String keySchemaDefinition;

  private String keySchemaType;

  private String keyDeserializerConfiguration;

  @Override
  public final void iterationStart(final LoopIterationEvent loopIterationEvent) {

    final JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(PropsKeysHelper.KEY_SCHEMA, keySchemaDefinition);
    variables.putObject(PropsKeysHelper.KEY_SCHEMA_PROPERTIES, keySchemaProperties);
    variables.putObject(PropsKeysHelper.KEY_SUBJECT_NAME, keySubjectName);
    variables.putObject(PropsKeysHelper.KEY_SCHEMA_TYPE, keySchemaType);
    variables.putObject(PropsKeysHelper.KEY_DESERIALIZER_CLASS_PROPERTY, keyDeserializerConfiguration);
    variables.putObject(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY, Boolean.TRUE);
  }

}
