# Debuging

This guide helps you debug KLoadGen in plugin mode with an IDE.

## Using IntelliJ IDEA

- Create a remote JVM configuration, should look like this:

![debug-configuration.png](images%2Fdebug-configuration.png)

- Now copy the Command line argument.

## - Windows

Open the file: `apache-jmeter-<version>\bin\jmeter.bat` whit any editor and paste the command on this 2 lines:

![debug-jmeter-bat.png](images%2Fdebug-jmeter-bat.png)


## - MacOS

Open the file: `apache-jmeter-<version>\bin\jmeter.sh` whit any editor and paste the command on this line:

![debug-jmeter-sh.png](images%2Fdebug-jmeter-sh.png)


## While Debuging

Now you can debug while you ejecute Jmeter.

Remember to create a new `kloadgen-<version>.jar` file and move it to Jmeter files, to apply
the changes you made in the code.