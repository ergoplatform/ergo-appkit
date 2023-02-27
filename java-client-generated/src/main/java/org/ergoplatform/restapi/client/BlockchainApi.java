package org.ergoplatform.restapi.client;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * BlockchainApi. Needs enabled enhanced indices.
 */
public interface BlockchainApi {
    /**
     * Get box contents for a box by a unique identifier. Spent boxes are returned as well as
     * unspent boxes, but not mempool.
     *
     * @param boxId ID of a wanted box (required)
     * @return Call&lt;ErgoTransactionOutput&gt;
     */
    @GET("blockchain/box/byId/{boxId}")
    Call<ErgoTransactionOutput> getBoxById(
        @retrofit2.http.Path("boxId") String boxId
    );

}
