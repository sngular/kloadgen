/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.kafkaheaders;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
  public final void iterationStart(final LoopIterationEvent iterEvent) {

    final JMeterContext context = getThreadContext();

    final Map<String, Object> threadVars = context.getSamplerContext();

    threadVars.put(ProducerKeysHelper.KAFKA_HEADERS, kafkaHeaders);
  }
}
