package org.ergoplatform.api.client;

import org.ergoplatform.api.client.CollectionFormats.*;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.ErgoTransactionOutput;
import org.ergoplatform.api.client.SerializedBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UtxoApi {
  /**
   * Get genesis boxes (boxes existed before the very first block)
   * 
   * @return Call&lt;List&lt;ErgoTransactionOutput&gt;&gt;
   */
  @GET("utxo/genesis")
  Call<List<ErgoTransactionOutput>> genesisBoxes();
    

  /**
   * Get box contents for a box by a unique identifier.
   * 
   * @param boxId ID of a wanted box (required)
   * @return Call&lt;ErgoTransactionOutput&gt;
   */
  @GET("utxo/byId/{boxId}")
  Call<ErgoTransactionOutput> getBoxById(
            @retrofit2.http.Path("boxId") String boxId            
  );

  /**
   * Get serialized box in Base16 encoding for a box with given unique identifier.
   * 
   * @param boxId ID of a wanted box (required)
   * @return Call&lt;SerializedBox&gt;
   */
  @GET("utxo/byIdBinary/{boxId}")
  Call<SerializedBox> getBoxByIdBinary(
            @retrofit2.http.Path("boxId") String boxId            
  );

}
