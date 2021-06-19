package org.ergoplatform.restapi.client;

import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.ApiError;
import java.math.BigDecimal;
import org.ergoplatform.restapi.client.BlockHeader;
import org.ergoplatform.restapi.client.BlockTransactions;
import org.ergoplatform.restapi.client.FullBlock;
import org.ergoplatform.restapi.client.MerkleProof;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * API tests for BlocksApi
 */
public class BlocksApiTest extends PeerFinder {

    private BlocksApi api;

    @Before
    public void setup() {
        api = findPeer(true).createService(BlocksApi.class);
    }


    /**
     * Get the block header info by a given signature
     *
     * 
     */
    @Test
    public void getBlockHeaderByIdTest() {
        String headerId = null;
        // BlockHeader response = api.getBlockHeaderById(headerId);

        // TODO: test validations
    }

    /**
     * Get the block transactions info by a given signature
     *
     * 
     */
    @Test
    public void getBlockTransactionsByIdTest() {
        String headerId = null;
        // BlockTransactions response = api.getBlockTransactionsById(headerId);

        // TODO: test validations
    }

    /**
     * Get headers in a specified range
     *
     * 
     */
    @Test
    public void getChainSliceTest() {
        Integer fromHeight = null;
        Integer toHeight = null;
        // java.util.List<BlockHeader> response = api.getChainSlice(fromHeight, toHeight);

        // TODO: test validations
    }

    /**
     * Get the header ids at a given height
     *
     * 
     */
    @Test
    public void getFullBlockAtTest() {
        Integer blockHeight = null;
        // java.util.List<String> response = api.getFullBlockAt(blockHeight);

        // TODO: test validations
    }

    /**
     * Get the full block info by a given signature
     *
     * 
     */
    @Test
    public void getFullBlockByIdTest() {
        String headerId = null;
        // FullBlock response = api.getFullBlockById(headerId);

        // TODO: test validations
    }

    /**
     * Get the Array of header ids
     *
     * 
     */
    @Test
    public void getHeaderIdsTest() {
        Integer limit = null;
        Integer offset = null;
        // java.util.List<String> response = api.getHeaderIds(limit, offset);

        // TODO: test validations
    }

    /**
     * Get the last headers objects
     *
     * 
     */
    @Test
    public void getLastHeadersTest() {
        BigDecimal count = null;
        // java.util.List<BlockHeader> response = api.getLastHeaders(count);

        // TODO: test validations
    }

    /**
     * Get the persistent modifier by its id
     *
     * 
     */
    @Test
    public void getModifierByIdTest() {
        String modifierId = null;
        // Void response = api.getModifierById(modifierId);

        // TODO: test validations
    }

    /**
     * Get Merkle proof for transaction
     *
     * 
     */
    @Test
    public void getProofForTxTest() {
        String headerId = null;
        String txId = null;
        // MerkleProof response = api.getProofForTx(headerId, txId);

        // TODO: test validations
    }

    /**
     * Send a mined block
     *
     * 
     */
    @Test
    public void sendMinedBlockTest() {
        FullBlock body = null;
        // Void response = api.sendMinedBlock(body);

        // TODO: test validations
    }
}
