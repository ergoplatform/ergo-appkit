package org.ergoplatform.appkit;

import java.util.List;

/**
 * Interface to access blockchain data source. Data source always performs a request to the
 * blockchain data, and does not hold or cache any information.
 */
public interface BlockchainDataSource {
    /**
     * @return blockchain parameters this data source is working with
     */
    BlockchainParameters getParameters();

    /**
     * Get the last headers objects, sorted by descending order
     *
     * @param count count of a wanted block headers (required)
     * @return List&lt;BlockHeader&gt;
     */
    List<BlockHeader> getLastBlockHeaders(int count);

    /**
     * Get box contents for a box by a unique identifier for use as an Input
     *
     * @param boxId ID of a wanted box (required)
     * @return InputBox
     */
    InputBox getBoxById(String boxId);

    /**
     * Get box contents for a box by a unique identifier for use as an Input, including mempool boxes
     *
     * @param boxId ID of a wanted box (required)
     * @return InputBox
     */
    InputBox getBoxByIdWithMemPool(String boxId);

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
     * Get unspent boxes owned by the given address starting from the given offset up to
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
     * Get all unspent boxes in the current node wallet, without unconfirmed boxes
     * @return List of input boxes representing all known
     */
    List<InputBox> getUnspentWalletBoxes();
    /**
     * Get all unspent boxes in the current node wallet, without unconfirmed boxes
     * @param minConfirmations Minimum number of confirmations for a box to be included in the returned list
     * @param minInclusionHeight Minimum creation height for a box to be included in the returned list
     * @return List of input boxes representing all known
     */
    List<InputBox> getUnspentWalletBoxes(int minConfirmations, int minInclusionHeight);

    /**
     * Get all unspent boxes in the current node wallet, including unconfirmed boxes from the MemPool
     * @return List of all unspent boxes in this node's current wallet
     */
    List<InputBox> getUnconfirmedUnspentWalletBoxes();
    /**
     * Get all unspent boxes in the current node wallet, including unconfirmed boxes from the MemPool
     * @param minInclusionHeight Minimum creation height for a box to be included in the returned list
     * @return List of all unspent boxes in this node's current wallet
     */
    List<InputBox> getUnconfirmedUnspentWalletBoxes(int minInclusionHeight);

    /**
     * @return unconfirmed transactions from mempool
     */
    List<Transaction> getUnconfirmedTransactions(int offset, int limit);
}
