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

package net.coru.kloadgen.config.valuedeserialized;

import static net.coru.kloadgen.util.ProducerKeysHelper.VALUE_NAME_STRATEGY;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_DESERIALIZER_CLASS_PROPERTY;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA_TYPE;

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
public class ValueDeserializedConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private List<FieldValueMapping> valueSchemaProperties;

  private String valueSchemaType;

  private String valueDeSerializerConfiguration;

  private String valueNameStrategy;

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {

    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(VALUE_SCHEMA_PROPERTIES , valueSchemaProperties);
    variables.putObject(VALUE_SCHEMA_TYPE , valueSchemaType);
    variables.putObject(VALUE_DESERIALIZER_CLASS_PROPERTY , valueDeSerializerConfiguration);
    variables.putObject(VALUE_NAME_STRATEGY , valueNameStrategy);

  }

}
