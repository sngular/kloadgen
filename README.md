# KLoadGen - Kafka + (Avro/Json Schema) Load Generator

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/85c9817742944668b5cc75e3fa1cdb23)](https://app.codacy.com/gh/corunet/kloadgen?utm_source=github.com&utm_medium=referral&utm_content=corunet/kloadgen&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://api.travis-ci.org/corunet/kloadgen.svg?branch=master)](https://travis-ci.org/corunet/kloadgen)

___

KLoadGen is kafka load generator plugin for jmeter designed to work with AVRO and JSON schema. It allows sending kafka messages with a structure defined as an AVRO Schema or a Json Schema. It connects to the Scheme Registry Server, retrieve the subject to send and generate a random message every time.

## Table of contents

* [Getting Started](#getting-started)
* [Producer setup](#producer-setup)
* [Consumer setup](#consumer-setup)
* [Example Test Plan](#example-test-plan)
* [StandAlone execution](#standalone-execution)
* [Special Thanks](#special-thanks)

## Getting Started

KLoadGen includes eight main components

* **Kafka Schema Sampler** : This jmeter java sampler sends messages to kafka, it uses the value and key configuration and generate a data matching that definition. 

* **Kafka Consumer Sampler** : This jmeter java sampler reads messages from kafka, it uses the value and key 
  configuration to deserialize read messages.

* **Kafka Headers Config** : This jmeter config element generates serialized object messages based on input class and its property
  configurations.

* **Value Serialized Config** : This jmeter config element generates plaintext messages based on input schema template designed.

* **Value File Serialized Config** : This jmeter config element allows to upload a value schema file instead to get it from the Schema
  Registry.
  
* **Value Deserialized Config** : This jmeter config element allows you to define how the value of a message is 
  deserialized.

* **Value Deserialized Config** : This jmeter config element allows to upload a value schema file to deserialize 
  messages.

* **Schema Registry Config** : This jmeter config element allows to configure the connection to a Schema Registry, security access,....

* **Key Serialized Config** : This jmeter config allows to configure a Key Schema from a Schema Registry

* **Key File Serialized Config** : This jmeter config allows to upload a key schema file instead to get it from the Schema Registry

* **Key Deserialized Config** : This jmeter config element allows you to define how the key of a message is
  deserialized.
  
* **Key File Deserialized Config** : This jmeter config allows to upload a key schema to deserialize message key.

* **Key Simple Config** : This jmeter config allows to define a simple basic key to send into de message.

### Setup

#### Requirement

KLoadGen uses Java, hence on JMeter machine JRE 8 or superior:

Install openjdk on Debian, Ubuntu, etc.,

```bash
 sudo apt-get install openjdk-8-jdk
```

Install openjdk on Fedora, Oracle Linux, Red Hat Enterprise Linux, etc.,

```bash
 su -c "yum install java-1.8.0-openjdk-devel"
```

For windows and mac and you can:

* download oracle JDK 11 setup from [here](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* using chocolatey (windows):
        <https://chocolatey.org/packages?q=java>
   brew (mac):

```bash
 brew tap adoptopenjdk/openjdk
 brew cask install adoptopenjdk11
```

#### Build Project

In order to build the project we have 2 profiles based on how you are gone to use it, as an standalone jar or as a Jmeter library under the lib/ext folder.
By default the build is go to generate the plugin jar, but to switch for standalone just pass it as parameter

```bash
 mvn clean install -P standalone
```

if you want to specify the plugin profile is like

```bash
 mvn clean install -P plugin
```
or just

```bash
 mvn clean install
```

Once build is completed, copy target/kloadgen-plugin-&lt;version&gt;.jar file to JMETER_HOME/lib/ext directory.

## Producer setup

### KLoadGenSampler

* **bootstrap.servers** : broker-ip-1:port, broker-ip-2:port, broker-ip-3:port
* **zookeeper.servers** : zookeeper-ip-1:port, zookeeper-ip-2:port, zookeeper-ip-3:port. _Optional_
* **kafka.topic.name** : Topic on which messages will be sent
* **keyed.message** : Enable adding a Key to the messages
* **compression.type** : kafka producer compression type(none/gzip/snappy/lz4)
* **batch.size** : messages batch size(increased batch size with compression like lz4 gives better throughput)
* **linger.ms** : How much maximum time producer should wait till batch becomes full(should be 5-10 when increased batch size and compression is enabled)
* **buffer.memory** : Total buffer memory for producer.
* **acks** : Message sent acknowledgement, value can be (0/1/-1).
* **send.buffer.bytes** : The size of the TCP send buffer (SO_SNDBUF) to use when sending data. If the value is -1, the OS default will be used.
* **receive.buffer.bytes** : The size of the TCP receive buffer (SO_RCVBUF) to use when reading data. If the value is -1, the OS default will be used.
* **security.protocol** : kafka producer protocol. Valid values are: PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL.
* **message.placeholder.key** : Config element message variable name. This name should be same as message placeholder key in serialized/plaintext config element.
* **kerberos.auth.enabled** : YES/NO if it is disabled all below properties will be ignored
* **java.security.auth.login.config** : jaas.conf of kafka Kerberos
* **java.security.krb5.conf** : Kerberos server krb5.conf file
* **sasl.kerberos.service.name** : Kafka Kerberos service name
* **sasl.mechanism** : Configure SASL mechanism to use to connect to the kafka cluster. GSSAPI by default
* **ssl.enabled** : SSL Flag enabled/disabled use of SSL. NO as Default.
* **ssl.key.password** : SSL password
* **ssl.keystore.location** : SSL Keystore location
* **ssl.keystore.password** : SSL Keystore password
* **ssl.truststore.location** : SSL Trust Store location
* **ssl.truststore.password** : SSL Trust Store password
* **client.id** : Kafka producer Client ID 
* **security.providers** : 
* **ssl.enabled.protocols** : SSL Enabled protocols TLSv1.2, TLSv1.3
* **ssl.endpoint.identification.algorithm** : SSL endpoint Identification algorithm. Leave default value is you don't want to sent it.
* **auto.register.schemas** : Allow or disallow SchemaRegistry Client to register the schema if missing

![Kafka Producer Configuration](/Kafka_Producer_Properties.png)

### Schema Registry Configuration

This screen will allow to configure the connection to the Schema Registry and retrieve the list of subjects hold there.
Server URL will be introduced and properties will be set in the table below. Only he actual set of properties are supported.

| Property | Values | Description |
|----------|--------|-------------|
| schema.registry.auth.enabled | YES/NO | Enable/Disable security configuration |
| schema.registry.auth.method | BASIC/BEARER | Authentication method type |
| schema.registry.username | String | The username |
| schema.registry.password | String | The password |
| schema.registry.bearer | String | The bearer for Token authentication |

The _Test Registry_ button will test the connection properties and **retrieve** the subjects list from the Schema Registry.

![Schema Registy Config](/Schema_Registry_Config.png)

Subject list will be used when configure the AVRO schema to download.

![Schema Registy Config Success](/Schema_Registry_Success.png)

A confirmation message will be show with the number of subjects retrieved from the Registry.

### Value Schema Configuration

This screen will allow to choose a subject and download it schema.
AVRO structure will be flattened and show in the table.
We will see 4 columns where we will configure the Random Generator system.

  * **Field Name** : Flattened field name compose by all the properties from the root class. Ex: PropClass1.PropClass2.PropClass3 **Note**: In case to be an array [] will appear at the end. If you want to define a specific size for the array just type the number.
  * **Field Type** : Field type, like String, Int, Double, Array **Note** : if the field is an array of basic types it will be show as string-array, int-array,...
  * **Field Length** : Field length configuration for the Random Tool. In case of an String mean the number of characters, in case of a Number the number of digits.
  * **Field Values List** : Field possibles values which will be used by the Random Tool to generate values.

![Load Generator Table](/Value_Schema_Load_Generator_Config.png)

### Value File Load Generator Configuration

This screen will allow to choose a schema from file(.avsc or.json).

![File Generator Table](/Value_Schema_File_Load_Generator_Config.png)

AVRO or Json structure will be flattened and show in the table.
We will see 4 columns where we will configure the Random Generator system.

  * **Field Name** : Flattened field name compose by all the properties from the root class. Ex: PropClass1.PropClass2.ProrpClass3 **Note**: In case to be an array [] will appear at the end. If you want to define a specific size for the array just type the number.
  * **Field Type** : Field type, like String, Int, Double, Map,Array **Note** : if the field is an array of basic types it will be show as string-array, int-array,...
  * **Field Length** : Field length configuration for the Random Tool. In case of an String mean the number of characters, in case of a Number the number of digits.
  * **Field Values List** : Field possibles values which will be used by the Random Tool to generate values

      **Note** In "Field Type" if the field type is an array or a map you can define a specific number of random values(metadata.extensions.flows[].correlation[2]).
              In "Field Values List" if the field type is an array or a map you can define a specific list of values([1,2,3,4,5] or [ key1:value1, key2:value2, key3:value3]).

### Key Schema Configuration

Similar to the Value Schema configuration element, but focus to configure a Key Schema. Whatever schema define and configure here will be used as a Key Message.

### Key File Load Generator Configuration

Similar to the Value File Schema configuration element, but focus to configure a Key Schema. Whatever schema define and configure here will be used as a Key Message.

### Key Simple Generator Configuration

Similar to the Value Schema configuration element, but focus to configure a Key Schema. Whatever schema define and configure here will be used as a Key Message.

![Key Plain Load Generator Config](/Key_Plain_Load_Generator_Config.png)

### Schema Template Functions

KLoadGen provides an easy way for random data generation base on the field type.

#### Data Types

| Type | Details |  Returns |
|----------|:-------:|:--------:|
| string | Field of String type | Random string with a longitude of 20 characters |
| int | Field of Int type | Random Integer |
| short | Field of short type | Random Short |
| long | Field of long type | Random Long |
| double | Field of double type | Random Double |
| float | Field of float type | Random Float |
| enum | Field of enum type | Random enum value bases on the AVRO enum type definition |
| stringTimestmap | Field of type String but coding a Timestmap | Localdatetime.now formatted as string |
| longTimestmap | Field of type Long but coding a Timestmap | Localdatetime.now formatted as long |
| boolean | Field of Boolean type | Ramdon bool value |

#### Special Data Types

| Type | Details |  Returns |
|----------|:-------:|:--------:|
| string-array | Field of type Array of String | Random size array of random generated String |
| int-array | Field of type Array of Int | Random size array of random generated Integers |
| long-array | Field of type Array of Long | Random size array of Random generated Longs |
| short-array | Field of type Array of Short | Random size array of Random generated Shorts |
| double-array | Field of type Array of Double | Random size array of Random generated Double |
| float-array | Field of type Array of Float | Random size array of Random generated Float |
| uuid-array | Field of type Array of UUIDs | Random size array of Random generated Uuid |
| boolean-array | Field of type Array of Boolean | Random size array of Random generated Boolean |
| string-map | Field of type Map of String, String | Random size map of random generated String, String |
| int-map | Field of type Map of String,  inter | Random size map of random generated String, Integers |
| long-map | Field of type Map of String, Long | Random size map of Random generated String, Longs |
| short-map | Field of type Map of String, Short | Random size map of Random generated String, Shorts |
| double-map | Field of type Map of String, Double | Random size map of Random generated String, Double |
| uuid-map | Field of type Map of String, UUIDs | Random size map of Random generated String, Uuid |
| boolean-map | Field of type Map of String, Boolean | Random size map of Random generated String, Boolean |
| Fixed | Field of type fixed | Random fixed Type |
| Bytes | Field of type bytes | Random size of Random generated bytes |

In case of (*)-array the data introduced in "Field Values List" will be used to generate example data. Format will be comma separated value list.
In case of (*)-map the data introduces in "Field Values List" will be used to generate example data. Format will be comma separated value list. Those values can be a set of pairs Key:value or a list of Keys.
Example:

* key1:value, key2:value, key3:value -- Will generate a map with data in between (key1:value, key2:value, key3:value)

* key1, key2, key3 -- Will generate a map with keys in between (key1,key2,key3) and value random generated

#### Special types

There is an special type, mix of map and array, (*)-map-array which will generate an array of maps. This specific type have a way to specify the size of boths collections. First, inline with the property name "[8]" will specify the array size, between brakes, as a normal array. For maps, the size will be specify in the field lenght column as property length as well.
Values in the "Field Values List" will refer *exclusively* to the map. So a list of "map values" must be there in order to generate a set to use by the random generator to select possible values.

#### Special functions

| Type | Details |  Returns |
|----------|:-------:|:--------:|
| seq | Generate a Numeric sequence starting in 1. Will cast to the AVRO Field Type | Returns a sequence starting in 1 |

In the sequence generator you can specify an starting value just put it in the Field Values List. It will only take the first value there,
others will be not consider.
Other values will be considered Constants for this field and will be converted to the Field Type. Keep that in mind to avoid Cast Exceptions

### Kafka Headers Configuration

This configuration component allow to specify a list of header which will be included in the producer. Headers specified here will be included in every message after be serialized.
Values will follow the same rules and the message body, if specify a type (basic type) it will generate a random value. If a value is set will be treated as String and serialize in the same way.

[Kafka Header Config](/Kafka_header_config_element.png)

## Consumer setup

### KLoadGenConsumerSampler

* **bootstrap.servers** : broker-ip-1:port, broker-ip-2:port, broker-ip-3:port
* **zookeeper.servers** : zookeeper-ip-1:port, zookeeper-ip-2:port, zookeeper-ip-3:port. _Optional_
* **kafka.topic.name** : Topic on which messages will be sent
* **send.buffer.bytes** : The size of the TCP send buffer (SO_SNDBUF) to use when sending data. If the value is -1, the OS default will be used.
* **receive.buffer.bytes** : The size of the TCP receive buffer (SO_RCVBUF) to use when reading data. If the value is -1, the OS default will be used.
* **auto.offset.reset** : The initial position for each assigned partition when the group is first created before 
  consuming any message. This value can be: earliest or latest.
* **security.protocol** : kafka producer protocol. Valid values are: PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL.
* **message.key.type** : Default message key type.
* **kerberos.auth.enabled** : YES/NO if it is disabled all below properties will be ignored
* **java.security.auth.login.config** : jaas.conf of kafka Kerberos
* **java.security.krb5.conf** : Kerberos server krb5.conf file
* **sasl.kerberos.service.name** : Kafka Kerberos service name
* **sasl.mechanism** : Configure SASL mechanism to use to connect to the kafka cluster. GSSAPI by default
* **value.subject.name.strategy** : Default value name strategy.
* **key.subject.name.strategy** : Default key name strategy.  
* **ssl.enabled** : SSL Flag enabled/disabled use of SSL. NO as Default.
* **ssl.key.password** : SSL password
* **ssl.keystore.location** : SSL Keystore location
* **ssl.keystore.password** : SSL Keystore password
* **ssl.truststore.location** : SSL Trust Store location
* **ssl.truststore.password** : SSL Trust Store password
* **client.id** : Kafka producer Client ID
* **security.providers** : 
* **ssl.enabled.protocols** : SSL Enabled protocols TLSv1.2, TLSv1.3
* **ssl.endpoint.identification.algorithm** : SSL endpoint Identification algorithm. Leave default value is you don't want to sent it.
* **ssl.keymanager.algorithm** : The algorithm used by key manager factory for SSL connections.
* **ssl.protocol** : The SSL protocol used to generate the SSLContext.
* **ssl.keystore.type** : Type of the repository of security certificates.
* **ssl.provider** : The name of the security provider used for SSL connections.
* **timeout.millis** : Max time trying to poll before considering current partitions empty.
* **max.poll.interval.ms** : The maximum delay between invocations of poll(). If poll() is not called before 
  expiration of this timeout, then the consumer is considered failed and the group will rebalance. This value should 
  be less than timeout.millis.
* **group.id** : Specifies the name of the consumer group the consumers will belong to.

![Kafka Consumer Configuration](/Kafka_Consumer_Properties.png)

>KLoadGen Consumer Sampler is compatible with **JSON** and **AVRO** messages. If **JSON** is being used, no extra 
> configuration is 
> needed, sampler will work with default deserialization settings. If **AVRO** is being used, some of the following 
> components must to be configured in order to define how messages will be deserialized.

### Value Schema Deserialization Configuration

This configuration component allows to specify a Name Strategy to get the correct schema from registry and use it to 
deserialize read messages. It also allows to select a deserializer, if Avro is being used,
**KafkaAvroDeserializer** must be chosen. (*io.confluent.kafka.serializers.KafkaAvroDeserializer*)

![Value Schema Deserialization Configuration](/Value_Schema_Deserialization_Config.png)

### Value Schema File Deserialization Configuration

This configuration component will allow you to select a schema from a file (.avsc)

AVRO structure will be flattened and shown in the table. You can see 2 columns:

* **Field Name** : Flattened field name compose by all the properties from the root class. Ex: PropClass1.
PropClass2.PropClass3 *Note* In case to be an array [] will appear at the end.
* **Field Type** : Field type, like String, Int, Double, Map, Array... **Note** : if the field is an array of basic 
  types it will be shown as string-array, int-array...
  
In this configuration screen you can also choose a deserializer. Make sure to **chose our custom deserializer** when 
you 
are using a schema loaded from a file. (*net.coru.kloadgen.serializer.AvroDeserializer*)

![Value Schema File Deserialization Configuration](/Value_Schema_File_Deserialization_Config.png)

### Key Schema Deserialization Configuration

Similar to Value Schema Deserialization Configuration, but focus to configure Key Schema deserialization. Whatever 
settings selected here will be used to deserialize message Keys.

### Key Schema File Deserialization Configuration

Similar to Value Schema File Deserialization Configuration, but focus to configure Key Schema deserialization. Whatever
settings selected here will be used to deserialize message Keys.

## Example Test Plan

[Here](/Example-Test-Plan.jmx) you can find an example of a JMeter Test Plan using the elements defined in this plugin. This test plan will only inject messages in a Kafka Cluster. **Before** execute it you should add your Schema Registry to retrieve the Schema in order to extract the Entity structure. In a future we will include support to read AVRO files.

## StandAlone execution

This plugin also support an StandAlone execution. Only requires a JMeter installation in order to read some configuration files:
* jmeter.properties
* saveservice.properties
* upgrade.properties

### Building

Build process required specify the standalone profile

```bash
$ mvn -P standalone clean install
```
### Execution

Execution in standalone mode is quite easy :

```bash
$ java -jar target/kloadgen-standalone-1.5.1.jar  -h ../JMeter/apache-jmeter-5.2\ 2 -l ../logs/results.log -t ../Example-Test-Plan.jmx -r ../logs
```

### Configuration Options

There are some mandatory configuration options:

- "h" or "jmeterHome" : Folder where reside a JMeter installation
- "t" or "testPlan" : Test plan file

And some optional ones who will let us configura the JMeter Engine and the test itself

- "o" or "optionalPros" : Optional properties file to pass to JMeter
- "r" or "reportOutput" : Report Output Folder
- "l" or "logFileName" : Jtl File where logs will be dump

## Special Thanks

* We would like to special thanks to [pepper-box](https://github.com/GSLabDev/pepper-box) for give us the base to create this plugin and the main ideas about how to face it.
