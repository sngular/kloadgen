/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.util;

import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerKeysHelper {

    public static final String KAFKA_TOPIC_CONFIG = "kafka.topic.name";

    public static final String ZOOKEEPER_SERVERS = "zookeeper.servers";

    public static final String ZOOKEEPER_SERVERS_DEFAULT = "<Zookeeper List>";

    public static final String KAFKA_TOPIC_CONFIG_DEFAULT = "<Topic>";

    public static final String KEY_SERIALIZER_CLASS_CONFIG_DEFAULT = StringSerializer.class.getName();

    public static final String VALUE_SERIALIZER_CLASS_CONFIG_DEFAULT = StringSerializer.class.getName();

    public static final String ACKS_CONFIG_DEFAULT = "1";

    public static final String SEND_BUFFER_CONFIG_DEFAULT = "131072";

    public static final String RECEIVE_BUFFER_CONFIG_DEFAULT = "32768";

    public static final String BATCH_SIZE_CONFIG_DEFAULT = "16384";

    public static final String LINGER_MS_CONFIG_DEFAULT = "0";

    public static final String BUFFER_MEMORY_CONFIG_DEFAULT = "33554432";

    public static final String COMPRESSION_TYPE_CONFIG_DEFAULT = "none";

    public static final String BOOTSTRAP_SERVERS_CONFIG_DEFAULT = "<Broker List>";

    public static final String JAVA_SEC_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";

    public static final String JAVA_SEC_KRB5_CONFIG = "java.security.krb5.conf";

    public static final String SASL_KERBEROS_SERVICE_NAME = "sasl.kerberos.service.name";

    public static final String SASL_MECHANISM = "sasl.mechanism";

    public static final String SASL_MECHANISM_DEFAULT = "GSSAPI";

    public static final String JAAS_ENABLED = "jaas.enabled";

    public static final String JAVA_SEC_AUTH_LOGIN_CONFIG_DEFAULT = "<JAAS File Location>";

    public static final String JAVA_SEC_KRB5_CONFIG_DEFAULT = "<krb5.conf location>";

    public static final String SASL_KERBEROS_SERVICE_NAME_DEFAULT = "kafka";

    public static final String KERBEROS_ENABLED = "kerberos.auth.enabled";

    public static final String SSL_ENABLED = "ssl.enabled";

    public static final String FLAG_NO = "NO";

    public static final String FLAG_YES = "YES";

    public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

    public static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

    public static final String KAFKA_HEADERS = "kafka.headers";

    public static final String ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG = "auto.register.schemas";

    public static final String ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG_DEFAULT = "true";

    public static final String SAMPLE_ENTITY = "SampleEntity";

    public static final String VALUE_NAME_STRATEGY = "value.subject.name.strategy";

    public static final String RECORD_NAME_STRATEGY = "io.confluent.kafka.serializers.subject.RecordNameStrategy";

    public static final String TOPIC_NAME_STRATEGY = "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy";
}
