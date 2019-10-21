package org.ergoplatform.polyglot;

import org.ergoplatform.api.client.InfoApi;
import org.ergoplatform.api.client.NodeInfo;
import retrofit2.Retrofit;
import retrofit2.RetrofitUtil;

import java.io.IOException;
import java.lang.reflect.Method;

public class ErgoNodeFacade {
  static private ErgoClientException clientError(Retrofit r, Throwable cause) {
    return new ErgoClientException("ErgoNodeFacade error: " + r.toString(), cause);
  }

  public static NodeInfo getNodeInfo(Retrofit r) throws ErgoClientException {
    try {
      Method method = InfoApi.class.getMethod("getNodeInfo");
      NodeInfo res = RetrofitUtil.<NodeInfo>invokeServiceMethod(r, method, null).execute().body();
      return res;
    } catch (NoSuchMethodException e) {
      throw clientError(r, e);
    } catch (IOException e) {
      throw clientError(r, e);
    }
  }
}

