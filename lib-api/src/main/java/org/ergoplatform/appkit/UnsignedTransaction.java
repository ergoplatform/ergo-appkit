package org.ergoplatform.appkit;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;

import java.util.List;

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
}
