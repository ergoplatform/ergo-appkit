package org.ergoplatform.appkit;

import sigmastate.Values;

import java.util.List;

/**
 * Interface of UTXO boxes which can be accessed in the blockchain node.
 * Instances of this interface can be {@link BlockchainContext#getBoxesById(String...) obtained}
 * from {@link BlockchainContext} and {@link UnsignedTransactionBuilder#boxesToSpend(java.util.List) spent}
 * as part of a new transaction.
 */
public interface InputBox {
    /**
     * Returns the id of this box.
     */
    ErgoId getId();

    /**
     * Returns the ERG value stored in this box, i.e. unspent value in UTXO.
     */
    Long getValue();

    /**
     * Returns the tokens stored in this box.
     */
     List<ErgoToken> getTokens();

    /**
     * Returns the ErgoTree of the script guarding the box
     */
    Values.ErgoTree getErgoTree();

    String toJson(boolean prettyPrint);
}

