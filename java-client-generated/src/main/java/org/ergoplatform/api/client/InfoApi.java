package org.ergoplatform.api.client;

import org.ergoplatform.api.client.CollectionFormats.*;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.NodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InfoApi {
  /**
   * Get the information about the Node
   * 
   * @return Call&lt;NodeInfo&gt;
   */
  @GET("info")
  Call<NodeInfo> getNodeInfo();
    

}
