[![Codacy Badge](https://api.codacy.com/project/badge/Grade/85c9817742944668b5cc75e3fa1cdb23)](https://app.codacy.com/gh/corunet/kloadgen?utm_source=github.com&utm_medium=referral&utm_content=corunet/kloadgen&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://api.travis-ci.org/corunet/kloadgen.svg?branch=master)](https://travis-ci.org/corunet/kloadgen)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.coru/kloadgen/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.coru/kloadgen)

<p align="center">
<a href="#-summary">Summary</a> · 
<a href="#-getting-started">Getting started</a> · 
<a href="#-usage">Usage</a> · 
<a href="#-technical-design">Technical design</a> · 
<a href="#-support">Support</a> · 
<a href="#-special-thanks">Special thanks</a> 
</p> 

## 📜 Summary

KLoadGen is a Kafka load generator plugin for JMeter designed to work with AVRO, JSON Schema, and PROTOBUF structures for sending Kafka messages. It connects to the Schema Registry server, retrieves the subject to send, and generates a random message every time.

Check our [wiki](https://github.com/corunet/kloadgen/wiki) for more details on KLoadGen. 

## 🚀 Getting Started

Take a look at the [prerequisites](https://github.com/corunet/kloadgen/wiki/getting-started#prerequisites) for KLoadGen. 

If you want to start right away with your load tests, follow the [quickstart](https://github.com/corunet/kloadgen/wiki/getting-started#quickstart) and see [how to run a test plan](https://github.com/corunet/kloadgen/wiki/run-test-plan).

### Installation

Check how to install KLoadGen as a JMeter library, how to download a prebuilt version from Maven, and how to install the plugin as a dependency in the [Installation](https://github.com/corunet/kloadgen/wiki/installation) page.

## 🧑🏻‍💻 Usage

### Configuration

See the [Producer configuration](https://github.com/corunet/kloadgen/wiki/producer-configuration) and [Consumer configuration](https://github.com/corunet/kloadgen/wiki/consumer-configuration) pages on our wiki for details on how to set up both ends of the message, producer and consumer, for this plugin.

### Schemas

KLoadGen supports schemas with both primitive and complex data types, including arrays, maps or a combination of both.

It also allows creating custom sequences of values and supports null values.

You can find more details in [Schemas](https://github.com/corunet/kloadgen/wiki/schemas).

## 📊 Technical design

See the architecture and project structure of KLoadGen in the [Architecture](https://github.com/corunet/kloadgen/wiki/architecture) page.

## 🧰 Support

We’ll be glad to talk and discuss how KLoadGen can help you 😊

Reach us through [GitHub issues](https://github.com/corunet/kloadgen/issues), [GitHub discussions](https://github.com/corunet/kloadgen/discussions), [email](mailto:info@corunet.com) or [Twitter](https://twitter.com/corunet).

## 💜 Special thanks

We would like to give special thanks to [pepper-box](https://github.com/GSLabDev/pepper-box) for giving us the base to create this plugin and the main ideas on how to face it.
