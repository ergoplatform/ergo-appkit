package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.explorer.client.DefaultApi;
import org.ergoplatform.explorer.client.model.Items;
import org.ergoplatform.explorer.client.model.ItemsA;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.explorer.client.model.TransactionInfo;

import retrofit2.Retrofit;
import retrofit2.RetrofitUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This class implements typed facade with Ergo Explorer API invocation methods.
 * It allows to bypass dynamic {@link java.lang.reflect.Proxy } generation which doesn't work under
 * Graal native-image.
 */
public class ExplorerFacade extends ApiFacade {
    /**
     * Get unspent boxes containing given address @GET("transactions/boxes/byAddress/unspent/{id}")
     *
     * @param id  address string (required)
     * @param offset  optional zero based offset of the first box in the list, default = 0
     * @param limit  optional number of boxes to retrive (default = 20)
     * @return list of requested outputs
     */
    static public List<OutputInfo> transactionsBoxesByAddressUnspentIdGet(
            Retrofit r, String id, Integer offset, Integer limit) throws ErgoClientException {
        return execute(r, () -> {
            Method method = DefaultApi.class.getMethod(
              "getApiV1BoxesUnspentByaddressP1", String.class, Integer.class, Integer.class, String.class);
            ItemsA res =
                    RetrofitUtil.<ItemsA>invokeServiceMethod(r, method, new Object[]{id, offset, limit, "asc"})
                            .execute().body();
            return res.getItems();
        });
    }

    public static List<TransactionInfo> getApiV1MempoolTransactionsByaddressP1(Retrofit r, String address, int offset, int limit) {
        return execute(r, () -> {
            Method method = DefaultApi.class.getMethod(
                "getApiV1MempoolTransactionsByaddressP1", String.class, Integer.class, Integer.class);
            Items res =
                RetrofitUtil.<Items<TransactionInfo>>invokeServiceMethod(r, method, new Object[]{address, offset, limit})
                    .execute().body();
            return res.getItems();
        });
    }

}
