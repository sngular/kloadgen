package net.coru.kloadgen.test;

import static org.assertj.core.api.Assertions.assertThat;

import net.coru.kloadgen.model.FieldValueMapping;

import java.util.ArrayList;
import java.util.List;

public class TestInputUtils {

    public static String testSchema = "{\n" +
            "\t\"messageId\":{{SEQUENCE(\"messageId\", 1, 1)}},\n" +
            "\t\"messageBody\":\"{{RANDOM_ALPHA_NUMERIC(\"abcedefghijklmnopqrwxyzABCDEFGHIJKLMNOPQRWXYZ\", 100)}}\",\n" +
            "\t\"messageCategory\":\"{{RANDOM_STRING(\"Finance\", \"Insurance\", \"Healthcare\", \"Shares\")}}\",\n" +
            "\t\"messageStatus\":\"{{RANDOM_STRING(\"Accepted\",\"Pending\",\"Processing\",\"Rejected\")}}\",\n" +
            "\t\"messageTime\":{{TIMESTAMP()}}\n" +
            "}";

    public static String testKeySchema = "{\n" +
            "\t\"messageId\":{{SEQUENCE(\"messageId\", 1, 1)}}" +
            "}";

    public static String defectSchema = "{\n" +
            "\t\"messageId\":{{WRONG_FUNCTION(\"messageId\", 1, 1)}},\n" +
            "\t\"messageBody\":\"{{RANDOM_ALPHA_NUMERIC(\"abcedefghijklmnopqrwxyzABCDEFGHIJKLMNOPQRWXYZ\", 100)}}\",\n" +
            "\t\"messageCategory\":\"{{RANDOM_STRING(\"Finance\", \"Insurance\", \"Healthcare\", \"Shares\")}}\",\n" +
            "\t\"messageStatus\":\"{{RANDOM_STRING(\"Accepted\",\"Pending\",\"Processing\",\"Rejected\")}}\",\n" +
            "\t\"messageTime\":{{TIMESTAMP()}}\n" +
            "}";


    public static String producerProps = "bootstrap.servers=%s:%s\n" +
            "zookeeper.servers=%s:%d\n" +
            "kafka.topic.name=test\n" +
            "key.serializer=org.apache.kafka.common.serialization.StringSerializer\n" +
            "value.serializer=org.apache.kafka.common.serialization.StringSerializer\n" +
            "acks=0\n" +
            "send.buffer.bytes=131072\n" +
            "receive.buffer.bytes=32768\n" +
            "batch.size=16384\n" +
            "linger.ms=0\n" +
            "buffer.memory=33554432\n" +
            "compression.type=none\n" +
            "security.protocol=PLAINTEXT\n" +
            "kerberos.auth.enabled=NO\n" +
            "java.security.auth.login.config=<JAAS File Location>\n" +
            "java.security.krb5.conf=<krb5.conf location>\n" +
            "sasl.kerberos.service.name=<kerboros service name>";

    public static List<FieldValueMapping> getKeyExpressionMappings() {
        List<FieldValueMapping> fieldValueMappings = new ArrayList<>();
        FieldValueMapping fieldValueMapping = new FieldValueMapping();
        fieldValueMapping.setFieldName("messageId");
        fieldValueMapping.setValueExpression("SEQUENCE(\"messageId\", 1, 1)");

        assertThat(fieldValueMapping.getFieldName()).isNotNull();
        assertThat(fieldValueMapping.getValueExpression()).isNotNull();

        fieldValueMappings.add(fieldValueMapping);
        return fieldValueMappings;
    }

    public static List<FieldValueMapping> getValueExpressionMappings() {
        List<FieldValueMapping> fieldValueMappings = new ArrayList<>();
        FieldValueMapping fieldValueMapping = new FieldValueMapping();
        fieldValueMapping.setFieldName("messageId");
        fieldValueMapping.setValueExpression("SEQUENCE(\"messageId\", 1, 1)");

        assertThat(fieldValueMapping.getFieldName()).isNotNull();
        assertThat(fieldValueMapping.getValueExpression()).isNotNull();

        FieldValueMapping fieldValueMapping1 = new FieldValueMapping();
        fieldValueMapping1.setFieldName("messageBody");
        fieldValueMapping1.setValueExpression("\"Test Message\"");

        FieldValueMapping fieldValueMapping2 = new FieldValueMapping();
        fieldValueMapping2.setFieldName("messageStatus");
        fieldValueMapping2.setValueExpression("RANDOM_STRING(\"Accepted\",\"Pending\",\"Processing\",\"Rejected\")");

        FieldValueMapping fieldValueMapping3 = new FieldValueMapping();
        fieldValueMapping3.setFieldName("messageCategory");
        fieldValueMapping3.setValueExpression("RANDOM_STRING(\"Finance\",\"Insurance\",\"Healthcare\",\"Shares\")");

        FieldValueMapping fieldValueMapping4 = new FieldValueMapping();
        fieldValueMapping4.setFieldName("messageTime");
        fieldValueMapping4.setValueExpression("TIMESTAMP()");

        fieldValueMappings.add(fieldValueMapping);
        fieldValueMappings.add(fieldValueMapping1);
        fieldValueMappings.add(fieldValueMapping2);
        fieldValueMappings.add(fieldValueMapping3);
        fieldValueMappings.add(fieldValueMapping4);
        return fieldValueMappings;
    }

    public static List<FieldValueMapping> getWrongValueExpressionMappings() {
        List<FieldValueMapping> fieldValueMappings = new ArrayList<>();
        FieldValueMapping fieldValueMapping = new FieldValueMapping();
        fieldValueMapping.setFieldName("messageId");
        fieldValueMapping.setValueExpression("WRONG_FUNCTION()");
        fieldValueMappings.add(fieldValueMapping);
        return fieldValueMappings;
    }
}
