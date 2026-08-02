[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/lib-api/src/main)

In the `.autodoc/docs/json/lib-api/src/main` folder, the `java` subfolder contains essential classes for working with Ergo blockchain data and transactions under the `org.ergoplatform` package. These classes provide a foundation for developers to create, inspect, and manipulate Ergo entities, such as addresses, boxes, IDs, and transactions. By using these classes, developers can easily interact with the Ergo blockchain and build applications on top of it.

1. `ErgoAddress.java`: This class is responsible for handling Ergo addresses. It provides methods to create and validate addresses, as well as to convert them to different formats (e.g., Base58, P2PK, P2S). This class is essential for any operation that involves Ergo addresses, such as sending and receiving transactions.

   Example usage:
   ```java
   ErgoAddress address = ErgoAddress.fromBase58("9f4QF8AD1nQDiyECtMvqYwJYwDk3N5xyaAU3zVVXgsg=");
   System.out.println("Address: " + address.toString());
   ```

2. `ErgoBox.java`: This class represents an Ergo box (i.e., a UTXO). It provides methods to access the box's properties, such as its value, tokens, and additional registers. This class is crucial for working with Ergo transactions, as it allows you to manipulate and inspect the boxes involved in a transaction.

   Example usage:
   ```java
   ErgoBox box = ErgoBox.fromJson(jsonString);
   System.out.println("Box value: " + box.getValue());
   ```

3. `ErgoId.java`: This class represents a unique identifier for Ergo entities (e.g., boxes, transactions). It provides methods to create and validate Ergo IDs, as well as to convert them to different formats (e.g., Base16, Base58). This class is useful for any operation that requires unique identification of Ergo entities.

   Example usage:
   ```java
   ErgoId id = ErgoId.fromBase16("6f1a8e7d8e1e4d7a8e1e4d7a8e1e4d7a8e1e4d7a8e1e4d7a");
   System.out.println("ID: " + id.toString());
   ```

4. `ErgoLikeTransaction.java`: This class represents an Ergo transaction. It provides methods to access the transaction's properties, such as its inputs, outputs, and data inputs. This class is essential for working with Ergo transactions, as it allows you to create, inspect, and manipulate transactions.

   Example usage:
   ```java
   ErgoLikeTransaction tx = ErgoLikeTransaction.fromJson(jsonString);
   System.out.println("Transaction inputs: " + tx.getInputs());
   ```

These classes are essential building blocks for working with Ergo blockchain data and transactions. They provide a foundation for developers to create, inspect, and manipulate Ergo entities, such as addresses, boxes, IDs, and transactions. By using these classes, developers can easily interact with the Ergo blockchain and build applications on top of it.
