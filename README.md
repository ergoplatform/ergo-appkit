# Appkit: A Library for Polyglot Development of Ergo Applications 

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

Appkit library is based on [GraalVM](https://www.graalvm.org) - a novel next
generation approach to implement software which is reusable across several
programming languages and execution environments (see [motivation for using
Graal](#why-graal) below).

Appkit is written in Java and is a thin wrapper around core
components provided by [ErgoScript
interpreter](https://github.com/ScorexFoundation/sigmastate-interpreter) and
[Ergo protocol](https://github.com/ergoplatform/ergo) implementations which are
written in Scala.

Using Appkit Ergo applications can be written in one of the languages
supported by GraalVM (i.e. Java, JavaScript, C/C++, Python, Ruby, R) and using
this library applications can communicate with Ergo nodes via unified API and
programming model provided by Appkit. Jump to [setup instructions](#setup) if
you want get started right away.

## Usage Example 

Among other things, Appkit library allows to communicate with Ergo nodes via
REST API. Let's see how we can write a simple Java console application (let's
call it ErgoTool) which uses Appkit library. ErgoTool will create
and send a new transaction to an Ergo node which is started locally and
available at `http://localhost:9052/`. Suppose we [set up a full
node](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node) and started
it using the following command.
```
java -jar -Xmx4G target/scala-2.12/ergo-3.1.3.jar --testnet -c ergo-testnet.conf
```

```
    val toolConf = ErgoToolConfig.load("ergotool.json")
```

In order to connect to the running testnet node from our Java application first create
`ErgoClient` instance.
```
String nodeUrl = "http://localhost:9052/";
String apiKey  = "82444a18c223dc42b78f52c58facfd909c8cc38858a5f22e89070959499076e1"; 
ErgoClient ergoClient = RestApiErgoClient.create(nodeUrl, NetworkType.TESTNET, cmd.apiKey)
```

Here `apiKey` is the secrete key required for API authentication which can be
obtained as described
[here](https://github.com/ergoplatform/ergo/wiki/Ergo-REST-API#setting-an-api-key).
Then we can `execute` any block of code in the current blockchain context.
```
SignedTransaction res = ergoClient.execute((BlockchainContext ctx) -> {
    // here we can use ctx to create and sign a new transaction
});
```


## Setup

Appkit require GraalVM (Community or Enterprise edition) to be
[downloaded](https://www.graalvm.org/downloads/) and installed. Community
edition should be enough for Ergo Appkit library.

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

## Why Graal?


