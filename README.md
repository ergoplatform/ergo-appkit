# Appkit: A Library for Polyglot Development of Ergo Applications 

## Contents
- [Introduction](#introduction)
- [Using from Java](#using-from-java)
- [Using from other languages](#using-from-other-languages)
- [Repository organization](#repository-organization)
- [Setup](#setup)
    - [Prerequisites](#prerequisites)
        - [Install GraalVM Community Edition on MacOS](#install-graalvm-community-edition-on-macos)
    - [Building the Appkit jar file](#building-the-appkit-jar-file)
- [Why Polyglot](#how-graalvm-can-be-used)
- [Projects that use Appkit](#projects-that-use-appkit)

## Introduction
[Ergo](https://ergoplatform.org/en/) is a resilient blockchain platform for
contractual money. In addition to [Bitcoin](https://bitcoin.org/en/)-like
blockchain architecture Ergo provides advanced contractual capabilities based on
eUTXO model, which are not possible in Bitcoin.

Because of these capabilities numerous decentralized applications become
possible (see also [ErgoScript](https://ergoplatform.org/docs/ErgoScript.pdf) and
[Advanced ErgoScript
examples](https://ergoplatform.org/docs/AdvancedErgoScriptTutorial.pdf)):
 - [SigmaUSD](https://sigmausd.io/) - the first UTxO-based stable coin.
 - [ErgoMixer](https://github.com/ergoMixer/ergoMixBack) - The first working
 non-custodial, programmable, non-interactive mixer in the cryptocurrency space.
 - [ErgoAuctions](https://ergoauctions.org) - a decentralized auction house,
 secure and easy to use. A simple way to sell or buy Ergo's tokens, artworks,
 NFTs, etc.
- [ErgoDex](https://ergodex.io/) - Decentralized exchange on Ergo and Cardano
- [ErgoGravity](https://github.com/ErgoGravity) - ErgoGravity provides the required tools for Ergo platform to be integrated with Gravity network, a blockchain-agnostic protocol that handles communication of blockchain networks with each other.
- [CrowdFunding](https://ergoplatform.org/en/blog/2019_09_06_crowdfund/) - a way of raising capital through the collective efforts of individuals
- [Interest-Free Loan
Contract](https://www.ergoforum.org/t/interest-free-loan-contract/67) etc.

Ergo Applications can avoid centralization of trust and instead rely on
trust-less truly decentralized protocols which are implemented using Ergo
contracts deployed on Ergo blockchain.

By its very nature, Ergo applications are both decentralized and cross-platform
ranging from web applications running in browsers, mobile applications running
on Android and iOS, standalone applications executing on Linux, MacOS or Windows
to services deployed in cloud servers.

That said, it is very desirable to provide consistent programming model and APIs
to facilitate all those application scenarios and platforms.

Ergo Appkit has idiomatic Java API and is written in Java/Scala. It is a thin
wrapper around core components provided by [ErgoScript
interpreter](https://github.com/ScorexFoundation/sigmastate-interpreter) and
[Ergo protocol](https://github.com/ergoplatform/ergo) implementations which are
written in Scala. It is
[published](https://mvnrepository.com/artifact/org.ergoplatform/ergo-appkit) on
maven repository and cross compiled to both Java 7 and Java 8+ jars.

The Appkit library is compatible with [GraalVM](https://www.graalvm.org) - a
novel next generation approach to implement software which is reusable across
several programming languages and execution environments. For example if Node.js
application is run on GraalVM, then it can use Appkit to interact with Ergo
Blockchain (see [motivation for using Graal](#why-graal) below). 

Using Appkit Ergo applications can be written in one of the languages supported
by GraalVM (i.e. Java, JavaScript, C/C++, Python, Ruby, R) and using this
library applications can communicate with Ergo nodes via unified API and
programming model provided by Appkit. In addition Appkit based Ergo applications
can be compiled into native code using [native-image ahead of time
compiler](https://www.graalvm.org/docs/reference-manual/native-image/) and then
executed without Java VM with very fast startup time and lower runtime memory
overhead compared to a Java VM. For example this allows to create very
responsive command line utility applications such as
[ergo-tool](https://github.com/ergoplatform/ergo-tool).

Please follow the [setup instructions](#setup) to get started.

## Examples

### Using from Java 

Among other things, Appkit library allows to communicate with Ergo nodes via
[REST API](https://github.com/ergoplatform/ergo/blob/master/src/main/resources/api/openapi.yaml). 
Let's see how we can write ErgoTool - a simple Java console application (similar to
[ergo-tool](https://github.com/ergoplatform/ergo-tool) utility)
which uses Appkit library. ErgoTool allows to create and send a new transaction
to an any existing Ergo node on the network which. A new node can also be started
locally and thus available at `http://localhost:9052/`. Suppose we [set up a full
node](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node) and started
it using the following command.
```shell
$ java -jar -Xmx4G target/scala-2.12/ergo-4.0.8.jar --testnet -c ergo-testnet.conf
```

We will need some configuration parameters which can be loaded from
[ergotool.json](ergotool.json) file which looks like this
```json
{
  "node": {
    "nodeApi": {
      "apiUrl": "http://139.59.29.87:9053",
      "apiKey": "82344a18c24adc42b78f52c58facfdf19c8cc38858a5f22e68070959499076e1"
    },
    "wallet": {
      "mnemonic": "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet",
      "password": "",
      "mnemonicPassword": ""
    },
    "networkType": "MAINNET"
  },
  "parameters": {
    "newBoxSpendingDelay": "30"
  }
}
```

Here `apiKey` is the secret key required for API authentication which can be
obtained as described
[here](https://github.com/ergoplatform/ergo/wiki/Ergo-REST-API#setting-an-api-key).
And mnemonic is the secret phrase obtained during [setup of a new
wallet](https://github.com/ergoplatform/ergo/wiki/Wallet-documentation) or if
you don't want to setup your node using ergo-tool's
[mnemonic](https://github.com/ergoplatform/ergo-tool#supported-commands)
command.

Our example app also reads the amount of NanoErg to put into a new box from command line arguments
```java
public static void main(String[] args) {
    long amountToPay = Long.parseLong(args[0]);
    ErgoToolConfig conf = ErgoToolConfig.load("ergotool.json");
    int newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));
    // the rest of the code shown below 
    ...
}
```

Next we connect to the running testnet node from our Java application by creating
`ErgoClient` instance.
```java
ErgoNodeConfig nodeConf = conf.getNode();
ErgoClient ergoClient = RestApiErgoClient.create(nodeConf, null);
```

Using `ErgoClient` we can
[execute](lib-api/src/main/java/org/ergoplatform/appkit/ErgoClient.java)
any block of code in the current blockchain context.

```java
String txJson = ergoClient.execute((BlockchainContext ctx) -> {
    // here we will use ctx to create and sign a new transaction
    // which then be sent to the node and also serialized into Json
});
```

The lambda passed to `execute` is called when the current blockchain context 
is loaded from the node. This is where we shall put our application logic.
We start with some auxiliary steps.
```java
// access wallet embedded in Ergo node
ErgoWallet wallet = ctx.getWallet();

// calculate total amount of NanoErgs we need to create the new box 
// and pay transaction fees
long totalToSpend = amountToPay + Parameters.MinFee;

// request wallet for unspent boxes that cover required amount of NanoErgs
Optional<List<InputBox>> boxes = wallet.getUnspentBoxes(totalToSpend);
if (!boxes.isPresent())
    throw new ErgoClientException(
        "Not enough coins in the wallet to pay " + totalToSpend, null);
    
// create a so called prover, a special object which will be used for signing the transaction
// the prover should be configured with secrets, which are nessesary to generate signatures (aka proofs)
ErgoProver prover = ctx.newProverBuilder()
    .withMnemonic(
            SecretString.create(nodeConf.getWallet().getMnemonic()),
            SecretString.create(nodeConf.getWallet().getMnemonicPassword()))
    .build();
```

Now that we have the input boxes to spend in the transaction, we need to create 
an output box with the requested `amountToPay` and the specific contract protecting 
that box.

```java
// the only way to create transaction is using builder obtained from the context
// the builder keeps relationship with the context to access necessary blockchain data.
UnsignedTransactionBuilder txB = ctx.newTxBuilder();

// create new box using new builder obtained from the transaction builder
// in this case we compile new ErgoContract from source ErgoScript code
OutBox newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
                ConstantsBuilder.create()
                        .item("freezeDeadline", ctx.getHeight() + newBoxDelay)
                        .item("pkOwner", prover.getP2PKAddress().pubkey())
                        .build(),
                "{ sigmaProp(HEIGHT > freezeDeadline) && pkOwner }"))
        .build();
```
Note, in order to compile `ErgoContract` from source code the `compileContract`
method requires to provide values for named constants which are used in the script.
If no such constants are used, then `ConstantsBuilder.empty()` can be passed.

In this specific case we pass public key of the `prover` for `pkOwner` 
placeholder of the script meaning the box can be spend only by the owner of the
public key from the `wallet` section of [ergotool.json](ergotool.json). 

Next we create an unsigned transaction using all the data collected so far.
```java
// tell transaction builder which boxes we are going to spend, which outputs
// to create, amount of transaction fees and address for change coins.
UnsignedTransaction tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress()) // i.e. back to the wallet's pk
        .build();
```

And finally we use prover to sign the transaction, obtain a new
`SignedTransaction` instance and use context to send it to the Ergo node.
The resulting `txId` can be used to refer to this transaction later and
is not really used here.

```java
SignedTransaction signed = prover.sign(tx);
String txId = ctx.sendTransaction(signed);
return signed.toJson(/*prettyPrint=*/true, /*formatJson=*/true);
```
As the last step we serialize signed transaction into Json with turned on pretty
printing. Please see the [full source
code](https://github.com/aslesarenko/ergo-appkit-examples/blob/master/java-examples/src/main/java/org/ergoplatform/appkit/examples/FreezeCoin.java) of the
example.

## Using from other languages
In additiona to Java, Appkit can be used to write Ergo applications in Scala, JavaScript,
Python and Ruby and run those applications under GraalVM, which support cross
language interoperability.
Please see [examples](https://github.com/aslesarenko/ergo-appkit-examples).

## Repository organization

| sub-module  | description |
|---|-----|
| common  |  Used in all other submodules and contain basic utility classes |
| java-client-generated  | Typed REST Java client [generated](https://swagger.io/tools/swagger-codegen/) from [Swagger definition](https://swagger.io/specification/) of [Ergo node API](https://github.com/ergoplatform/ergo/blob/master/src/main/resources/api/openapi.yaml) |
| lib-api   | All Appkit Java interfaces which can be implemented elsewhere |
| lib-impl  | Implementation of Appkit interfaces using `java-client-generated` to connect to Ergo API  |
| examples | collection of simple Appkit example applications   |

## Setup

### Prerequisites

Appkit is Java/Scala library which is cross compiled by both Scala 2.11 and Scala 2.12
compilers. As a result it can run on JVMv7 (when it is built with Scala 2.11) and JVMv8
and above (when it is built with Scala 2.12). In addition Appkit jar built with Scala 2.11
can be [used as a library in Android
application](https://github.com/aslesarenko/ergo-android).

For using Appkit from non-JVM languages (JS, Python, Ruby) GraalVM is required (Community
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

## How GraalVM can be used?

After many years of research and development GraalVM project has matured enough end
became [production ready](https://medium.com/graalvm/announcing-graalvm-19-4590cf354df8).

From Ergo perspective GraalVM platform opens many unique opportunities in the
nearest and the distant future. Here is a summary of some GraalVM related facts:

- Ergo node can run on GraalVM without any modifications, in fact GraalVM is
fully compliant with JDK 1.8 (which is minimal requirement for [Ergo Protocol
Client](https://github.com/ergoplatform/ergo/releases))

- Graal's
[native-image](https://www.graalvm.org/docs/reference-manual/native-image/) tool
can be used to compile JVM applications into native applications which can run
without JVM. It can also produce shared libraries (`*.so` files on Linux and
`*.dylib` files on MacOS) that can be used from C/C++ applications (and also
from JavaScript, Python, etc using FFI).

- All cryptography classes [BouncyCastle Java
library](https://bouncycastle.org/java.html) which are used in Ergo reference
implementation can be compiled to shared library and used from non-JVM
languages.

- All the code from [ErgoScript
interpreter](https://github.com/ScorexFoundation/sigmastate-interpreter/pulls),
[ergo-wallet](https://github.com/ergoplatform/ergo-wallet) and this Appkit
library is compatible with `native-image` and can be compiled into either native
application or shared library.

- Appkit API interfaces can be used from JavaScript and Python by design. They
can also be [used from C/C++](https://github.com/aslesarenko/ergo-appkit-examples/tree/master/c-examples) .

- Appkit can be pre-compiled to native-image based launchers of both JavaScript
and Python to enjoy fast startup and lower runtime memory
overhead when running scripts 

- Appkit native shared library can be used from Python and JavaScript through FFI

## Projects that use Appkit

Appkit is a foundational non-opinionated libarary which can be used to create other
libraries, Apps and tools. Here is the list of projects which use Appkit. 

- [ergo-tool](https://github.com/ergoplatform/ergo-tool) - a Command Line Interface application for Ergo
- [ergo-appkit-examples](https://github.com/aslesarenko/ergo-appkit-examples) - Ergo Appkit Examples
- [ergo-android](https://github.com/aslesarenko/ergo-android) - Example Android application 
- [ErgoMixer](https://github.com/ergoMixer/ergoMixBack) - a web application for mixing ergs and tokens based on Ergo platform
- [ergo-playgrounds](https://github.com/ergoplatform/ergo-playgrounds) - Run contracts + off-chain code in the browser
- [ErgoGravity](https://github.com/ErgoGravity) - provides the required tools for Ergo platform to be integrated with Gravity network (`gateway-proxy`, `ergo-susy-proxy`, `ergo-luport-executor`, `startup-script`)
- [ergo-faucet](https://github.com/zargarzadehm/ergo-faucet) - A tool for gain some test tokens or Erg in mainnet or testnet network - ErgoFaucet is available [here](https://ergofaucet.org) 
- [ergo-jde](https://github.com/ergoplatform/ergo-jde) - JSON dApp Environment (JDE)
- [Kiosk](https://github.com/scalahub/Kiosk) - a library on top of Ergo-Appkit for interacting with the Ergo Blockchain
- [ergo-mixer-demo](https://github.com/anon92048/ergo-mixer-demo) - a non-interactive (and non-custodial) mixing scheme on top of the Ergo Platform blockchain
- [ergo-subpooling](https://github.com/K-Singh/ergo-subpooling) - a smart contract based mining pool for smaller miners and groups of friends
- Add your project here by creating a PR
