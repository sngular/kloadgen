/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.kafkaheaders;

import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_HEADERS;

import java.util.List;
import java.util.Map;

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
import org.apache.jmeter.threads.JMeterContext;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class KafkaHeadersConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private List<FieldValueMapping> kafkaHeaders;

  @Override
  public void iterationStart(LoopIterationEvent iterEvent) {

    JMeterContext context = getThreadContext();

    Map<String, Object> threadVars = context.getSamplerContext();

    threadVars.put(KAFKA_HEADERS , kafkaHeaders);
  }
}
