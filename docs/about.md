# About KLoadGen

KLoadGen is a plugin for JMeter which acts as a load generator designed to send Kafka messages and that supports both AVRON and JSON structures. 

It can also connect to the Schema Registry server, where it retrieves the subjects to send.

## Benefits

- Test your performance and the business logic. You can easily test both the performance of your system and the business logic behind your data.
- Define schemas for your test. You can specify a file with the schema or configure the connection to the Schema Registry server on the go.
- Generate synthetic data. You can set schema constraints to generate random data that matches your structure.
- Generate key values. You can also define keys that match your structure while improving your cluster performance.
- Dependency-free. You don't need external libraries and there are no other dependencies; the plugin automatically embeds the latest supported version of Kafka libraries.
- AVRO and JSON. This plugin supports both AVRO and JSON schemas.
- Arrays and maps. KLoadgen generates messages both from basic data types and from complex structures, such as arrays or maps.

## Product roadmap

KLoadGen is a stable plugin generally available to everyone who would like to use it. 

We are open to your suggestions and ideas to improve our product, so feel free to contact us and give us your feedback. Take a look at the [CONTRIBUTING.MD](https://github.com/corunet/kloadgen/CONTRIBUTING.MD) file to know how you can contribute.

## Tool story

<!--Write a tool story for the plugin

You can use this page to briefly explain the story of your product.

Try to write precisely that, a "story", because it will be more engaging and easy-to-read.-->