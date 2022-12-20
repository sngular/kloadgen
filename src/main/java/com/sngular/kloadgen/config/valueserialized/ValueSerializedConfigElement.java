/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.valueserialized;

import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.util.ProducerKeysHelper;
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

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public final class ValueSerializedConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String valueSubjectName;

  private List<FieldValueMapping> valueSchemaProperties;

  private String valueSchemaType;

  private String valueSerializerConfiguration;

  private String valueNameStrategy;

  @Override
  public void iterationStart(final LoopIterationEvent loopIterationEvent) {

    final JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(PropsKeysHelper.VALUE_SUBJECT_NAME, valueSubjectName);
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES, valueSchemaProperties);
    variables.putObject(PropsKeysHelper.VALUE_SCHEMA_TYPE, valueSchemaType);
    variables.putObject(PropsKeysHelper.VALUE_SERIALIZER_CLASS_PROPERTY, valueSerializerConfiguration);
    variables.putObject(ProducerKeysHelper.VALUE_NAME_STRATEGY, valueNameStrategy);
  }
}
