[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-api/src/main/java/org/ergoplatform/appkit/BlockchainContext.java)

The `BlockchainContext` interface is a representation of a specific context of the blockchain for executing transaction building scenarios. It contains methods for accessing blockchain data, current blockchain state, node information, etc. An instance of this interface can also be used to create new builders for creating new transactions and provers (used for transaction signing).

The `createPreHeader()` method creates a new PreHeader based on this blockchain context. The header of the last block is used to derive default values for the new PreHeader.

The `signedTxFromJson(String json)` method parses the given JSON string and creates a `SignedTransaction` instance. It should be inverse to `SignedTransaction#toJson(boolean)`.

The `newTxBuilder()` method creates a new builder of an unsigned transaction. A new builder is created for every call.

The `getDataSource()` method returns the blockchain data source this blockchain context was created from.

The `getBoxesById(String... boxIds)` method retrieves UTXO boxes available in this blockchain context.

The `newProverBuilder()` method creates a new builder of `ErgoProver`.

The `getNetworkType()` method returns a network type of this context.

The `getHeight()` method returns the height of the blockchain at the point of time when this context was created.

The `sendTransaction(SignedTransaction tx)` method sends a signed transaction to a blockchain node.

The `newContract(Values.ErgoTree ergoTree)` method creates a new `ErgoContract`.

The `compileContract(Constants constants, String ergoScript)` method compiles a contract.

The `getUnspentBoxesFor(Address address, int offset, int limit)` method gets unspent boxes owned by the given address starting from the given offset up to the given limit.

The `getCoveringBoxesFor(Address address, long amountToSpend, List<ErgoToken> tokensToSpend)` method gets unspent boxes owned by the given address starting from the given offset up to the given limit. It is deprecated and should be replaced with `BoxOperations#getCoveringBoxesFor(long, List, Function)`.

The `parseReducedTransaction(byte[] txBytes)` method deserializes the transaction from the serialized bytes of a `ReducedErgoLikeTransaction`.

The `parseSignedTransaction(byte[] txBytes)` method deserializes the transaction from the serialized bytes of an `ErgoLikeTransaction`.

Overall, the `BlockchainContext` interface provides a set of methods for interacting with the blockchain and creating new transactions and provers. It is a key component of the `ergo-appkit` project and is used extensively throughout the project.
## Questions: 
 1. What is the purpose of the `BlockchainContext` interface?
- The `BlockchainContext` interface represents a specific context of blockchain for execution of transaction building scenario. It contains methods for accessing blockchain data, current blockchain state, node information, etc. An instance of this interface can also be used to create new builders for creating new transactions and provers (used for transaction signing).

2. What is the purpose of the `createPreHeader()` method?
- The `createPreHeader()` method creates a new PreHeader based on this blockchain context. The header of the last block is used to derive default values for the new PreHeader.

3. What is the purpose of the `sendTransaction(SignedTransaction tx)` method?
- The `sendTransaction(SignedTransaction tx)` method sends a signed transaction to a blockchain node. On the blockchain node, the transaction is first placed in a pool and then later can be selected by a miner and included in the next block. The new transactions are also replicated all over the network.