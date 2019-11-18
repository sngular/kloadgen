package net.coru.kloadgen.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Properties;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka.zk.EmbeddedZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by satish on 5/3/17.
 */
public class PepperBoxLoadGenTest {
    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final String BROKERPORT = "9092";
    private static final String TOPIC = "test";

    private EmbeddedZookeeper zkServer = null;

    private KafkaServer kafkaServer = null;

    private ZkClient zkClient = null;

    @BeforeEach
    public void setup() throws IOException {

        zkServer = new EmbeddedZookeeper();

        String zkConnect = ZKHOST + ":" + zkServer.port();
        zkClient = new ZkClient(zkConnect, 30000, 30000, ZKStringSerializer$.MODULE$);
        ZkUtils zkUtils = ZkUtils.apply(zkClient, false);

        Properties brokerProps = new Properties();
        brokerProps.setProperty("zookeeper.connect", zkConnect);
        brokerProps.setProperty("broker.id", "0");
        brokerProps.setProperty("log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString());
        brokerProps.setProperty("listeners", "PLAINTEXT://" + BROKERHOST +":" + BROKERPORT);
        KafkaConfig config = new KafkaConfig(brokerProps);
        kafkaServer = TestUtils.createServer(config, new MockTime());
        //AdminUtils.createTopic(zkUtils, TOPIC, 1, 1, new Properties(), RackAwareMode.Disabled$.MODULE$);

        JMeterContext jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());

    }
    @Test
    public void consoleLoadGenTest() throws IOException {
        File schemaFile = File.createTempFile("json", ".schema");
        schemaFile.deleteOnExit();
        FileWriter schemaWriter = new FileWriter(schemaFile);
        schemaWriter.write(TestInputUtils.testSchema);
        schemaWriter.close();

        File producerFile = File.createTempFile("producer", ".properties");
        producerFile.deleteOnExit();
        FileWriter producerPropsWriter = new FileWriter(producerFile);
        producerPropsWriter.write(String.format(TestInputUtils.producerProps, BROKERHOST, BROKERPORT, ZKHOST, zkServer.port()));
        producerPropsWriter.close();

        String[] vargs = new String[]{"--schema-file", schemaFile.getAbsolutePath(), "--producer-config-file", producerFile.getAbsolutePath(), "--throughput-per-producer", "10", "--test-duration", "1", "--num-producers", "1"};
        QueAvrocadoLoadGenerator.main(vargs);

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.setProperty("group.id", "group");
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(30000);
        assertThat(records.count()).as("PepperBoxLoadGenerator validation failed").isPositive();
    }

    @AfterEach
    public void teardown(){
        kafkaServer.shutdown();
        zkClient.close();
        zkServer.shutdown();
    }
}
