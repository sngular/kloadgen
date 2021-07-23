package net.coru.kloadgen.sampler;

import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

import com.salesforce.kafka.test.junit5.SharedKafkaTestResource;
import com.salesforce.kafka.test.listeners.PlainListener;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ConsumerTest {

  @RegisterExtension
  public static final SharedKafkaTestResource sharedKafkaTestResource =
          new SharedKafkaTestResource()
            .registerListener(new PlainListener().onPorts(1234))
            .withBrokers(1)
            .withBrokerProperty("auto.create.topics.enable", "false");

  private KafkaConsumerSampler consumerSampler;

  @Test
  void testConsumer() throws TimeoutException {
    consumerSampler = new KafkaConsumerSampler();
    Arguments consumerArguments = consumerSampler.getDefaultParameters();
    consumerArguments.removeArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
    consumerArguments.addArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                  sharedKafkaTestResource.getKafkaBrokers().asList().get(0).getConnectString());
    consumerArguments.addArgument(GROUP_ID_CONFIG, "anonymous");
    consumerArguments.removeArgument(KAFKA_TOPIC_CONFIG);
    consumerArguments.addArgument(KAFKA_TOPIC_CONFIG, "testTopic");
    consumerArguments.removeArgument("timeout.millis");
    consumerArguments.addArgument("timeout.millis", "20000");
    consumerArguments.addArgument(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization" +
            ".StringDeserializer");
    consumerArguments.addArgument(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization" +
            ".StringDeserializer");
    consumerArguments.addArgument(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    JavaSamplerContext javaSamplerContext = new JavaSamplerContext(consumerArguments);

    LoopController loopController = new LoopController();
    loopController.setLoops(2);
    ThreadGroup tg = new ThreadGroup();
    tg.setSamplerController(loopController);
    javaSamplerContext.getJMeterContext().setThreadGroup(tg);

    sharedKafkaTestResource.getKafkaTestUtils().waitForBrokerToComeOnLine(1, 10, TimeUnit.SECONDS);
    sharedKafkaTestResource.getKafkaTestUtils().createTopic("testTopic", 2, (short) 1);
    consumerSampler.setupTest(javaSamplerContext);

    sharedKafkaTestResource.getKafkaTestUtils().produceRecords(Map.of("key".getBytes(), "value".getBytes()),
            "testTopic", 0);
    SampleResult result = consumerSampler.runTest(javaSamplerContext);

    assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("success", true);
    assertThat(result.getResponseDataAsString()).isEqualToIgnoringCase("{ partition: 0, message: { key: key, " +
            "value: value }}");

    result = consumerSampler.runTest(javaSamplerContext);
    assertThat(result).isNull();

    result = consumerSampler.runTest(javaSamplerContext);
    assertThat(result).isNotNull().hasFieldOrPropertyWithValue("success",false);
  }
}

