# Top 5 Things To Do With Ergo Appkit

[Ergo Appkit](https://github.com/aslesarenko/ergo-appkit) is a library for
polyglot development of Ergo Applications based on
[GraalVM](https://www.graalvm.org/). Many [useful
things](https://medium.com/graalvm/graalvm-ten-things-12d9111f307d) can be done
with GraalVM. Relying on that, in this article we’ll list some of the Appkit
features inherited from GraalVM and explain how you can use them.

- [0. Example Scenario](#example-scenario)
- [1. Develop Ergo Applications in Java]()
- [2. Low-footprint, fast-startup Ergo Applications]()
- [3. Develop Ergo Applications in JavaScript, Python, Ruby, and R]()
- [4. Ergo Application as native library]()
- [5. Debug your polyglot Ergo Application]()

## Example Scenario

We will create a simple Java console application (called
[ErgoTool](https://github.com/aslesarenko/ergo-appkit/blob/master/examples/src/main/java/org/ergoplatform/example/ErgoToolJava.java))
which uses Appkit library to send a new transaction to an Ergo node. The
transaction transfers a given amount of Erg to a new box protected by the
following Ergo contract written in ErgoScript (see this
[introduction](https://ergoplatform.org/docs/ErgoScript.pdf) and more [advanced
examples](https://ergoplatform.org/docs/AdvancedErgoScriptTutorial.pdf)).
```
{ 
  // Parameters
  // freezeDeadline: Int - some future block number after which the box can be spent
  // walletOwnerPk: SigmaProp - public key of the new box owner
  sigmaProp(HEIGHT > freezeDeadline) && walletOwnerPk
}
```
This contract checks the following conditions to allow spending of the box: 
1) current block number in Ergo blockchain (aka blockchain HEIGHT) should be
greater than the given deadline
2) the spending transaction should be signed by the owner of the Ergo node

The first condition effectively forbids spending of the box before the Ergo
blockchain grows to the given height (thus delaying possibility to spend it).
Because each new block is mined every 2 minutes on average, using height it is
easy to compute 1 day, 1 week, 1 month or any other delays. The funds become
frozen in the box for some time. If you want to protect spending of your own
Ergs even from yourself (for whatever reason :) you can use this Ergo contract
and the ErgoTool we will develop in this post.

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

As the first step ErgoTool reads the configuration and the amount of NanoErg to
transfer into the new box from the file and command line arguments
```java
public static void main(String[] args) {
    long amountToPay = Long.parseLong(args[0]);
    ErgoToolConfig conf = ErgoToolConfig.load("ergotool.json");
    ErgoNodeConfig nodeConf = conf.getNode();
    int newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));
    // the rest of the code discussed below 
    ...
}
```

Next we connect to the running testnet node from our Java application by creating
`ErgoClient` instance.
```java
ErgoClient ergoClient = RestApiErgoClient.create(nodeConf);
```

Using
[ErgoClient](https://github.com/aslesarenko/ergo-appkit/blob/master/lib-api/src/main/java/org/ergoplatform/appkit/ErgoClient.java)
any block of code can be executed in the current blockchain context.

```java
String txJson = ergoClient.execute((BlockchainContext ctx) -> {
    // use ctx here to create and sign a new transaction
    // then sent it to the node 
});
```

The lambda passed to `execute` is called when the current blockchain context 
is loaded from the node. This is where we shall put our application logic.
We start with some auxiliary steps.
```java
// access the wallet embedded in the Ergo node
ErgoWallet wallet = ctx.getWallet();

// calculate total amount of NanoErgs we need to send to the new box 
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
If no such constants are used, then `ConstantsBuilder.empty()` can be passed.

In this specific case we pass public key of the `prover` for `pkOwner` 
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

And finally we use prover to sign the transaction, obtain a new
`SignedTransaction` instance and use context to send it to the Ergo node.
The resulting `txId` can be used to refer to this transaction later and
is not really used here.

```java
SignedTransaction signed = prover.sign(tx);
String txId = ctx.sendTransaction(signed);
return signed.toJson(true);
```
As the last step we serialize signed transaction into Json with turned on pretty
printing. Please see the [full source
code](examples/src/main/java/org/ergoplatform/example/ErgoToolJava.java) of the
example.


## 2. Low-footprint, fast-startup Ergo Applications

Using Java for short-running processes can suffer from longer startup time and
relatively high memory usage.
