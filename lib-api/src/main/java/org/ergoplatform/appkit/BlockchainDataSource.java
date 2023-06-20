package org.ergoplatform.appkit;

import java.util.List;

/**
 * Interface to access blockchain data source. Data source always performs a request to the
 * blockchain data, and does not hold or cache any information.
 */
public interface BlockchainDataSource {
    /**
     * @return blockchain parameters this data source is working with. The returned value
     *         might be cached by the data source as it is not subject to change frequently
     */
    BlockchainParameters getParameters();

    /**
     * Get the last headers objects, sorted by descending order
     *
     * @param count           count of a wanted block headers (required)
     * @param onlyFullHeaders restrict returned list to full headers. If set to true, amount of
     *                        returned block headers might be less than "count"
     * @return List&lt;BlockHeader&gt;
     */
    List<BlockHeader> getLastBlockHeaders(int count, boolean onlyFullHeaders);

    /**
     * Get box contents for an unspent box by a unique identifier for use as an Input,
     * including mempool boxes
     *
     * @param boxId ID of a wanted box (required)
     * @param findInPool whether to find boxes that are currently in mempool
     * @param findInSpent whether to find boxes that are spent
     * @return InputBox
     */
    InputBox getBoxById(String boxId, boolean findInPool, boolean findInSpent);

    /**
     * Send an Ergo transaction
     * Headers({ "Content-Type:application/json" })
     * POST("transactions")
     *
     * @param tx signed transaction to be posted to the blockchain
     * @return transaction id of the submitted transaction
     */
    String sendTransaction(SignedTransaction tx);

    /**
     * Get confirmed unspent boxes owned by the given address starting from the given offset up to
     * the given limit (basically one page of the boxes).
     *
     * @param address owner of the boxes to be retrieved
     * @param offset  optional zero based offset of the first box in the list,
     *                default = 0
     * @param limit   optional number of boxes to retrieve. Note that returned list might
     *                contain less elements if data for some boxes couldn't be retrieved
     * @return a requested chunk of boxes owned by the address
     */
    List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit);

    /**
     * Get unspent boxes owned by the given address starting from the given offset up to
     * the given limit (basically one page of the boxes), restricted to mempool.
     *
     * @param address owner of the boxes to be retrieved
     * @param offset  optional zero based offset of the first box in the list,
     *                default = 0
     * @param limit   optional number of boxes to retrieve. Note that returned list might
     *                contain less elements if data for some boxes couldn't be retrieved
     * @return a requested chunk of boxes owned by the address
     */
    List<InputBox> getUnconfirmedUnspentBoxesFor(Address address, int offset, int limit);

    /**
     * @return unconfirmed transactions from mempool
     */
    List<Transaction> getUnconfirmedTransactions(int offset, int limit);
}
