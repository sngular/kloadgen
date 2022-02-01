/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.kafkaheaders;

import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.apache.jmeter.threads.JMeterContextService;
import org.junit.jupiter.api.Test;

class KafkaHeadersConfigElementTest {

  @Test
  public void testIterateStart() {
    KafkaHeadersConfigElement kafkaHeadersConfigElement = new KafkaHeadersConfigElement();
    kafkaHeadersConfigElement.setKafkaHeaders(Collections.emptyList());
    kafkaHeadersConfigElement.iterationStart(null);

    Object kafkaHeaders = JMeterContextService.getContext().getSamplerContext().get(KAFKA_HEADERS);

    assertThat(kafkaHeaders).isNotNull();
    assertThat(kafkaHeaders).isInstanceOf(List.class);
  }
}