# KLoadGen - Kafka + Avro Load Generator

[![Build Status](https://travis-ci.org/corunet/kloadgen.svg?branch=master)](https://travis-ci.org/corunet/kloadgen) [![Coverage Status](https://coveralls.io/repos/github/corunet/kloadgen/badge.svg?branch=master&maxAge=0)](https://coveralls.io/github/corunet/kloadgen?branch=master)

___

KLoadGen is kafka load generator plugin for jmeter designed to work with AVRO schema Registries. It allows to send kafka messages with a structure defined as an AVRO Subject. It connects to the Scheme Registry Server, retrieve the subject to send and generate a random messages every time.
## Getting Started
___

KLoadGen includes four main components

* **KLoadGen Kafka Sampler** : This jmeter java sampler sends messages to kafka. THere are 3 different samples base on the Serializer class used:
   * **ConfluentKafkaSampler** : Based on the Confluent Kafka Serializer
   * **Kafka Sampler** : Our own and simple Kafka Sampler
   * **Generic Kafka Sampler** : Simple Kafka Sampler where serializer is configure by properties.
* **KLoadGen Config** : This jmeter config element generates plaintext messages based on input schema template designed.
* **Kafka Headers Config** : This jmeter config element generates serialized object messages based on input class and its property configurations.

### Setup
___

#### Requirement

KLoadGen uses Java, hence on JMeter machine JRE 8 or superior:

Install openjdk on Debian, Ubuntu, etc.,
```
 sudo apt-get install openjdk-8-jdk
``` 

Install openjdk on Fedora, Oracle Linux, Red Hat Enterprise Linux, etc.,
```
 su -c "yum install java-1.8.0-openjdk-devel"
```
For windows and mac and you can:
 * download oracle JDK 8 setup from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
 * using chocolatey (windows):
        https://chocolatey.org/packages?q=java
   brew (mac):
```
 brew tap adoptopenjdk/openjdk 
 brew cask install adoptopenjdk8 
```


#### Build Project
```
 mvn clean install
```
Once build is completed, copy target/kloadgen-<version>.jar file to JMETER_HOME/lib/ext directory.

### KLoadGenSampler
___

* **bootstrap.servers** : broker-ip-1:port, broker-ip-2:port, broker-ip-3:port
* **zookeeper.servers** : zookeeper-ip-1:port, zookeeper-ip-2:port, zookeeper-ip-3:port. _Optional_
* **kafka.topic.name** : Topic on which messages will be sent
* **key.serializer** : Key serializer (This is optional and can be kept as it is as we are not sending keyed messages).
* **value.serializer** : For plaintext config element value can be kept same as default but for serialized config element, value serializer can be "ObjectSerializer"
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
* **auto.register.schemas** : Allow or disallow SchemaRegistry Client to register the schema if missing.

Above properties are added by default in sampler as those are more significant in terms of performance in most of the cases. But you can add other non listed kafka properties with prefix "_".

For example to enable SSL properties you can add below properties

```
_ssl.key.password
_ssl.keystore.location
_ssl.keystore.password
_ssl.truststore.location
_ssl.truststore.password

```

![Kafka Producer Configuration](/Kafka_producer_properties.png)


### Load Generator Configuration
---
This screen will allow JMeter connect to a Schema Registry and download the specified subject. AVRO sctructure will be flattened and show in the lower table. 
On that Table we will see 4 columns where we will configure the Random Generator system.

* **Field Name** : Flattened field name compose by all the properties from the root class. Ex: PropClass1.PropClass2.ProrpClass3 **Note**: In case to be an array [] will appear at the end. If you want to define a specific size for the array just type the number.
* **Field Type** : Field type, like String, Int, Double, Array **Note** : if the field is an array of basic types it will be show as string-array, int-array,...
* **Field Length** : Field length configuration for the Random Tool. In case of an String mean the number of characters, in case of a Number the number of digits.
* **Field Values List** : Field possibles values which will be used by the Random Tool to generate values.

![Load Generator Table](/Kafka_load_generator_config.png)

### Schema Template Functions
___

KLoadGen provides an easy way for random data generation base on the field type.

| Type | Details |  Returns |
|----------|:-------:|:--------:|
| string | Field of String type| Random string with a longitude of 20 characters |
| int | Field of Int type | Random Integer |
| short | Field of short type | Random Short |
| long | Field of long type | Random Long |
| double | Field of double type | Random Double |
| enum | Field of enum type | Random enum value bases on the AVRO enum type definition |
| stringTimestmap | Field of type String but coding a Timestmap | Localdatetime.now formatted as string |
| longTimestmap | Field of type Long but coding a Timestmap | Localdatetime.now formatted as long |
| string-array | Field of type Array of String | Random size array of random generated String |
| int-array | Field of type Array of String | Random size array of random generated Integers |
| long-array | Field of type Array of Long | Random size array of Random generated Longs |
| short-array | Field of type Array of Short | Random size array of Random generated Shorts |
| double-array | Field of type Array of Double | Random size array of Random generated Double |

Other values will be considered Constants for this field and will be converted to the Field Type. Keep that in mind to avoid Cast Exceptions

### Kafka Headers Configuration
---
This configuration component allow to specify a list of header which will be included in the producer. Headers specified here will be included in every message after be serialized.
Values will follow the same rules and the message body, if specify a type (basic type) it will generate a random value. If a value is set will be treated as String and serialize in the same way.

[Kafka Header Config](/Kafka_header_config_element.png)

### Example Test Plan

[Here](/Example-Test-Plan.jmx) you can find an example of a JMeter Test Plan using the elements defined in this plugin. This test plan will only inject messages in a Kafka Cluster. **Before** execute it you should add your Schema Registry to retrieve the Schema in order to extract the Entity structure. In a future we will include support to read AVRO files.

## Special Thanks!

* We would like to special thanks to [pepper-box
](https://github.com/GSLabDev/pepper-box) for give us the base to create this plugin and the main ideas about how to face it.