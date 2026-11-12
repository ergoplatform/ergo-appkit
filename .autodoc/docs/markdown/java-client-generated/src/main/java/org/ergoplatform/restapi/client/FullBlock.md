[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/java-client-generated/src/main/java/org/ergoplatform/restapi/client/FullBlock.java)

The `FullBlock` class is part of the Ergo Node API and represents a block in the Ergo blockchain. It contains the header, transactions, and additional data (AD proofs and extension). The purpose of this class is to provide a standardized way to represent a block in the Ergo blockchain and to allow for easy serialization and deserialization of block data.

The `FullBlock` class has several methods that allow for setting and getting the different components of a block. The `header` method sets or gets the block header, which contains metadata about the block such as the block height, timestamp, and difficulty. The `blockTransactions` method sets or gets the block transactions, which are the transactions included in the block. The `adProofs` method sets or gets the additional data (AD) proofs, which are used to verify the correctness of the block. The `extension` method sets or gets the extension data, which can be used to store additional information about the block.

The `size` method gets the size of the block in bytes. This can be useful for optimizing block storage and transmission.

Overall, the `FullBlock` class is an important part of the Ergo Node API and is used extensively throughout the Ergo ecosystem. It provides a standardized way to represent a block and allows for easy serialization and deserialization of block data. Below is an example of how the `FullBlock` class can be used to create a new block:

```
BlockHeader header = new BlockHeader();
BlockTransactions transactions = new BlockTransactions();
BlockADProofs adProofs = new BlockADProofs();
Extension extension = new Extension();
FullBlock block = new FullBlock()
    .header(header)
    .blockTransactions(transactions)
    .adProofs(adProofs)
    .extension(extension)
    .size(1024);
```
## Questions: 
 1. What is the purpose of this code?
- This code defines a Java class called `FullBlock` which represents a block with header and transactions in the Ergo Node API.

2. What are the properties of the `FullBlock` class?
- The `FullBlock` class has properties such as `header`, `blockTransactions`, `adProofs`, `extension`, and `size`, which are all related to the block data.

3. What is the purpose of the `toIndentedString` method?
- The `toIndentedString` method is a private helper method that converts an object to a string with each line indented by 4 spaces, except for the first line. It is used in the `toString` method to format the output of the `FullBlock` class.