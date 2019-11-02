# Polyglot: A Library for Polyglot Development of Ergo Applications 

[Ergo](https://ergoplatform.org/en/) is a resilient blockchain platform for
contractual money. In addition to [Bitcoin](https://bitcoin.org/en/)-like
blockchain architecture Ergo provides advanced contractual capabilities, which
are not possible in bitcoin.

Because of these capabilities numerous decentralized [applications]() become
possible. Ergo applications can avoid centralization of trust and instead rely
on trust-less truly decentralized protocols which are implemented using Ergo
contracts deployed on Ergo blockchain.

By its very nature, Ergo applications are both decentralized and cross-platform
ranging from web applications running in browsers, mobile applications running
on Android and iOS, standalone applications executing on Linux, MacOS or Windows
to services deployed in cloud servers.

That said, it is very desirable to provide consistent programming model and APIs
to facilitate all those application scenarios and platforms.

This library is based on [GraalVM](https://www.graalvm.org) - a novel next
generation approach to implement software which is reusable across several
programming languages and execution environment.

Polyglot library is written in Java and is a thin wrapper around core
components provided by [ErgoScript
interpreter](https://github.com/ScorexFoundation/sigmastate-interpreter) and
[Ergo protocol](https://github.com/ergoplatform/ergo) implementations which are
written in Scala.

Using Polyglot Ergo applications can be written in one of the languages
supported by GraalVM (i.e. Java, JavaScript, C/C++, Python, Ruby, R) and using
this library applications can communicate with Ergo nodes via unified API and
programming model provided by Polyglot. Jump to [setup instructions](#setup) if
you want get started right away.

## Usage Example 

Among other things, Polyglot library allows to communicate with Ergo nodes via
REST API. Let's see how we can write a simple Java console application which
uses Polyglot library. The application will create and send a new transaction
using Ergo node which is started locally and available at
`http://localhost:9052/`. Suppose we [set up a full
node](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node) and started
it using the following command.
```
java -jar -Xmx4G -Dlogback.stdout.level=WARN target/scala-2.12/ergo-3.1.3.jar --mainnet -c ergo.conf
```

In order to connect to this node in our Java application 



## Setup

Polyglot require GraalVM (Community or Enterprise edition) to be
[downloaded](https://www.graalvm.org/downloads/) and installed. 
Community edition should be enough for Ergo Polyglot library. 

#### Install GraalVM Community Edition on MacOS

First you need to download an archive with the [latest
release](https://github.com/oracle/graal/releases) of GraalVM (e.g.
`graalvm-ce-darwin-amd64-19.2.1.tar.gz`) for MacOS and put the programs from it
onto the `$PATH`.

```
cd <your/directory/with/downloaded/graal>
tar -zxf graalvm-ce-darwin-amd64-19.2.1.tar.gz
export GRAAL_HOME=<your/directory/with/downloaded/graal>/graalvm-ce-19.1.1/Contents/Home
export PATH=$PATH:${GRAAL_HOME}/bin
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




