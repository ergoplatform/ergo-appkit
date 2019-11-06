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
call it
[ErgoTool](examples/src/main/java/org/ergoplatform/example/ErgoToolJava.java))
which uses Appkit library. ErgoTool will create and send a new transaction to an
Ergo node which is started locally and available at `http://localhost:9052/`.
Suppose we [set up a full
node](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node) and started
it using the following command.
```shell
$ java -jar -Xmx4G target/scala-2.12/ergo-3.1.3.jar --testnet -c ergo-testnet.conf
```

We will need some configuration parameters which we can load from
[ergotool.json](ergotool.json) file which looks like this
```json
{
  "node": {
    "nodeApi": {
      "apiUrl": "http://localhost:9051/",
      "apiKey": "82344a18c24adc42b78f52c58facfdf19c8cc38858a5f22e68070959499076e1"
    },
    "wallet": {
      "mnemonic": "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet",
      "password": "",
      "mnemonicPassword": ""
    },
    "networkType": "TESTNET"
  }
}
```
Here `apiKey` is the secrete key required for API authentication which can be
obtained as described
[here](https://github.com/ergoplatform/ergo/wiki/Ergo-REST-API#setting-an-api-key).
And mnemonic is the secrete phrase obtained during [setup of a new
wallet](https://github.com/ergoplatform/ergo/wiki/Wallet-documentation).

Our example app also reads the amount of NanoErg to put into a new box from command line arguments
```java
import java.util.*;
import org.ergoplatform.ergotool.*;
import org.ergoplatform.polyglot.*;

long amountToPay = Long.parseLong(args[0]);
ErgoToolConfig conf = ErgoToolConfig.load("ergotool.json");
ErgoNodeConfig nodeConf = conf.getNode();
```

Next we connect to the running testnet node from our Java application by creating
`ErgoClient` instance.
```java
ErgoClient ergoClient = RestApiErgoClient.create(nodeConf);
```

Using `ErgoClient` we can `execute` any block of code in the current blockchain context.

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
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword())
    .build();
```

Now that we have the input boxes to spend in the transaction, we need to create 
an output box with the requested `amountToPay` and the specific contract protecting 
that box.

```java
// the only way to create transaction is using builder obtained from the context
// the builder keeps relationship with the context to access nessary blockchain data.
UnsignedTransactionBuilder txB = ctx.newTxBuilder();

// create new box using new builder obtained from the transaction builder
// in this case we compile new ErgoContract from source ErgoScript code
OutBox newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
                ConstantsBuilder.create()
                        .item("deadline", ctx.getHeight() + newBoxDelay)
                        .item("pkOwner", prover.getP2PKAddress().pubkey())
                        .build(),
                "{ sigmaProp(HEIGHT > deadline) && pkOwner }"))
        .build();
```
Note, in order to compile `ErgoContract` from source code the `compileContract`
method requires to provide values for named constants which are used in the script.
If no such constants are used, then `ConstantsBuilder.empty()` can be used.

In this specific case we pass public key of the `prover` for `pkOwner` 
placeholder of the script meaning the box can be spend only be the owner of the
Ergo node we are working with. 

We next create an unsigned transaction using data collected so far.
```java
// tell transaction builder which boxes we are going to spend, which outputs
// to create, amount of transaction fees and address for change coins.
UnsignedTransaction tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();
```

And finally we use prover to sign the transaction, obtain a new
`SignedTransaction` instance and use context to send it to the Ergo node.
The resulting `txId` can be used to refer to this transaction later and
is not really used here.

```java
SignedTransaction signed = prover.sign(tx);
String txId = ctx.sendTransaction(signed);
return signed.toJson(true);
```
As the last step we serialize signed transaction into Json.
Please see the [full source code]() of the example.

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


