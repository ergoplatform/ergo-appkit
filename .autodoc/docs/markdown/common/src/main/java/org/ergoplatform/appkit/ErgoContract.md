[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/common/src/main/java/org/ergoplatform/appkit/ErgoContract.java)

The code above defines an interface called `ErgoContract` which represents an ErgoScript contract. An ErgoScript contract is a program that defines the rules for spending a UTXO (unspent transaction output) on the Ergo blockchain. The `ErgoContract` interface provides methods to access the information needed to compile an ErgoScript contract into an `ErgoTree` object, which can be used to create a transaction output on the Ergo blockchain.

The `ErgoContract` interface has five methods. The `getConstants()` method returns a `Constants` object that contains the named constants used in the contract. The `getErgoScript()` method returns the source code of the ErgoScript contract as a string. The `substConstant(String name, Object value)` method creates a new contract by substituting the constant with the given name with a new value. The `getErgoTree()` method returns the `ErgoTree` object that represents the compiled contract. Finally, the `toAddress()` method returns the base58 encoded address that represents the contract.

The `ErgoContract` interface is used in the larger `ergo-appkit` project to represent ErgoScript contracts. Developers can use this interface to create, modify, and compile ErgoScript contracts. For example, a developer could create a new contract by implementing the `ErgoContract` interface and providing the necessary information, such as the source code and named constants. The developer could then use the `getErgoTree()` method to compile the contract into an `ErgoTree` object, which can be used to create a transaction output on the Ergo blockchain.

Here is an example of how the `ErgoContract` interface could be used to create a new contract:

```
// Define named constants
Constants constants = new ConstantsBuilder()
    .item("maxAge", 100)
    .item("minAge", 50)
    .build();

// Define source code
String ergoScript = "HEIGHT < maxAge && HEIGHT > minAge";

// Create new contract
ErgoContract contract = new MyErgoContract(constants, ergoScript);

// Compile contract into ErgoTree
Values.ErgoTree ergoTree = contract.getErgoTree();

// Get contract address
Address address = contract.toAddress();
```

In this example, we define two named constants (`maxAge` and `minAge`) and a source code that checks if the current block height is between `maxAge` and `minAge`. We then create a new contract by implementing the `ErgoContract` interface and passing in the constants and source code. We can then compile the contract into an `ErgoTree` object and get the contract address.
## Questions: 
 1. What is the purpose of the `ErgoContract` interface?
- The `ErgoContract` interface represents an ErgoScript contract using source code and named constants, and provides methods to retrieve information about the contract and create new instances with substituted constants.

2. What is the `getErgoTree()` method used for?
- The `getErgoTree()` method returns the underlying `Values.ErgoTree` used by the contract, which can be used to execute the contract on the Ergo blockchain.

3. How does the `toAddress()` method work?
- The `toAddress()` method returns the base58 encoded address that represents the contract, which can be used to send Ergs (the native currency of the Ergo blockchain) to the contract or to execute the contract via a transaction on the blockchain.