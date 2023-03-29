# Installation 

This guide helps you install KLoadGen plugin.

## Install as a JMeter library

By default, the installation process will generate the JMeter plugin JAR file. After cloning the repository, run the following command where the `pom.xml` file is located:

```bash
 mvn clean install -P plugin
```
or just:

```bash
 mvn clean install
```

Once the installation is complete, copy `target/kloadgen-<version>.jar` file to the `JMETER_HOME/lib/ext` directory.

You are now ready to create a new test plan in JMeter. See the specific sections for details on how to set them:

- [Producer configuration](producer-configuration.md#kafka-producer-sampler-configuration)
- [Consumer configuration](producer-configuration.md#kafka-consumer-sampler-configuration)

## Download a prebuilt version from Maven

You can also download a prebuilt version of this plugin from [Maven Central](https://mvnrepository.com/artifact/com.sngular/kloadgen).

1. Choose the version you want to install.
2. Choose the build tool you want to use.
3. Copy the corresponding link.
4. Paste it into your CLI.

## Install as a dependency

This plugin can also be used automatically as a dependency. Include this code in your `pom.xml` file:

```
<dependency>
    <groupId>com.sngular</groupId>
    <artifactId>kloadgen</artifactId>
    <version>_VERSION_NUMBER_</version>
</dependency>
```