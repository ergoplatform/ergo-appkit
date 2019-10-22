package org.ergoplatform.api.client;

import org.ergoplatform.api.client.CollectionFormats.*;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.ExternalCandidateBlock;
import org.ergoplatform.api.client.InlineResponse2004;
import org.ergoplatform.api.client.PowSolutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MiningApi {
  /**
   * Read miner reward address
   * 
   * @return Call&lt;InlineResponse2004&gt;
   */
  @GET("mining/rewardAddress")
  Call<InlineResponse2004> miningReadMinerRewardAddress();
    

  /**
   * Request block candidate
   * 
   * @return Call&lt;ExternalCandidateBlock&gt;
   */
  @GET("mining/candidate")
  Call<ExternalCandidateBlock> miningRequestBlockCandidate();
    

  /**
   * Submit solution for current candidate
   * 
   * @param body  (required)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("mining/solution")
  Call<Void> miningSubmitSolution(
                    @retrofit2.http.Body PowSolutions body    
  );

}
