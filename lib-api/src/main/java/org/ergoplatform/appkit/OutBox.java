package org.ergoplatform.appkit;

import org.ergoplatform.ErgoBoxCandidate;
import sigmastate.Values;

import java.util.List;

/**
 * This interface is used to represent output boxes on newly created transactions.
 * Each {@link OutBox} corresponds to {@link ErgoBoxCandidate} which is not yet part
 * of UTXO and hence doesn't have transaction id and box index parameter.
 *
 * @see OutBoxBuilder
 */
public interface OutBox {

    /**
     * Returns the amount of ERG stored in this box.
     */
    long getValue();

    /**
     * The height (block number) when the transaction containing this output box was
     * created.
     */
    int getCreationHeight();

    /**
     * Returns list of tokens held in R2 of the current OutBox.
     */
    List<ErgoToken> getTokens();

    /**
     * Returns list of additional registers that were set in this OutBox
     */
    List<ErgoValue<?>> getRegisters();

    /**
     * Returns the ErgoTree of the script guarding the box
     */
    Values.ErgoTree getErgoTree();

    /**
     * Returns the serialized bytes of this output box without any transaction reference data.
     */
    byte[] getBytesWithNoRef();

    /**
     * Converts this box candidate into a new instance of {@link InputBox} by
     * associating it with the given transaction and output position.
     * This method can be used to create input boxed from scratch, without
     * retrieving them from the UTXOs. Thus created boxes can be indistinguishable from those
     * loaded from blockchain node, and as result can be used to create new transactions.
     * This method can also be used to create chains of transactions in advance
     *
     * @param txId        the id of the transaction of which the newly created box will be output
     * @param outputIndex zero-based position (index) of the box in the outputs of the transaction.
     * @return a new {@link InputBox} representing UTXOs' box
     */
    InputBox convertToInputWith(String txId, short outputIndex);
}
