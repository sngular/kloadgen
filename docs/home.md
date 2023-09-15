# About KLoadGen

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/85c9817742944668b5cc75e3fa1cdb23)](https://app.codacy.com/gh/corunet/kloadgen?utm_source=github.com&utm_medium=referral&utm_content=corunet/kloadgen&utm_campaign=Badge_Grade_Dashboard)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.coru/kloadgen/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.coru/kloadgen)

KLoadGen is a plugin for JMeter which acts as a load generator designed to send Kafka messages and that supports AVRO, JSON Schema and PROTOBUF structures.

It can also connect to the Schema Registry server, where it retrieves the subjects to send.

## Benefits

- Test your performance and the business logic. You can easily test both the performance of your system and the business logic behind your data.
- Define schemas for your test. You can specify a file with the schema or configure the connection to the Schema Registry server on the go.
- Generate synthetic data. You can set schema constraints to generate random data that matches your structure.
- Generate key values. You can also define keys that match your structure while improving your cluster performance.
- Dependency-free. You don't need external libraries and there are no other dependencies; the plugin automatically embeds the latest supported version of Kafka libraries.
- AVRO, JSON Schema and PROTOBUF. This plugin supports these three structures.
- Arrays and maps. KLoadGen generates messages both from basic data types and from complex structures, such as arrays or maps.

## Product roadmap

KLoadGen is a stable plugin generally available.

Current evolution is centered on solving issues and bugs that can arise, improving the current flows and refactoring the code to gain on readability.
