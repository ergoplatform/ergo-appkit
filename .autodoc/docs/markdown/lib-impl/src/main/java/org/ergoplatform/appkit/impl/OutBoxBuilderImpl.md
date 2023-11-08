[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-impl/src/main/java/org/ergoplatform/appkit/impl/OutBoxBuilderImpl.scala)

The `OutBoxBuilderImpl` class is a part of the `ergo-appkit` project and is used to build an `OutBox` object. An `OutBox` is a data structure that represents an output box of a transaction in the Ergo blockchain. The `OutBoxBuilderImpl` class provides methods to set the value, contract, tokens, registers, and creation height of an `OutBox`. 

The `OutBoxBuilderImpl` class implements the `OutBoxBuilder` trait, which defines the methods that can be used to set the properties of an `OutBox`. The `OutBoxBuilderImpl` class has a private variable `_txB` of type `UnsignedTransactionBuilderImpl`, which is used to get the context of the blockchain. The context is obtained by casting the context of the transaction builder to `BlockchainContextImpl`. 

The `OutBoxBuilderImpl` class has private variables `_value`, `_contract`, `_tokens`, `_registers`, and `_creationHeightOpt`. The `_value` variable is used to store the value of the output box. The `_contract` variable is used to store the contract of the output box. The `_tokens` variable is used to store the tokens of the output box. The `_registers` variable is used to store the registers of the output box. The `_creationHeightOpt` variable is used to store the creation height of the output box.

The `OutBoxBuilderImpl` class provides methods to set the value, contract, tokens, registers, and creation height of an `OutBox`. The `value` method is used to set the value of the output box. The `contract` method is used to set the contract of the output box. The `tokens` method is used to set the tokens of the output box. The `mintToken` method is used to mint a new token and add it to the output box. The `registers` method is used to set the registers of the output box. The `creationHeight` method is used to set the creation height of the output box.

The `build` method is used to build an `OutBox` object. The `build` method checks if the contract is defined and creates an `ErgoBoxCandidate` object using the value, contract, tokens, registers, and creation height of the output box. The `ErgoBoxCandidate` object is then used to create an `OutBoxImpl` object, which is returned by the `build` method.

Example usage:

```
val outBoxBuilder = new OutBoxBuilderImpl(unsignedTransactionBuilder)
val outBox = outBoxBuilder
  .value(1000000000)
  .contract(contract)
  .tokens(token1, token2)
  .registers(register1, register2)
  .creationHeight(1000)
  .build()
```
## Questions: 
 1. What is the purpose of the `OutBoxBuilderImpl` class?
- The `OutBoxBuilderImpl` class is used to build an `OutBox` object, which represents an output box in the Ergo blockchain.

2. What is the difference between the `tokens` and `mintToken` methods?
- The `tokens` method is used to add one or more `ErgoToken` objects to the output box being built, while the `mintToken` method is used to add an `Eip4Token` object to the output box and also adds some additional registers to the output box.

3. What happens if the `contract` parameter is not defined when calling the `build` method?
- If the `contract` parameter is not defined when calling the `build` method, a `checkState` exception will be thrown with the message "Contract is not defined".