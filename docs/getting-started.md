# Getting started

Now that you know what KLoadGen is and that you are familiar with its components, you can install the plugin and put it to work.

## Prerequisites

You will need these tools to start using the plugin:

- JDK/JRE 11+
- JMeter 5.4+
- Maven

## Quickstart

If you want to start right away with your load tests, you can run this project as a standalone jar file.

### Standalone installation

After cloning the repository, run the following command:

```bash
 mvn clean install -P standalone
```
> In this installation, JMeter is needed in order to read some configuration files:
>
> - jmeter.properties
> - saveservice.properties
> - upgrade.properties> 

### Execution 

Execution in standalone mode is quite easy:

```bash
$ java -jar target/standalone.jar  -h ../JMeter/apache-jmeter-5.4\ 2 -l ../logs/results.log -t ../Example-Test-Plan.jmx -r ../logs
```

### Setup options

There are some mandatory setup options:

- "h" or "jmeterHome": folder where the JMeter installation resides
- "t" or "testPlan": test plan file

And some optional ones who will let you configure the JMeter engine and the test itself:

- "o" or "optionalPros": optional properties file to pass to JMeter
- "r" or "reportOutput": report output folder
- "l" or "logFileName": JTL file where logs will be dumped

Now you are ready to begin testing your loads!

If you prefer to install the project as a JMeter plugin jar file, see the [Installation as a JMeter library] page.

