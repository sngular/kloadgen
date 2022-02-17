[![Codacy Badge](https://api.codacy.com/project/badge/Grade/85c9817742944668b5cc75e3fa1cdb23)](https://app.codacy.com/gh/corunet/kloadgen?utm_source=github.com&utm_medium=referral&utm_content=corunet/kloadgen&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://api.travis-ci.org/corunet/kloadgen.svg?branch=master)](https://travis-ci.org/corunet/kloadgen)

[Summary](#summary) · [Getting started](#getting-started) · [Usage](#usage) · [Support](#support) · [Special thanks](#special-thanks) 

## 📜 Summary

KLoadGen is a Kafka load generator plugin for JMeter designed to work with AVRO and JSON schemas. It allows to send Kafka messages with a structure defined as an AVRO schema or a JSON schema. It connects to the Schema Registry server, retrieves the subject to send, and generates a random message every time.

### Documentation

You can learn more details on our [wiki](https://github.com/corunet/kloadgen/wiki). 


### Technology

KLoadGen is a plugin based on Java that needs a JMeter machine running JRE  8 or higher.

## 🚀 Getting Started

Check our [Getting Started](https://github.com/corunet/kloadgen/wiki#getting-started) section for details on:

- KLoadGen components
- Plugin requirements
- Installation process

## 🧑🏻‍💻 Usage

### Configuration

See the sections [Producer setup](https://github.com/corunet/kloadgen/wiki#producer-setup) and [Consumer setup](https://github.com/corunet/kloadgen/wiki#consumer-setup) on our wiki for details on how to set up both ends of the message, producer and consumer, for this plugin.

### Deployment

Take a look at the [Example test plan](https://github.com/corunet/kloadgen/wiki#example-test-plan) section to see the contents of the Java extension and at the [Standalone execution](https://github.com/corunet/kloadgen/wiki#standalone-execution) section to see how to work with a standalone profile.

## 🧰 Support

We’ll be glad to talk and discuss how KLoadGen can help you 😊

Reach us through [GitHub issues](https://github.com/corunet/kloadgen/issues), [email](mailto:info@corunet.com) or [Twitter](https://twitter.com/corunet).

## 💜 Special thanks

We would like to give special thanks to [pepper-box](https://github.com/GSLabDev/pepper-box) for giving us the base to create this plugin and the main ideas on how to face it.
