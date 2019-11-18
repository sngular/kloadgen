package net.coru.kloadgen.test;

import static org.assertj.core.api.Assertions.assertThat;

import net.coru.kloadgen.config.plaintext.PlainTextConfigElement;
import net.coru.kloadgen.config.serialized.SerializedConfigElement;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.sampler.KafkaSampler;
import net.coru.kloadgen.util.ProducerKeys;
import net.coru.kloadgen.util.PropsKeys;
import java.util.Collections;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.*;
import kafka.zk.EmbeddedZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by satish on 5/3/17.
 */
public class PepperBoxSamplerTest {

    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final String BROKERPORT = "9092";
    private static final String TOPIC = "test";

    private EmbeddedZookeeper zkServer = null;

    private KafkaServer kafkaServer = null;

    private ZkClient zkClient = null;

    private  JavaSamplerContext jmcx = null;

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
    public void plainTextSamplerTest() throws IOException {

        KafkaSampler sampler = new KafkaSampler();
        Arguments arguments = sampler.getDefaultParameters();
        arguments.removeArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        arguments.removeArgument(ProducerKeys.KAFKA_TOPIC_CONFIG);
        arguments.removeArgument(ProducerKeys.ZOOKEEPER_SERVERS);
        arguments.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKERHOST + ":" + BROKERPORT);
        arguments.addArgument(ProducerKeys.ZOOKEEPER_SERVERS, ZKHOST + ":" + zkServer.port());
        arguments.addArgument(ProducerKeys.KAFKA_TOPIC_CONFIG, TOPIC);

        jmcx = new JavaSamplerContext(arguments);
        sampler.setupTest(jmcx);

        PlainTextConfigElement plainTextConfigElement = new PlainTextConfigElement();
        plainTextConfigElement.setJsonSchema(TestInputUtils.testSchema);
        plainTextConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        plainTextConfigElement.iterationStart(null);

        Object msgSent = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        sampler.runTest(jmcx);

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.setProperty("group.id", "group0");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(30000);
        assertThat(records.count()).isEqualTo(1);
        for (ConsumerRecord<String, String> record : records){
            assertThat(record.value()).isEqualTo(msgSent.toString());
        }

        sampler.teardownTest(jmcx);

    }

    @Test
    public void plainTextKeyedMessageSamplerTest() throws IOException {

        KafkaSampler sampler = new KafkaSampler();
        Arguments arguments = sampler.getDefaultParameters();
        arguments.removeArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        arguments.removeArgument(ProducerKeys.KAFKA_TOPIC_CONFIG);
        arguments.removeArgument(ProducerKeys.ZOOKEEPER_SERVERS);
        arguments.removeArgument(PropsKeys.KEYED_MESSAGE_KEY);
        arguments.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKERHOST + ":" + BROKERPORT);
        arguments.addArgument(ProducerKeys.ZOOKEEPER_SERVERS, ZKHOST + ":" + zkServer.port());
        arguments.addArgument(ProducerKeys.KAFKA_TOPIC_CONFIG, TOPIC);
        arguments.addArgument(PropsKeys.KEYED_MESSAGE_KEY,"YES");

        jmcx = new JavaSamplerContext(arguments);
        sampler.setupTest(jmcx);

        PlainTextConfigElement keyConfigElement = new PlainTextConfigElement();
        keyConfigElement.setJsonSchema(TestInputUtils.testKeySchema);
        keyConfigElement.setPlaceHolder(PropsKeys.MSG_KEY_PLACEHOLDER);
        keyConfigElement.iterationStart(null);

        PlainTextConfigElement valueConfigElement = new PlainTextConfigElement();
        valueConfigElement.setJsonSchema(TestInputUtils.testSchema);
        valueConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        valueConfigElement.iterationStart(null);

        Object keySent = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_KEY_PLACEHOLDER);
        Object valueSent = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        sampler.runTest(jmcx);

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.setProperty("group.id", "group0");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(30000);
        assertThat(records.count()).isEqualTo(1);
        for (ConsumerRecord<String, String> record : records){
            assertThat(record.key()).isEqualTo(keySent.toString());
            assertThat(record.value()).isEqualTo(valueSent.toString());
        }

        sampler.teardownTest(jmcx);
    }

    @Test
    public void serializedSamplerTest() throws IOException {

        KafkaSampler sampler = new KafkaSampler();
        Arguments arguments = sampler.getDefaultParameters();
        arguments.removeArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        arguments.removeArgument(ProducerKeys.KAFKA_TOPIC_CONFIG);
        arguments.removeArgument(ProducerKeys.ZOOKEEPER_SERVERS);
        arguments.removeArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        arguments.addArgument(ProducerKeys.KAFKA_TOPIC_CONFIG, TOPIC);
        arguments.addArgument(ProducerKeys.ZOOKEEPER_SERVERS, ZKHOST + ":" + zkServer.port());
        arguments.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKERHOST + ":" + BROKERPORT);
        arguments.addArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.gslab.pepper.input.serialized.ObjectSerializer");

        jmcx = new JavaSamplerContext(arguments);
        sampler.setupTest(jmcx);

        List<FieldValueMapping> fieldValueMappings = TestInputUtils.getValueExpressionMappings();
        SerializedConfigElement serializedConfigElement = new SerializedConfigElement();
        serializedConfigElement.setClassName("com.gslab.pepper.test.Message");
        serializedConfigElement.setObjProperties(fieldValueMappings);
        serializedConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        serializedConfigElement.iterationStart(null);

        Message msgSent = (Message) JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        sampler.runTest(jmcx);

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.setProperty("group.id", "group0");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.setProperty("value.deserializer", "com.gslab.pepper.input.serialized.ObjectDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, Message> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(TOPIC));
        ConsumerRecords<String, Message> records = consumer.poll(30000);
        assertThat(records.count()).isEqualTo(1);
        for (ConsumerRecord<String, Message> record : records){
            assertThat(record.value().getMessageBody()).isEqualTo(msgSent.getMessageBody());
        }
        sampler.teardownTest(jmcx);
    }

    @Test
    public void serializedKeyMessageSamplerTest() throws IOException {

        KafkaSampler sampler = new KafkaSampler();
        Arguments arguments = sampler.getDefaultParameters();
        arguments.removeArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        arguments.removeArgument(ProducerKeys.KAFKA_TOPIC_CONFIG);
        arguments.removeArgument(ProducerKeys.ZOOKEEPER_SERVERS);
        arguments.removeArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        arguments.removeArgument(PropsKeys.KEYED_MESSAGE_KEY);
        arguments.addArgument(ProducerKeys.KAFKA_TOPIC_CONFIG, TOPIC);
        arguments.addArgument(ProducerKeys.ZOOKEEPER_SERVERS, ZKHOST + ":" + zkServer.port());
        arguments.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKERHOST + ":" + BROKERPORT);
        arguments.addArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.gslab.pepper.input.serialized.ObjectSerializer");
        arguments.addArgument(PropsKeys.KEYED_MESSAGE_KEY, "YES");
        jmcx = new JavaSamplerContext(arguments);
        sampler.setupTest(jmcx);

        List<FieldValueMapping> keyExpressionMappings = TestInputUtils.getKeyExpressionMappings();
        SerializedConfigElement keySerializedConfigElement = new SerializedConfigElement();
        keySerializedConfigElement.setClassName("com.gslab.pepper.test.MessageKey");
        keySerializedConfigElement.setObjProperties(keyExpressionMappings);
        keySerializedConfigElement.setPlaceHolder(PropsKeys.MSG_KEY_PLACEHOLDER);
        keySerializedConfigElement.iterationStart(null);

        List<FieldValueMapping> fieldValueMappings = TestInputUtils.getValueExpressionMappings();
        SerializedConfigElement valueSerializedConfigElement = new SerializedConfigElement();
        valueSerializedConfigElement.setClassName("com.gslab.pepper.test.Message");
        valueSerializedConfigElement.setObjProperties(fieldValueMappings);
        valueSerializedConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        valueSerializedConfigElement.iterationStart(null);

        MessageKey keySent = (MessageKey) JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_KEY_PLACEHOLDER);
        Message valueSent = (Message) JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        sampler.runTest(jmcx);

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.setProperty("group.id", "group0");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.setProperty("value.deserializer", "com.gslab.pepper.input.serialized.ObjectDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, Message> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(TOPIC));
        ConsumerRecords<String, Message> records = consumer.poll(30000);
        assertThat(records.count()).isEqualTo(1);
        for (ConsumerRecord<String, Message> record : records){
            assertThat(record.key()).isEqualTo(keySent.toString());
            assertThat(record.value().getMessageBody()).isEqualTo(valueSent.getMessageBody());
        }

        sampler.teardownTest(jmcx);

    }

    @AfterEach
    public void teardown(){
        kafkaServer.shutdown();
        zkClient.close();
        zkServer.shutdown();

    }

}
