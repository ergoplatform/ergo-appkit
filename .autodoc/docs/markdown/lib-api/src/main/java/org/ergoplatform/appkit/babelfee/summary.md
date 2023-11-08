[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/lib-api/src/main/java/org/ergoplatform/appkit/babelfee)

The `babelfee` folder contains classes that enable the creation and management of Babel Fee Boxes on the Ergo blockchain. Babel Fee Boxes are used to pay for transaction fees using tokens instead of the platform's primary token (ERG).

The `BabelFeeBoxContract` class represents a smart contract used to create a Babel Fee Box. It has two constructors that take either an `ErgoId` or an `ErgoTree` object as input. The `getErgoTree` and `getTokenId` methods return the contract's logic and the token ID used to pay for transaction fees, respectively.

```java
ErgoId tokenId = new ErgoId("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
BabelFeeBoxContract contract = new BabelFeeBoxContract(tokenId);
Values.ErgoTree ergoTree = contract.getErgoTree();
long value = 1000000000L;
ErgoBox box = new ErgoBox(value, ergoTree);
```

The `BabelFeeBoxState` class represents the state of a Babel Fee Box and provides methods to interact with it, such as calculating the amount of tokens to sell to receive a certain amount of nanoErgs, building a new Babel Fee Box state after a token swap, and building an `OutBox` representing the Babel Fee Box.

The `BabelFeeBoxStateBuilder` class allows for the creation of a `BabelFeeBoxState` object with custom properties. It provides methods like `withPricePerToken`, `withTokenId`, `withBoxCreator`, `withValue`, and `withTokenAmount` to set specific properties of the `BabelFeeBoxState` object.

```java
BabelFeeBoxStateBuilder builder = new BabelFeeBoxStateBuilder();
BabelFeeBoxState boxState = builder
    .withPricePerToken(1000000)
    .withTokenId(tokenId)
    .withBoxCreator(boxCreator)
    .withValue(1000000000)
    .withTokenAmount(10)
    .build();
```

The `BabelFeeOperations` class provides methods for creating, canceling, and finding Babel fee boxes on the Ergo blockchain. The `createNewBabelContractTx` method creates a new Babel fee box for a given token ID and price per token. The `cancelBabelFeeContract` method cancels a Babel fee contract. The `findBabelFeeBox` method fetches a Babel fee box for the given token ID from the blockchain data source. The `addBabelFeeBoxes` method adds Babel fee boxes (input and output) to the given transaction builder.

These classes can be used in the larger project to manage transaction fees on the Ergo blockchain using Babel Fee Boxes. They provide a convenient way to create, cancel, and find Babel fee boxes, as well as interact with their states and properties.
