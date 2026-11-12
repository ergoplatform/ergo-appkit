[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/common/src/main/java/org/ergoplatform/appkit/impl)

The `.autodoc/docs/json/common/src/main/java/org/ergoplatform/appkit/impl` folder contains two Java classes, `ErgoScriptContract` and `ErgoTreeContract`, which are part of the `ergo-appkit` project. Both classes implement the `ErgoContract` interface and represent smart contracts on the Ergo blockchain, providing methods to interact with them.

`ErgoScriptContract` allows developers to create and interact with smart contracts by specifying constant values and ErgoScript code. It provides methods to compile the code, get the contract address, and replace constant values. For example:

```java
Constants constants = ... // define constants
String code = ... // define ErgoScript code
NetworkType networkType = NetworkType.MAINNET; // set the network type
ErgoScriptContract contract = new ErgoScriptContract(constants, code, networkType); // create a contract object
Address address = contract.toAddress(); // get the address of the contract
```

`ErgoTreeContract` represents a smart contract on the Ergo blockchain using the serialized script (ErgoTree) of the contract. It provides a way to get the contract's `Address` object, which can be used to send Ergs or tokens to the contract or call its methods. However, it does not implement methods for getting constants or the ErgoScript code. Example usage:

```java
Values.ErgoTree ergoTree = ... // get the serialized script of the contract
NetworkType networkType = NetworkType.MAINNET; // set the network type
ErgoTreeContract contract = new ErgoTreeContract(ergoTree, networkType); // create a contract object
Address address = contract.toAddress(); // get the address of the contract
```

Both classes provide a convenient way to interact with smart contracts on the Ergo blockchain. Developers can choose the appropriate class based on their requirements, whether they need to work with ErgoScript code and constants (`ErgoScriptContract`) or directly with the serialized script (`ErgoTreeContract`). These classes can be used in conjunction with other parts of the `ergo-appkit` project to build and deploy smart contracts, send transactions, and interact with the Ergo blockchain.
