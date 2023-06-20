package org.ergoplatform.restapi.client;

import org.ergoplatform.explorer.client.model.Items;
import org.ergoplatform.explorer.client.model.TotalBalance;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * BlockchainApi. Needs enabled enhanced indices.
 */
public interface BlockchainApi {
    @GET("blockchain/indexedHeight")
    Call<BlockchainIndexHeight> getBlockchainIndexHeight();

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

    /**
     * Retrieve unspent boxes by their associated address
     *
     * @param sortDirection use {@link #sortDirectionNewestFirst} or {@link #sortDirectionOldestFirst}
     */
    @POST("blockchain/box/unspent/byAddress/")
    Call<List<ErgoTransactionOutput>> getUnspentBoxesByAddress(
        @retrofit2.http.Body String address,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("offset") Integer offset,
        @retrofit2.http.Query("sortDirection") String sortDirection
    );

    /**
     * Retrieve unspent boxes by their associated ergotree
     *
     * @param sortDirection use {@link #sortDirectionNewestFirst} or {@link #sortDirectionOldestFirst}
     */
    @POST("blockchain/box/unspent/byErgoTree/")
    Call<List<ErgoTransactionOutput>> getUnspentBoxesByErgoTree(
        @retrofit2.http.Body String ergoTree,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("offset") Integer offset,
        @retrofit2.http.Query("sortDirection") String sortDirection
    );

    public static String sortDirectionOldestFirst = "asc";
    public static String sortDirectionNewestFirst = "desc";

    @GET("blockchain/transaction/byId/{txId}")
    Call<BlockchainTransaction> getTxById(
        @retrofit2.http.Path("txId") String txId
    );

    @GET("blockchain/transaction/byIndex/{txIdx}")
    Call<BlockchainTransaction> getTxByIndex(
        @retrofit2.http.Path("txIdx") String txIdx
    );

    @POST("blockchain/transaction/byAddress/")
    Call<Items<BlockchainTransaction>> getTransactionsByAddress(
        @retrofit2.http.Body String address,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("offset") Integer offset
    );

    @GET("blockchain/token/byId/{tokenId}")
    Call<BlockchainToken> getTokenById(
        @retrofit2.http.Path("tokenId") String tokenId
    );

    @POST("blockchain/balance/")
    Call<TotalBalance> getBalance(
        @retrofit2.http.Body String address
    );}
