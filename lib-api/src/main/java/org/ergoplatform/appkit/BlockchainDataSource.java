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
     * Send an Ergo transaction
     * Headers({ "Content-Type:application/json" })
     * POST("transactions")
     *
     * @param tx signed transaction to be posted to the blockchain
     * @return transaction id of the submitted transaction
     */
    String sendTransaction(SignedTransaction tx);

    /**
     * Get a list of unspent boxes for a wallet set up in data source. When data source is a node,
     * it is the node's users wallet.
     *
     * @param minConfirmations   Minimal number of confirmations (optional)
     * @param minInclusionHeight Minimal box inclusion height (optional)
     * @return list of InputBoxes to use for a new transaction
     */
    List<InputBox> getWalletUnspentBoxes(int minConfirmations, int minInclusionHeight);

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
}
