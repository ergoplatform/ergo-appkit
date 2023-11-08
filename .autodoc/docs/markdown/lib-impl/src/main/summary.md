[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/lib-impl/src/main)

The `lib-impl/src/main/java/org/ergoplatform/appkit` folder contains the implementation of the Ergo Appkit library, which provides a set of tools and utilities for building Ergo applications. The library is designed to simplify the interaction with the Ergo blockchain and make it easier for developers to create, test, and deploy their applications.

Here is a brief overview of the files in this folder:

1. **Address.java**: This file contains the `Address` class, which represents an Ergo address. It provides methods for creating and validating addresses, as well as converting them to and from different formats (e.g., Base58, ErgoTree).

   Example usage:
   ```java
   Address address = Address.fromBase58("9f4QF8AD1nQ3nJahQVkM6c5RqW5iH6G3tBmaJxU8CHutaHRdPzg");
   ErgoTree ergoTree = address.getErgoTree();
   ```

2. **BlockchainContext.java**: This file contains the `BlockchainContext` interface, which defines methods for interacting with the Ergo blockchain. It allows developers to access blockchain data, such as the current height, box values, and transaction data.

   Example usage:
   ```java
   BlockchainContext context = ...;
   long currentHeight = context.getHeight();
   List<InputBox> boxes = context.getBoxesByIds(boxIds);
   ```

3. **ErgoToken.java**: This file contains the `ErgoToken` class, which represents an Ergo token. It provides methods for creating and manipulating tokens, as well as converting them to and from different formats (e.g., JSON, ErgoBox).

   Example usage:
   ```java
   ErgoToken token = new ErgoToken(tokenId, tokenAmount);
   long amount = token.getValue();
   ```

4. **InputBox.java**: This file contains the `InputBox` class, which represents an input box in an Ergo transaction. It provides methods for accessing box properties, such as the box value, tokens, and ErgoTree.

   Example usage:
   ```java
   InputBox box = ...;
   long boxValue = box.getValue();
   List<ErgoToken> tokens = box.getTokens();
   ```

5. **SignedTransaction.java**: This file contains the `SignedTransaction` class, which represents a signed Ergo transaction. It provides methods for accessing transaction properties, such as the transaction id, inputs, and outputs.

   Example usage:
   ```java
   SignedTransaction tx = ...;
   String txId = tx.getId();
   List<InputBox> inputs = tx.getInputs();
   List<OutputBox> outputs = tx.getOutputs();
   ```

These classes and interfaces work together to provide a high-level API for interacting with the Ergo blockchain. Developers can use them to create, sign, and submit transactions, as well as query the blockchain for relevant data. By using the Ergo Appkit library, developers can focus on building their applications without having to worry about the low-level details of the Ergo protocol.
