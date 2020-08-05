/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.avroserialized;

import static net.coru.kloadgen.util.PropsKeysHelper.AVRO_SUBJECT_NAME;
import static net.coru.kloadgen.util.PropsKeysHelper.SCHEMA_PROPERTIES;

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
public class AvroSerializedConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String avroSubject;

  private List<FieldValueMapping> schemaProperties;

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {

      JMeterVariables variables = JMeterContextService.getContext().getVariables();
      variables.putObject(AVRO_SUBJECT_NAME, avroSubject);
      variables.putObject(SCHEMA_PROPERTIES, schemaProperties);
  }

}
