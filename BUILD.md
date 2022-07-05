### GraalVM

For using Appkit from non-JVM languages (JS, Python, Ruby) GraalVM can be used (Community
or Enterprise edition). It can be [downloaded](https://www.graalvm.org/downloads/) and
installed on the computer where the App is supposed to run. Community
edition should be enough for Ergo Appkit library.

#### Install GraalVM Community Edition on MacOS

First you need to download an archive with the [latest
release](https://github.com/oracle/graal/releases) of GraalVM (e.g.
`graalvm-ce-darwin-amd64-19.2.1.tar.gz` at the time of writing) for MacOS and
put the programs from it onto the `$PATH`.

```shell
$ cd <your/directory/with/downloaded/graal>
$ tar -zxf graalvm-ce-darwin-amd64-19.2.1.tar.gz
$ export GRAAL_HOME=<your/directory/with/downloaded/graal>/graalvm-ce-19.2.1/Contents/Home
$ export PATH=$PATH:${GRAAL_HOME}/bin
```

GraalVM comes with a package manager called `gu` that lets you install
additional languages (JavaScript comes with GraalVM). You will also need to
install the `native-image` tool. These all get downloaded from GitHub.
```
$ gu install native-image
$ gu install ruby
$ gu install python
$ gu install R
```
When you run `java` or `js` you'll get the GraalVM versions of those runtimes.
```
$ java -version
openjdk version "1.8.0_232"
OpenJDK Runtime Environment (build 1.8.0_232-20191009173705.graal.jdk8u-src-tar-gz-b07)
OpenJDK 64-Bit GraalVM CE 19.2.1 (build 25.232-b07-jvmci-19.2-b03, mixed mode)

$ js --version
GraalVM JavaScript (GraalVM CE Native 19.2.1)
```

### Building the Appkit jar file

Appkit is
[published](https://mvnrepository.com/artifact/org.ergoplatform/ergo-appkit),
however, you can clone the whole repository build it and published locally in
the Ivy repository.

```shell
$ git clone https://github.com/ergoplatform/ergo-appkit.git
$ cd ergo-appkit
$ sbt publishLocal 
```

If you are working with Gradle/Maven, you might want to publish to your local Maven repository with

    sbt publishM2

In case you need a fat JAR (or uber JAR), you can build it with

    sbt clean assembly

and find it in the target/scala-* subfolder.

