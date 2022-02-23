package org.ergoplatform.appkit;

import java.util.List;

import sigmastate.Values;

public interface IErgoBox {
    /**
     * Returns the ERG value stored in this box, i.e. unspent value in UTXO.
     */
    long getValue();

    /**
     * The height (block number) when the transaction containing this output box was
     * created.
     */
    int getCreationHeight();

    /**
     * Returns the tokens stored in this box.
     */
    List<ErgoToken> getTokens();

    /**
     * Returns values of the non-mandatory registers which are stored in the box (R4, R5, R6, R7, R8, R9)
     * Index 0 corresponds to R4, 1 -> R5, etc.
     */
    List<ErgoValue<?>> getRegisters();

    /**
     * Returns the ErgoTree of the script guarding the box
     */
    Values.ErgoTree getErgoTree();
}
