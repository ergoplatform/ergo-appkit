package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.explorer.client.TransactionsApi;
import org.ergoplatform.explorer.client.model.TransactionOutput;
import retrofit2.Retrofit;
import retrofit2.RetrofitUtil;

import java.lang.reflect.Method;

/**
 * This class implements typed facade with Ergo Explorer API invocation methods.
 * It allows to bypass dynamic {@link java.lang.reflect.Proxy } generation which doesn't work under
 * Graal native-image.
 */
public class ExplorerFacade extends ApiFacade {
    /**
     * Get unspent boxes containing given address @GET("transactions/boxes/byAddress/unspent/{id}")
     *
     * @param id  (required)
     * @return TransactionOutput
     */
    static public TransactionOutput transactionsBoxesByAddressUnspentIdGet(
            Retrofit r, String id) throws ErgoClientException {
        return execute(r, () -> {
            Method method = TransactionsApi.class.getMethod("transactionsBoxesByAddressUnspentIdGet");
            TransactionOutput res =
                    RetrofitUtil.<TransactionOutput>invokeServiceMethod(r, method, new Object[]{id})
                            .execute().body();
            return res;
        });
    }

}
