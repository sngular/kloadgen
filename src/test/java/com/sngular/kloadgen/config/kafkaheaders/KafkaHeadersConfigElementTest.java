/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.kafkaheaders;

import java.util.Collections;
import java.util.List;

import com.sngular.kloadgen.util.ProducerKeysHelper;
import org.apache.jmeter.threads.JMeterContextService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaHeadersConfigElementTest {

  @Test
  final void testIterateStart() {
    final var kafkaHeadersConfigElement = new KafkaHeadersConfigElement();
    kafkaHeadersConfigElement.setKafkaHeaders(Collections.emptyList());
    kafkaHeadersConfigElement.iterationStart(null);

    final Object kafkaHeaders = JMeterContextService.getContext().getSamplerContext().get(ProducerKeysHelper.KAFKA_HEADERS);

    Assertions.assertThat(kafkaHeaders).isNotNull().isInstanceOf(List.class);
  }
}