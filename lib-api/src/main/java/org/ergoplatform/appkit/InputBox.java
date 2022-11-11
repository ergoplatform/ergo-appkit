package org.ergoplatform.appkit;

import special.sigma.Box;

/**
 * Interface of UTXO boxes which can be accessed in the blockchain node.
 * Instances of this interface can be {@link BlockchainContext#getBoxesById(String...) obtained}
 * from {@link BlockchainContext} and {@link UnsignedTransactionBuilder#boxesToSpend(java.util.List) spent}
 * as part of a new transaction.
 */
public interface InputBox extends TransactionBox {
    /**
     * Returns the id of this box.
     */
    ErgoId getId();


    /**
     * Extends the given input with context variables (aka {@link
     * sigmastate.interpreter.ContextExtension}.
     * Note, this method don't change `this` instance.
     *
     * @param variables a list of variables
     * @return a new instance of InputBox with the given variables attached.
     */
    InputBox withContextVars(ContextVar... variables);

    /**
     * Json representation of this transaction.
     *
     * @param prettyPrint if `true` then ergoTree is pretty-printed, otherwise encoded bytes
     *                    are used.
     * @return string with json text
     */
    String toJson(boolean prettyPrint);

    /**
     * Json representation of this transaction.
     * Note, Json formatting is pretty-printed if either prettyPrint or formatJson is set
     * to true.
     *
     * @param prettyPrint if `true` then ergoTree is pretty-printed, otherwise
     *                    encoded bytes are used.
     * @param formatJson  if `true` then json pretty printer is used to format json output.
     *
     * @return string with json text
     */
    String toJson(boolean prettyPrint, boolean formatJson);

    /**
     * Returns the serialized bytes representing this InputBox, including transaction reference data
     */
    byte[] getBytes();

    /**
     * @return this box as an ergo value to store in a register
     */
    ErgoValue<Box> toErgoValue();
}

