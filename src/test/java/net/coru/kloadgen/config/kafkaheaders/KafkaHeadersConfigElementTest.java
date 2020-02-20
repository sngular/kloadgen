package net.coru.kloadgen.config.kafkaheaders;

import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.apache.jmeter.threads.JMeterContextService;
import org.junit.jupiter.api.Test;

class KafkaHeadersConfigElementTest {

  private KafkaHeadersConfigElement kafkaHeadersConfigElement;

  @Test
  public void testIterateStart() {
    kafkaHeadersConfigElement = new KafkaHeadersConfigElement();
    kafkaHeadersConfigElement.setKafkaHeaders(Collections.emptyList());
    kafkaHeadersConfigElement.iterationStart(null);

    Object kafkaHeaders = JMeterContextService.getContext().getSamplerContext().get(KAFKA_HEADERS);

    assertThat(kafkaHeaders).isNotNull();
    assertThat(kafkaHeaders).isInstanceOf(List.class);
  }
}