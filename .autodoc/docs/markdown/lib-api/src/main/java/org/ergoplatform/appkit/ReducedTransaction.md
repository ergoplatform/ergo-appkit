[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-api/src/main/java/org/ergoplatform/appkit/ReducedTransaction.java)

The `ReducedTransaction` interface is a part of the `ergo-appkit` project and is used to represent an unsigned transaction that has been reduced. A reduced transaction is an unsigned transaction that has been augmented with one `ReductionResult` for each `UnsignedInput`. The `ReducedTransaction` interface extends the `Transaction` interface, which means that it inherits all the methods of the `Transaction` interface.

The `ReducedTransaction` interface has three methods: `getTx()`, `getCost()`, and `toBytes()`. The `getTx()` method returns the underlying reduced transaction data. The `getCost()` method returns the cost accumulated while reducing the original unsigned transaction. The `toBytes()` method returns the serialized bytes of this transaction.

The `ReducedTransaction` interface can be used in the larger project to represent an unsigned transaction that has been reduced. For example, the `ReducedTransaction` interface can be used in a smart contract that requires an unsigned transaction to be reduced before it can be executed. The `ReducedTransaction` interface can also be used in a wallet application that allows users to create and sign transactions.

Here is an example of how the `ReducedTransaction` interface can be used:

```
// create an unsigned transaction
UnsignedTransaction unsignedTx = new UnsignedTransaction(inputs, outputs);

// reduce the unsigned transaction
ReducedTransaction reducedTx = unsignedTx.reduce();

// get the underlying reduced transaction data
ReducedErgoLikeTransaction reducedData = reducedTx.getTx();

// get the cost accumulated while reducing the original unsigned transaction
int cost = reducedTx.getCost();

// serialize the reduced transaction
byte[] serializedTx = reducedTx.toBytes();
```

In this example, we create an unsigned transaction using the `UnsignedTransaction` class. We then reduce the unsigned transaction using the `reduce()` method, which returns a `ReducedTransaction` object. We can then use the methods of the `ReducedTransaction` interface to get the underlying reduced transaction data, the cost accumulated while reducing the original unsigned transaction, and the serialized bytes of the reduced transaction.
## Questions: 
 1. What is the purpose of this interface and how is it used in the ergo-appkit project?
   - This interface represents an unsigned transaction that has been reduced and augmented with a `ReductionResult` for each `UnsignedInput`. It can be obtained by reducing an unsigned transaction. It is used to provide access to the reduced transaction data, cost, and serialized bytes.
2. What is a `ReducedErgoLikeTransaction` and how is it related to this interface?
   - `ReducedErgoLikeTransaction` is the underlying reduced transaction data that can be accessed through the `getTx()` method of this interface. It contains the reduced inputs and outputs of the original unsigned transaction.
3. How is the cost of reducing the original unsigned transaction calculated and what does it represent?
   - The cost of reducing the original unsigned transaction can be obtained through the `getCost()` method of this interface. It represents the amount of resources (e.g. time, memory) required to perform the reduction process.