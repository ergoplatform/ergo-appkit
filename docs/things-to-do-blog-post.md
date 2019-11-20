# Top 5 Things To Do With Ergo Appkit

[Ergo Appkit](https://github.com/aslesarenko/ergo-appkit) is a library for
polyglot development of Ergo Applications based on
[GraalVM](https://www.graalvm.org/). Many [useful
things](https://medium.com/graalvm/graalvm-ten-things-12d9111f307d) can be done
with GraalVM. Relying on that, in this article we’ll list some of the Appkit
features inherited from GraalVM and explain how you can use them.

- [0. Example Scenario](#example-scenario)
- [1. Develop Ergo Applications in Java](#1-develop-ergo-applications-in-java)
- [2. Low-footprint, fast-startup Ergo Applications](#2-low-footprint-fast-startup-ergo-applications)
- [3. Develop Ergo Applications in JavaScript, Python, Ruby, and R](#3-develop-ergo-applications-in-javascript-python-ruby)
- [4. Ergo Application as native library](#4-ergo-application-as-native-shared-library)
- [5. Debug your polyglot Ergo Application](#5-debug-your-polyglot-ergo-application)

## Example Scenario

We will create a simple Java console application (called
[ErgoTool](https://github.com/aslesarenko/ergo-appkit/blob/master/examples/src/main/java/org/ergoplatform/example/ErgoToolJava.java))
which uses Appkit library to send a new transaction to an Ergo node. The
transaction transfers a given amount of Erg to a new box protected by the
following Ergo contract written in ErgoScript (see this
[introduction](https://ergoplatform.org/docs/ErgoScript.pdf) and more [advanced
examples](https://ergoplatform.org/docs/AdvancedErgoScriptTutorial.pdf)).
```
// Freezer Contract
{ 
  // Parameters
  // freezeDeadline: Int - some future block number after which the box can be spent
  // userPk: SigmaProp - public key of the new box owner
  sigmaProp(HEIGHT > freezeDeadline) && userPk
}
```
This contract checks the following conditions to allow spending of the box: 
1) current block number in Ergo blockchain (aka blockchain HEIGHT) should be
greater than the given deadline
2) the spending transaction should be signed by the user of ErgoTool using 
the secret key corresponding to the userPk.

The first condition effectively forbids spending of the box before the Ergo
blockchain grows to the given height (thus delaying possibility to spend it).
Because each new block is mined every 2 minutes on average, using the current
blockchain height it is easy to compute 1 day, 1 week, 1 month or any other
delays (i.e. 30 * 24 * 7 blocks per week). The funds become frozen in the box
for some time. If you want to protect spending of your own Ergs even from
yourself (for whatever reason :) you can use this Ergo contract and the ErgoTool
we are going to develop in this post.

## 1. Develop Ergo Applications in Java

Appkit aims to provide a set of Java interfaces which can be used idiomatically
in Java. If you use Java, you will feel right at home using Appkit.

Please follow the [setup
instructions](https://github.com/aslesarenko/ergo-appkit#setup) for GraalVM and
Appkit if you want to reproduce the examples below.

Appkit needs to connect with an Ergo Node via REST API. For example, the node
can be started locally and made available at `http://localhost:9051/` by
following [these
instructions](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node). 
Assume we did the setup and started an Ergo Node using the following command.
```shell
$ java -jar -Xmx4G target/scala-2.12/ergo-3.1.3.jar --testnet -c ergo-testnet.conf
```

Our application will need a connection and security parameters along with
application specific parameters to do its work. We will use a json file with the
configuration parameters which ErgoTool loads at its startup. See for example
[ergotool.json](https://github.com/aslesarenko/ergo-appkit/blob/master/ergotool.json)
file which have the following content
```json
{
  "node": {
    "nodeApi": {
      "apiUrl": "http://localhost:9051/",
      "apiKey": "put here your secret apiKey generated during node setup"
    },
    "wallet": {
      "mnemonic": "the mnemonic key used to initialize or restore the wallet on the node",
      "password": "the password you chose to protect the wallet",
      "mnemonicPassword": "the password you chose to protect the mnemonic"
    },
    "networkType": "TESTNET"
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
wallet](https://github.com/ergoplatform/ergo/wiki/Wallet-documentation).

In ErgoTool as the first step we read the configuration and the amount of
NanoErg to transfer into the new box from the file and command line arguments
```java
public static void main(String[] args) {
    long amountToSend = Long.parseLong(args[0]);  // positive value in NanoErg
    ErgoToolConfig conf = ErgoToolConfig.load("ergotool.json");
    int newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));
    // the rest of the code discussed below 
    ...
}
```

Next we connect to the running Ergo node from our Java application by creating
`ErgoClient` instance.
```java
ErgoNodeConfig nodeConf = conf.getNode();
ErgoClient ergoClient = RestApiErgoClient.create(nodeConf);
```

Using
[ErgoClient](https://github.com/aslesarenko/ergo-appkit/blob/master/lib-api/src/main/java/org/ergoplatform/appkit/ErgoClient.java)
any block of code can be executed in the current blockchain context.

```java
String txJson = ergoClient.execute((BlockchainContext ctx) -> {
    // use ctx here to create and sign a new transaction
    // then send it to the node 
});
```

The lambda passed to `execute` is called when the current blockchain context
with the current blockchain data is loaded from the Ergo node. In this lambda we
shall put our application logic. We start with some auxiliary steps.
```java
// access the wallet embedded in the Ergo node 
// (the wallet's mnemonic we put in ergotool.json)
ErgoWallet wallet = ctx.getWallet();

// calculate total amount of NanoErgs we need to send to the new box 
// and pay transaction fees
long totalToSpend = amountToSend + Parameters.MinFee;

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

Now we have the input boxes to spend in the transaction and we need to create 
an output box with the requested `amountToSend` and the contract protecting 
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
                        .item("freezeDeadline", ctx.getHeight() + newBoxSpendingDelay)
                        .item("userPk", prover.getP2PKAddress().pubkey())
                        .build(),
                "{ sigmaProp(HEIGHT > freezeDeadline) && userPk }"))
        .build();
```
Note, in order to compile `ErgoContract` from source code the `compileContract`
method requires to provide values for named constants which are used in the script.
If no such constants are used, then `ConstantsBuilder.empty()` can be passed.

In this specific case we pass public key of the `prover` for `userPk` 
placeholder of the script meaning the box can be spend only by the owner of the
Ergo node we are working with. 

Next create an unsigned transaction using all the data collected so far.
```java
// tell transaction builder which boxes we are going to spend, which outputs
// to create, amount of transaction fees and address for change coins.
UnsignedTransaction tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();
```

And finally we 1) use prover to sign the created transaction; 2) obtain a new
`SignedTransaction` instance and 3) use context to send the signed transaction to
the Ergo node. The resulting `txId` can be used to refer to this transaction
later and is not really used here.
```java
SignedTransaction signed = prover.sign(tx);
String txId = ctx.sendTransaction(signed);
return signed.toJson(true);
```

As the last step and for demonstration purposes we serialize the signed
transaction into a Json string with turned on pretty printing. Look at the [full
source code](examples/src/main/java/org/ergoplatform/example/ErgoToolJava.java)
of the example for more details and for using it as a template in your
application.

We can run the implemented ErgoTool using the following steps, assuming we are 
at the directory you cloned ergo-tool.
```shell
$ pwd
the/directory/you/cloned/ergo-tool
$ sbt assembly
```
This will assemble the jar file containing ErgoTool application, the step have
to be done only once after any changes in the source code are made.

Having ergo-appkit-3.1.0.jar assembled we can run our console application 
```shell
$ java -cp target/scala-2.12/ergo-appkit-3.1.0.jar \
      org.ergoplatform.example.ErgoToolJava  1000000000 
```
And get the following [output](https://gist.github.com/aslesarenko/cacee372350458ac31bd5c73e957e322) in the console.

That is it, the transaction is accepted by the Ergo node, added to the
unconfirmed transaction pool and broadcasted all over the network. After one of
the miners picks it up, validates and includes in a new block it becomes part of
the blockchain history.

## 2. Low-footprint, fast-startup Ergo Applications

Using Java for short-running processes can suffer from longer startup time and
relatively high memory usage. Let's run ErgoToolJava using the time command to
get the real, wall-clock elapsed time it takes to run the entire program from
start to finish. We use -l to print the memory used as well as time used. 
```shell
$ /usr/bin/time -l java -cp build/libs/appkit-examples-3.1.0-all.jar \
   org.ergoplatform.appkit.examples.ErgoToolJava 1000000000
...
       4.97 real         8.41 user         0.69 sys
 513703936  maximum resident set size
         0  average shared memory size
         0  average unshared data size
         0  average unshared stack size
    125010  page reclaims
      1216  page faults
         0  swaps
         0  block input operations
         0  block output operations
        13  messages sent
        86  messages received
         1  signals received
      2384  voluntary context switches
     17409  involuntary context switches
```
Which means this simple operation took 2 parallel threads to run for almost 4
seconds to do the job. Most of that time can be attributed to JVM startup and
the background JIT compiler running.

GraalVM gives us a tool that solves this problem by compiling Java code
ahead-of-time, to a native executable image, instead of compiling just-in-time
at runtime. This is similar to how a conventional compiler like gcc works. Note,
we may need to run `assembly` first.
```
$ sbt assembly
$ native-image --no-server \
 -cp build/libs/appkit-examples-3.1.0-all.jar\
 --report-unsupported-elements-at-runtime\
  --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
   --allow-incomplete-classpath \
    --enable-url-protocols=http,https org.ergoplatform.appkit.examples.ErgoToolJava ergotool
[ergotool:3133]    classlist:  35,217.78 ms
[ergotool:3133]        (cap):   6,063.07 ms
[ergotool:3133]        setup:   8,268.99 ms
[ergotool:3133]   (typeflow):  60,238.25 ms
[ergotool:3133]    (objects):  33,009.06 ms
[ergotool:3133]   (features):   4,796.86 ms
[ergotool:3133]     analysis: 102,876.01 ms
[ergotool:3133]     (clinit):  11,642.43 ms
[ergotool:3133]     universe:  13,718.96 ms
[ergotool:3133]      (parse):   5,053.18 ms
[ergotool:3133]     (inline):  18,317.24 ms
[ergotool:3133]    (compile):  44,806.82 ms
[ergotool:3133]      compile:  72,288.24 ms
[ergotool:3133]        image:   7,955.29 ms
[ergotool:3133]        write:   2,872.25 ms
[ergotool:3133]      [total]: 243,813.30 ms
```

This command produces a native executable called `ergotool`. This executable
isn’t a launcher for the JVM, it doesn’t link to the JVM, and it doesn’t bundle
the JVM in any way. `native-image` compile out ErgoToolJava code, and any
Java libraries it depends on, all the way down to simple machine code. 

If we look at the libraries which ergotool uses you can see they are only
standard system libraries. We can move just this one file to a system
which don't a JVM installed and it will run there. 
```
$ otool -L ergotool    # ldd ergotool on Linux
ergotool:
	/usr/lib/libSystem.B.dylib (compatibility version 1.0.0, current version 1252.50.4)
	/System/Library/Frameworks/CoreFoundation.framework/Versions/A/CoreFoundation (compatibility version 150.0.0, current version 1455.12.0)
	/usr/lib/libz.1.dylib (compatibility version 1.0.0, current version 1.2.11)
```

If we run the executable we can see that it starts around 8x faster, and uses
around 6x less memory, than running the same program on the
JVM does. You don’t feel that pause you always get when running a
short-running command with the JVM.
```
$ DYLD_LIBRARY_PATH=$GRAAL_HOME/jre/lib /usr/bin/time -l ./ergotool 1800000000
        0.43 real         0.15 user         0.03 sys
  81289216  maximum resident set size
         0  average shared memory size
         0  average unshared data size
         0  average unshared stack size
     20079  page reclaims
         0  page faults
         0  swaps
         0  block input operations
         0  block output operations
        13  messages sent
        86  messages received
         0  signals received
        11  voluntary context switches
       138  involuntary context switches
```

## 3. Develop Ergo Applications in JavaScript, Python, Ruby

GraalVM supports so called polyglot programming in which different components of
an application can be developed using the most suitable language and then
seamlessly combined at runtime. In this way some unique libraries written in say
Java can be used for example in a new node.js application written in JavaScript.

To support polyglot programming GraalVM platform has it's own high performance
implementations of popular languages which we are going to use to implement 
our example ErgoTool scenario.

Before running the examples in JavaScript, Python and Ruby sections below it my
be helpful to run Java example first to make sure everything is set up
correctly.

### JavaScript

GraalVM can [run JavaScript and
Node.js](https://www.graalvm.org/docs/reference-manual/languages/js/)
applications out of the box and it is compatible with the [ECMAScript 2019
specification](http://www.ecma-international.org/ecma-262/10.0/index.html).
Additionally, `js` and `node` launchers accept special `--jvm` and `--polyglot`
command line options which allow JS script to access Java objects and classes.

That said, a JS implementation of ErgoTool can be easily written using Appkit
API interfaces.
Please see the full source code of [ErgoTool JS
implementation](https://github.com/aslesarenko/ergo-appkit-examples/blob/master/js-examples/ErgoTool.js)
for details.
The following command use `node` launcher to execute ErgoTool.js script.
```shell
$ node --jvm --vm.cp=build/libs/appkit-examples-3.1.0-all.jar \
  js-examples/ErgoTool.js  1000000000
```
Note, the paths in the command are relative to the root of
`ergo-appkit-examples` project directory.

### Python

GraalVM can [run Python
scripts](https://www.graalvm.org/docs/reference-manual/languages/python/), though
the Python implementation is still experimental (see also
[compatibility section](https://www.graalvm.org/docs/reference-manual/languages/python/#python-compatibility)
for details).

[Python example of
ErgoTool](https://github.com/aslesarenko/ergo-appkit-examples/blob/master/python-examples/ErgoTool.py)
can be executed using the following command
```shell
$ graalpython --jvm --polyglot --vm.cp=build/libs/appkit-examples-3.1.0-all.jar \
   python-examples/ErgoTool.py 1900000000
```

### Ruby

GraalVM can [run Ruby
scripts](https://www.graalvm.org/docs/reference-manual/languages/ruby/) using
TruffleRuby implementation, which is however still experimental (see also
[compatibility section](https://www.graalvm.org/docs/reference-manual/languages/ruby/#compatibility)
for details).
TruffleRuby aims to be fully compatible with the standard implementation of Ruby, MRI, version 2.6.2

[Ruby example of
ErgoTool](https://github.com/aslesarenko/ergo-appkit-examples/blob/master/ruby-examples/ErgoTool.rb)
can be executed using the following command
```shell
$ ruby --polyglot --jvm --vm.cp=build/libs/appkit-examples-3.1.0-all.jar \
    ruby-examples/ErgoTool.rb 1900000000
```

## 4. Ergo Application as native shared library

Generate shared library 
```
native-image --no-server \
 -cp build/libs/appkit-examples-3.1.0-all.jar\
 --report-unsupported-elements-at-runtime\
  --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
   --allow-incomplete-classpath \
    --enable-url-protocols=http,https 
    --shared -H:Name=libergotool -H:Path=c-examples
```
Check there is no JVM dependencies
```    
otool -L libergotool.dylib
```

Compile C application 
```
clang -Ic-examples -Lc-examples -lergotool c-examples/freezecoin.c -o call_freezecoin
otool -L call_freezecoin
DYLD_LIBRARY_PATH=$GRAAL_HOME/jre/lib ./call_freezecoin 1000000000
```

## 5. Debug your polyglot Ergo Application


## Conclusions

We see how easy it is to use Appkit from different most popular languages such
as Java, JavaScript and Python to implement non-standard application scenarios.

The example assumes the Ergo node (and the embedded wallet) is owned by the
ErgoTool user. However this is not strictly required and the Appkit interfaces
can be used to create and send new transactions using arbitrary public Ergo
node.

