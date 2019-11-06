package org.ergoplatform.example.util;

import org.ergoplatform.appkit.ErgoClient;

/**
 * This interface is used to represent {@link ErgoClient} whose communication with
 * Ergo network node can be mocked with some pre-defined test data.
 * This interface can be implemented in different ways, depending on the source
 * of the test data.
 * This interface allows to abstract testing code from this decision.
 */
public interface MockedErgoClient extends ErgoClient {
    /**
     * Response content for mocked `/info` request.
     */
    String getNodeInfoResp();

    /**
     * Response content for mocked `/blocks/lastHeaders/10` request.
     */
    String getLastHeadersResp();

    /**
     * Response content for mocked `/utxo/byId/{boxId}` request.
     */
    String getBoxResp();
}

