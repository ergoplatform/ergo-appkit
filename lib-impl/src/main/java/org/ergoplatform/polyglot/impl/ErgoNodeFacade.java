package org.ergoplatform.polyglot.impl;

import org.ergoplatform.polyglot.ErgoClientException;
import org.ergoplatform.restapi.client.*;
import retrofit2.Retrofit;
import retrofit2.RetrofitUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

public class ErgoNodeFacade {
    /**
     * Get the information about the Node
     *
     * @return Call&lt;NodeInfo&gt;
     */
    static public NodeInfo getNodeInfo(Retrofit r) throws ErgoClientException {
        return execute(r, () -> {
            Method method = InfoApi.class.getMethod("getNodeInfo");
            NodeInfo res = RetrofitUtil.<NodeInfo>invokeServiceMethod(r, method, null).execute().body();
            return res;
        });
    }

    /**
     * Get the last headers objects
     *
     * @param count count of a wanted block headers (required)
     * @return List&lt;BlockHeader&gt;
     */
    static public List<BlockHeader> getLastHeaders(Retrofit r, BigDecimal count) throws ErgoClientException {
        return execute(r, () -> {
            Method method = BlocksApi.class.getMethod("getLastHeaders", BigDecimal.class);
            List<BlockHeader> res = RetrofitUtil.<List<BlockHeader>>invokeServiceMethod(r, method,
             new Object[]{count}).execute().body();
            return res;
        });
    }

    static public ErgoTransactionOutput getBoxById(Retrofit r, String boxId) throws ErgoClientException {
        return execute(r, () -> {
            Method method = UtxoApi.class.getMethod("getBoxById", String.class);
            ErgoTransactionOutput res = RetrofitUtil.<ErgoTransactionOutput>invokeServiceMethod(r, method,
                    new Object[]{boxId}).execute().body();
            return res;
        });
    }

    static private ErgoClientException clientError(Retrofit r, Throwable cause) {
        return new ErgoClientException("ErgoNodeFacade error: " + r.toString(), cause);
    }

    private interface Supplier<T> {
        T get() throws NoSuchMethodException, IOException;
    }

    static private <T> T execute(Retrofit r, Supplier<T> block) throws ErgoClientException {
        try {
            return block.get();
        } catch (NoSuchMethodException e) {
            throw clientError(r, e);
        } catch (IOException e) {
            throw clientError(r, e);
        }
    }

}

