[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-impl/src/main/java/org/ergoplatform/appkit/impl/BlockchainContextBase.java)

The `BlockchainContextBase` class is an abstract class that provides a base implementation for the `BlockchainContext` interface. It contains methods for creating and compiling Ergo contracts, getting the network type, and parsing reduced and signed transactions. 

The `newContract` method takes an `ErgoTree` and returns an `ErgoContract` object. This method is used to create a new contract from an `ErgoTree`. The `compileContract` method takes `Constants` and an `ergoScript` string and returns an `ErgoContract` object. This method is used to compile an `ergoScript` into an `ErgoContract`.

The `getNetworkType` method returns the network type of the blockchain context. This method is used to get the network type of the blockchain context.

The `parseReducedTransaction` method takes a byte array of a reduced transaction and returns a `ReducedTransaction` object. This method is used to parse a reduced transaction.

The `parseSignedTransaction` method takes a byte array of a signed transaction and returns a `SignedTransaction` object. This method is used to parse a signed transaction.

Overall, the `BlockchainContextBase` class provides a base implementation for the `BlockchainContext` interface. It is used to create and compile Ergo contracts, get the network type, and parse reduced and signed transactions. This class can be extended to provide additional functionality for a specific blockchain context. 

Example usage:

```
BlockchainContext context = new BlockchainContextBase(NetworkType.MAINNET);
ErgoContract contract = context.compileContract(ConstantsBuilder.create().build(), "sigmaProp(true)");
NetworkType networkType = context.getNetworkType();
byte[] txBytes = ... // get transaction bytes
ReducedTransaction reducedTx = context.parseReducedTransaction(txBytes);
SignedTransaction signedTx = context.parseSignedTransaction(txBytes);
```
## Questions: 
 1. What is the purpose of the `BlockchainContextBase` class?
- The `BlockchainContextBase` class is an abstract class that implements the `BlockchainContext` interface and provides some common functionality for blockchain contexts.

2. What is the difference between `ReducedTransaction` and `SignedTransaction`?
- `ReducedTransaction` is a reduced version of an `ErgoLikeTransaction` that contains only the essential information, while `SignedTransaction` is a fully signed `ErgoLikeTransaction` that can be broadcasted to the network.

3. What is the purpose of the `parseReducedTransaction` method?
- The `parseReducedTransaction` method takes a byte array that represents a serialized reduced transaction and returns a `ReducedTransaction` object that can be used to interact with the transaction.