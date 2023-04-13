[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/lib-api/src/main/java/org/ergoplatform/appkit/UnsignedTransactionBuilder.java)

The `UnsignedTransactionBuilder` interface is used to build a new `UnsignedTransaction` which can later be signed by an `ErgoProver`. A new instance of this builder can be obtained from the `BlockchainContext`. Before the unsigned transaction can be sent to the blockchain, it should be signed by a prover. The prover should be constructed by the `ErgoProverBuilder` obtained from the same `BlockchainContext`.

This interface provides several methods to add inputs, data inputs, outputs, transaction fees, tokens to burn, and change outputs to the transaction. The `addInputs` method adds input boxes to an already specified list of inputs or, if no input boxes are defined yet, as the boxes to spend. The `addDataInputs` method adds input boxes to an already specified list of data inputs or, if no data input boxes are defined yet, sets the boxes as the data input boxes to be used. The `addOutputs` method adds output boxes to an already specified list of outputs or, if no output boxes are defined yet, as the boxes to be output. The `fee` method configures the transaction fee amount in NanoErgs. The `tokensToBurn` method configures amounts for tokens to be burnt. The `sendChangeTo` method adds a change output to the specified address if needed.

The `build` method builds a new unsigned transaction in the `BlockchainContext` inherited from this builder. The `getCtx` method returns the context for which this builder is building transactions. The `getPreHeader` method returns the current (either default of configured) pre-header. The `getNetworkType` method returns the network type of the blockchain represented by the context of this builder. The `outBoxBuilder` method creates a new builder of output box. The `getInputBoxes` method returns all input boxes attached to this builder. The `getOutputBoxes` method returns all output boxes attached to this builder.

Overall, this interface provides a convenient way to build unsigned transactions for the Ergo blockchain. It allows developers to specify inputs, data inputs, outputs, transaction fees, tokens to burn, and change outputs for the transaction. Once the transaction is built, it can be signed by a prover and sent to the blockchain.
## Questions: 
 1. What is the purpose of this interface and how does it relate to the Ergo blockchain?
- This interface is used to build a new unsigned transaction that can later be signed by a prover and sent to the Ergo blockchain. It is obtained from the BlockchainContext and allows for the addition of input and output boxes, transaction fees, and token burning.

2. What is the difference between addInputs() and addDataInputs() methods?
- The addInputs() method adds input boxes to the list of boxes to spend, while the addDataInputs() method adds input boxes to the list of data input boxes to be used. Both methods take an array of InputBox objects as a parameter.

3. What is the purpose of the outBoxBuilder() method?
- The outBoxBuilder() method creates a new builder of output boxes, which can be used to build a single instance of OutBox. A new OutBoxBuilder should be created for each new OutBox.