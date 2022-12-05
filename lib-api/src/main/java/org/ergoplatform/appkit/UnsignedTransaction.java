package org.ergoplatform.appkit;

import org.ergoplatform.ErgoAddress;

import javax.annotation.Nonnull;
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
     * The list of tokens requested (in builders) for burning in this transaction when
     * it will be executed on blockchain.
     *
     * Returns empty list when no burning was explicitly requested (which is by default).
     */
    @Nonnull
    List<ErgoToken> getTokensToBurn();

    /**
     * Json representation of this transaction.
     */
    @Nonnull
    String toJson(boolean prettyPrint);

    /**
     * Json representation of this transaction.
     */
    @Nonnull
    String toJson(boolean prettyPrint, boolean formatJson);
}
