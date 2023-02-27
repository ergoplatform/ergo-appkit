package org.ergoplatform.restapi.client;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

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

    @POST("blockchain/box/unspent/byAddress/{address}")
    Call<List<ErgoTransactionOutput>> getUnspentBoxesByAddress(
        @retrofit2.http.Path("address") String address,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("offset") Integer offset
    );

}
