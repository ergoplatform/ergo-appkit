package org.ergoplatform.example.util;

/**
 *
 */
public interface MockedRunner extends Runner {
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

