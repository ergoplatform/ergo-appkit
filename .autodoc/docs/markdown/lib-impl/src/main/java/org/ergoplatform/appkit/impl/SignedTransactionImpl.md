[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-impl/src/main/java/org/ergoplatform/appkit/impl/SignedTransactionImpl.java)

The `SignedTransactionImpl` class is a part of the `ergo-appkit` project and provides an implementation of the `SignedTransaction` interface. This class is responsible for creating a signed transaction and provides methods to interact with the transaction. 

The `SignedTransactionImpl` class has three instance variables: `_ctx`, `_tx`, and `_txCost`. The `_ctx` variable is of type `BlockchainContextBase` and represents the blockchain context. The `_tx` variable is of type `ErgoLikeTransaction` and represents the signed transaction. The `_txCost` variable is of type `int` and represents the cost of the transaction.

The `SignedTransactionImpl` class provides several methods to interact with the transaction. The `getTx()` method returns the underlying `ErgoLikeTransaction`. The `getId()` method returns the ID of the transaction. The `toJson()` method returns the transaction in JSON format. The `getSignedInputs()` method returns a list of signed inputs. The `getOutputsToSpend()` method returns a list of output boxes that can be spent. The `getInputBoxesIds()` method returns a list of input box IDs. The `getOutputs()` method returns a list of output boxes. The `getCost()` method returns the cost of the transaction. The `toBytes()` method returns the transaction in byte format.

The `toJson()` method is particularly interesting as it takes two boolean parameters: `prettyPrint` and `formatJson`. If `prettyPrint` is true, the output JSON is formatted with indentation and line breaks. If `formatJson` is true, the output JSON is formatted with a consistent style. 

Here is an example of how to use the `SignedTransactionImpl` class:

```java
BlockchainContext ctx = ...; // create a blockchain context
ErgoLikeTransaction tx = ...; // create a signed transaction
int txCost = ...; // set the cost of the transaction
SignedTransaction signedTx = new SignedTransactionImpl(ctx, tx, txCost);

// get the ID of the transaction
String txId = signedTx.getId();

// get the transaction in JSON format
String txJson = signedTx.toJson(true, true);

// get a list of signed inputs
List<SignedInput> signedInputs = signedTx.getSignedInputs();

// get a list of output boxes that can be spent
List<InputBox> outputsToSpend = signedTx.getOutputsToSpend();

// get a list of input box IDs
List<String> inputBoxIds = signedTx.getInputBoxesIds();

// get a list of output boxes
List<OutBox> outputs = signedTx.getOutputs();

// get the cost of the transaction
int txCost = signedTx.getCost();

// get the transaction in byte format
byte[] txBytes = signedTx.toBytes();
```
## Questions: 
 1. What is the purpose of the `SignedTransactionImpl` class?
- The `SignedTransactionImpl` class is an implementation of the `SignedTransaction` interface and provides methods for interacting with a signed Ergo transaction.

2. What is the `toJson` method used for?
- The `toJson` method is used to serialize the transaction to JSON format. It takes two boolean parameters, `prettyPrint` and `formatJson`, which control the formatting of the output.

3. What is the purpose of the `getOutputsToSpend` method?
- The `getOutputsToSpend` method returns a list of `InputBox` objects that represent the outputs of previous transactions that are being spent by the current transaction.