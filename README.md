## Appkit: SDK for writing applications interacting with Ergo blockchain

![Maven Central](http://maven-badges.herokuapp.com/maven-central/org.ergoplatform%20/ergo-appkit_2.12/badge.svg)
[![CI](https://github.com/ergoplatform/ergo-appkit/actions/workflows/ci.yml/badge.svg)](https://github.com/ergoplatform/ergo-appkit/actions/workflows/ci.yml)
[![Publish a release](https://github.com/ergoplatform/ergo-appkit/actions/workflows/release.yml/badge.svg)](https://github.com/ergoplatform/ergo-appkit/actions/workflows/release.yml)

### Contents
- [Introduction](#introduction)
- [Setup](#setup)
- [How to use](#how-to-use)
- [Repository organisation](#repository-organization)
- [Projects that use Appkit](#projects-that-use-appkit)

### Introduction
[Ergo](https://ergoplatform.org/en/) is a resilient blockchain platform for
contractual money. In addition to [Bitcoin](https://bitcoin.org/en/)-like
blockchain architecture Ergo provides advanced contractual capabilities based on
eUTXO model, which are not possible in Bitcoin.

Because of these capabilities numerous decentralized applications become
possible.

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
written in Scala. This gives easy access to core Ergo functionality such as generalized
signatures based on [sigma
protocols](https://hackernoon.com/sigma-protocols-for-the-working-programmer), data
serializers, address encodings etc.

Appkit is [published](https://mvnrepository.com/artifact/org.ergoplatform/ergo-appkit) on
maven repository and cross compiled to both Java 7 and Java 8+ jars.

### Supported platforms (iOS, Android, Desktop)

Appkit has been successfully used on Android, iOS and Desktop platforms in following
scenarios:

1. Appkit is compatible with [GraalVM](https://www.graalvm.org) - a
novel next generation approach to implement software which is reusable across
several programming languages and execution environments. For example if Node.js
application is run on GraalVM, then it can use Appkit to interact with Ergo
Blockchain (see [motivation for using Graal](#why-graal) below). 

2. Using Appkit,  Ergo applications can be written in one of the languages supported
by GraalVM (i.e. Java, JavaScript, C/C++, Python, Ruby, R) and using this
library applications can communicate with Ergo nodes via unified API and
programming model provided by Appkit.

3. Appkit based Ergo applications
can be compiled into native code using [native-image ahead of time
compiler](https://www.graalvm.org/latest/reference-manual/native-image/) and then
executed without Java VM with very fast startup time and lower runtime memory
overhead compared to a Java VM. For example this allows to create very
responsive command line utility applications such as
[ergo-tool](https://github.com/ergoplatform/ergo-tool).

4. Appkit is used in [Ergo Wallet App](https://github.com/ergoplatform/ergo-wallet-app)
   and hence can be used to develop Andoid, iOS and Desktop applications.

### Setup

#### Maven

Appkit is **Java/Scala** library which is cross compiled by both Scala 2.11 and Scala 2.12
compilers. As a result it can run on JVMv7/[Android](https://github.com/aslesarenko/ergo-android) 
(when it is built with Scala 2.11) and JVMv8 and above (when it is built with Scala 2.12).

Appkit is released on [Maven Central](https://mvnrepository.com/artifact/org.ergoplatform/ergo-appkit) 
and you can add it to your project with SBT, Maven or Gradle:

Maven: 

    <dependency>
        <groupId>org.ergoplatform</groupId>
        <artifactId>ergo-appkit_2.12</artifactId>
        <version>4.0.9</version>
    </dependency>

Gradle:

     implementation 'org.ergoplatform:ergo-appkit_2.12:4.0.9'

(other examples on the Maven Central page).

As an alternative, you can download the jar file from the 
[releases page](https://github.com/ergoplatform/ergo-appkit/releases) and add it to your project manually.

#### Building locally

See [Build instructions](BUILD.md).

#### Using from non-JVM languages

##### GraalVM
In addition to Java, Appkit can be used to write Ergo applications in **Scala, JavaScript,
Python and Ruby** and run those applications under GraalVM, which support cross
language interoperability.
Please see [examples](https://github.com/aslesarenko/ergo-appkit-examples), 
[Build instructions Java and GraalVM](BUILD.md) and [GraalVM instructions](graalvm.md).

##### Python 
From **Python**, you can also use Appkit running in JVM context. 
[Guide how to use Appkit from Python by bridging with JPype](https://github.com/ergoplatform/ergo-appkit/wiki/Using-Appkit-from-Python), 
or simply by using

     pip install ergpy

See [ergpy project page](https://github.com/mgpai22/ergpy) for more information.


### How to use

#### Using from JVM 

* [Tutorial starting with Appkit](https://github.com/ergoplatform/ergo-appkit/wiki/Tutorial-starting-with-Appkit-on-Gradle-projects)
* [Interacting with a local node](https://github.com/ergoplatform/ergo-appkit/wiki/Interacting-with-a-local-node)
* [Building ErgoPay transactions in Spring](https://medium.com/@bschulte19e/implement-a-dapp-using-ergopay-d95e17a51410)

#### Python

* [Building transaction, minting a token](https://github.com/ergoplatform/ergo-appkit/wiki/Using-Appkit-from-Python)

### Repository organization

| sub-module  | description |
|---|-----|
| common  |  Used in all other submodules and contain basic utility classes |
| java-client-generated  | Typed REST Java client [generated](https://swagger.io/tools/swagger-codegen/) from [Swagger definition](https://swagger.io/specification/) of [Ergo node API](https://github.com/ergoplatform/ergo/blob/master/src/main/resources/api/openapi.yaml) |
| lib-api   | All Appkit Java interfaces which can be implemented elsewhere |
| lib-impl  | Implementation of Appkit interfaces using `java-client-generated` to connect to Ergo API  |
| examples | collection of simple Appkit example applications   |

### Build locally

[Build instructions Java and GraalVM](BUILD.md)

### Projects that use Appkit

Appkit is a foundational non-opinionated library which can be used to create other
libraries, Apps and tools. Here is the list of projects which use Appkit. 

- [Official Ergo Wallet App](https://github.com/ergoplatform/ergo-wallet-app) - a cross-platform wallet for Ergo supporting Android, iOS (via RoboVM) and Desktop (via Kotlin Compose). Can be used as an example of cross-platform application using Appkit.
- [SigmaFi](https://github.com/K-Singh/Sigma-Finance) - A Decentralized P2P Financial Contracts on the Ergo blockchain.
- [ErgoMixer](https://github.com/ergoMixer/ergoMixBack) - a web application for mixing ergs and tokens based on Ergo platform
- [ergo-playgrounds](https://github.com/ergoplatform/ergo-playgrounds) - Run contracts + off-chain code in the browser
- [ErgoGravity](https://github.com/ErgoGravity) - provides the required tools for Ergo platform to be integrated with Gravity network (`gateway-proxy`, `ergo-susy-proxy`, `ergo-luport-executor`, `startup-script`)
- [ergo-tool](https://github.com/ergoplatform/ergo-tool) - a Command Line Interface application for Ergo
- [ergo-appkit-examples](https://github.com/aslesarenko/ergo-appkit-examples) - Ergo Appkit Examples
- [ergo-android](https://github.com/aslesarenko/ergo-android) - Example Android application 
- [ergo-faucet](https://github.com/zargarzadehm/ergo-faucet) - A tool for gain some test tokens or Erg in mainnet or testnet network - ErgoFaucet is available [here](https://ergofaucet.org) 
- [ergo-jde](https://github.com/ergoplatform/ergo-jde) - JSON dApp Environment (JDE)
- [Kiosk](https://github.com/scalahub/Kiosk) - a library on top of Ergo-Appkit for interacting with the Ergo Blockchain
- [ergo-mixer-demo](https://github.com/anon92048/ergo-mixer-demo) - a non-interactive (and non-custodial) mixing scheme on top of the Ergo Platform blockchain
- [ergo-subpooling](https://github.com/K-Singh/ergo-subpooling) - a smart contract based mining pool for smaller miners and groups of friends
- [GetBlok-Plasma](https://github.com/GetBlok-io/GetBlok-Plasma) - An Ergo-Appkit based library providing an abstraction layer to easily interact with AVL Trees as an L2 Solution
- [ergo-python-appkit](https://github.com/ergo-pad/ergo-python-appkit)
- [scala-play-next-ergo](https://github.com/kii-dot/scala-play-next-ergo) - Scala Play with Ergo Appkit and NextJs
- Add your project here by creating a PR
