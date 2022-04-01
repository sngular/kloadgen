# Installation as a JMeter library

By default, the installation process will generate the JMeter plugin jar file. After cloning the repository, run the following command:

```bash
 mvn clean install -P plugin
```
or just:

```bash
 mvn clean install
```

Once the installation is complete, copy **target/kloadgen-&lt;version&gt;.jar** file to the `JMETER_HOME/lib/ext` directory.

You are now ready to create a new test plan in JMeter. See the specific sections for details on how to set them:

- [Producer configuration](producer-configuration.md#kafka-producer-sampler-configuration)
- [Consumer configuration](producer-configuration.md#kafka-consumer-sampler-configuration)

