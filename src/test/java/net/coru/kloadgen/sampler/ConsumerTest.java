package net.coru.kloadgen.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;

import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerTest {

    private KafkaConsumerSampler consumerSampler;

    @Test
    void testConsumer(){
        consumerSampler = new KafkaConsumerSampler();
        Arguments arguments1 = consumerSampler.getDefaultParameters();
        arguments1.removeArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        arguments1.addArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        arguments1.addArgument(GROUP_ID_CONFIG, "anonymous");
        JavaSamplerContext javaSamplerContext = new JavaSamplerContext(arguments1);
        consumerSampler.setupTest(javaSamplerContext);

        SampleResult result = consumerSampler.runTest(javaSamplerContext);
        assertThat(result).isNotNull().hasFieldOrPropertyWithValue("success", false);
    }
}

