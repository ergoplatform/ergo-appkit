[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/common/src/main/java/org)

In the `org.ergoplatform` package, you will find essential classes and interfaces that are used throughout the Ergo Appkit project. These classes provide the foundation for working with Ergo blockchain data structures and transactions.

Here is a brief overview of the key classes in this package:

1. **ErgoAddress**: This class represents an Ergo address, which is a unique identifier for a user or a contract on the Ergo blockchain. It provides methods to create and validate Ergo addresses, as well as to convert them to different formats (e.g., Base58, Hex, or ErgoTree).

   Example usage:
   ```java
   ErgoAddress address = ErgoAddress.fromBase58("9f4QF8AD1nQ3nJahQVkM6c8S22pZ5pG5sgz");
   ```

2. **ErgoBox**: This class represents an Ergo box, which is a fundamental data structure in Ergo that holds tokens, registers, and an ErgoTree script. ErgoBox provides methods to create and manipulate Ergo boxes, as well as to serialize and deserialize them.

   Example usage:
   ```java
   ErgoBox box = new ErgoBox(value, ergoTree, tokens, additionalRegisters, creationHeight);
   ```

3. **ErgoId**: This class represents an Ergo box identifier, which is a unique identifier for an Ergo box on the Ergo blockchain. It provides methods to create and validate Ergo box identifiers, as well as to convert them to different formats (e.g., Base58, Hex).

   Example usage:
   ```java
   ErgoId id = ErgoId.fromBase58("21Lt8D5Zk5WNNM5s6Q9zPQJqKf5MpbCb6xcvU9C6VkL8v5JLa1L");
   ```

4. **ErgoTree**: This class represents an ErgoTree script, which is a program that defines the spending conditions for an Ergo box. ErgoTree provides methods to create and manipulate ErgoTree scripts, as well as to serialize and deserialize them.

   Example usage:
   ```java
   ErgoTree tree = ErgoTree.fromBytes(byteArray);
   ```

5. **ErgoValue**: This class represents a value in the Ergo blockchain, such as a token amount or a register value. It provides methods to create and manipulate Ergo values, as well as to serialize and deserialize them.

   Example usage:
   ```java
   ErgoValue<Long> value = ErgoValue.of(1000000000L);
   ```

These classes are essential for working with Ergo blockchain data structures and transactions. They provide the foundation for creating, validating, and manipulating Ergo addresses, boxes, identifiers, scripts, and values. By using these classes, developers can easily interact with the Ergo blockchain and build powerful applications on top of it.
