package org.ergoplatform.appkit;

import org.ergoplatform.ErgoAddress;

import java.util.List;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;

/**
 * This interface is used to represent unsigned transactions after they are
 * built using {@link UnsignedTransactionBuilder}.
 *
 * @see UnsignedTransactionBuilder
 * @see SignedTransaction
 */
public interface UnsignedTransaction {
    UnsignedErgoLikeTransaction getTx();

    List<ExtendedInputBox> getBoxesToSpend();

    List<ErgoBox> getDataBoxes();

    ErgoLikeStateContext getStateContext();

    /**
     * Gets unsigned input boxes that will be used in this transaction
     */
    List<InputBox> getInputs();

    /**
     * Gets output boxes that will be created by this transaction
     */
    List<OutBox> getOutputs();

    /**
     * Gets data inputs that will be used in this transaction
     */
    List<InputBox> getDataInputs();

    /**
     * Returns the change address associated with this unsigned transaction
     */
    ErgoAddress getChangeAddress();
}
