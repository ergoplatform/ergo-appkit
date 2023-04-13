[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-api/src/main/java/org/ergoplatform/appkit/SignedTransaction.java)

The `SignedTransaction` interface is a representation of a signed transaction that can be sent to the blockchain. It extends the `Transaction` interface and provides additional methods for working with signed transactions.

The `toJson` method returns a JSON representation of the transaction. It takes a boolean parameter `prettyPrint` which determines whether the ErgoTrees will be pretty printed or output as hex strings. There is also an overloaded version of this method that takes an additional boolean parameter `formatJson` which determines whether the JSON output will be pretty printed.

The `getSignedInputs` method returns a list of all signed inputs that will be spent when the transaction is included in the blockchain. Each signed input has an attached signature (proof) that evidences that the prover knows the required secrets.

The `getOutputsToSpend` method returns a list of outputs of the transaction represented as `InputBox` objects ready to be spent in the next chained transaction. This method can be used to create a chain of transactions.

The `getCost` method returns the estimated cost of the transaction. Note that this cost is only an approximation of the actual cost of the transaction, which may depend on the blockchain context.

The `toBytes` method returns the serialized bytes of the transaction.

Overall, the `SignedTransaction` interface provides a way to work with signed transactions in the Ergo platform. It can be used to create, sign, and send transactions to the blockchain. For example, a developer could use this interface to create a new transaction, sign it with a prover, and then send it to the blockchain using the `ErgoClient` class provided by the `ergo-appkit` project.
## Questions: 
 1. What is the purpose of this interface and how does it relate to the Ergo blockchain?
- This interface represents a signed transaction that can be sent to the Ergo blockchain. It contains signed inputs and outputs represented as InputBox objects, and provides methods for getting a JSON representation of the transaction, estimating its cost, and getting its serialized bytes.

2. What is the difference between the two toJson() methods?
- The first toJson() method takes a boolean parameter for pretty-printing the ErgoTrees in the JSON output, while the second method takes two boolean parameters for pretty-printing the ErgoTree and the JSON output, respectively.

3. How can the getOutputsToSpend() method be used to create a chain of transactions?
- The getOutputsToSpend() method returns a list of InputBox objects that can be used as input boxes for a new transaction. By calling this method on a SignedTransaction object and passing the resulting list of InputBox objects to the inputs parameter of a new UnsignedTransaction object, a chain of transactions can be created.