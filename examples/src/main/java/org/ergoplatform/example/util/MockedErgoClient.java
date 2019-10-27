package org.ergoplatform.example.util;

import org.ergoplatform.polyglot.ErgoClient;

/**
 *
 */
public interface MockedErgoClient extends ErgoClient {
    /**
     * Response content for `/info` request.
     */
    String getNodeInfoResp();

    /**
     * Response content for `/blocks/lastHeaders/10` request.
     */
    String getLastHeadersResp();

    /**
     * Response content for `/utxo/byId/{boxId}` request.
     */
    String getBoxResp();
}

