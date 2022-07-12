package org.ergoplatform.appkit;

import org.ergoplatform.ErgoAddress;

import java.util.List;

/**
 * This interface is used to represent unsigned transactions after they are
 * built using {@link UnsignedTransactionBuilder}.
 *
 * @see UnsignedTransactionBuilder
 * @see SignedTransaction
 */
public interface UnsignedTransaction extends Transaction {

    /**
     * Gets unsigned input boxes that will be used in this transaction
     */
    List<InputBox> getInputs();

    /**
     * Gets data inputs that will be used in this transaction
     */
    List<InputBox> getDataInputs();

    /**
     * Returns the change address associated with this unsigned transaction
     */
    ErgoAddress getChangeAddress();

    /**
     * Optional list of tokens requested for burning in this transaction.
     * Returns
     */
    List<ErgoToken> getTokensToBurn();
}
