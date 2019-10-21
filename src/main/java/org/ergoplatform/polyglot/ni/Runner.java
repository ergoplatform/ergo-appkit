package org.ergoplatform.polyglot.ni;

import okhttp3.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.ergoplatform.ErgoAddressEncoder;
import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.InfoApi;
import org.ergoplatform.api.client.JSON;
import org.ergoplatform.api.client.NodeInfo;
import org.ergoplatform.polyglot.*;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.RetrofitUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class Runner {

    public String sign(String boxId) {
        UnsignedTransactionBuilder txB = new UnsignedTransactionBuilderImpl(ErgoAddressEncoder.TestnetNetworkPrefix());
        OutBoxBuilder boxB = txB.outBoxBuilder();
        OutBox box = boxB
                .value(10)
                .contract(
                        ConstantsBuilder.create().item("deadline", 10).build(),
                        "{ HEIGHT > deadline }")
                .build();
        UnsignedTransaction tx = txB
                .inputs(boxId)
                .outputs(box)
                .build();

        ErgoProverBuilder proverB = new ErgoProverBuilder();
        ErgoProver prover = proverB.withSeed("abc").build();
        SignedTransaction signed = prover.sign(tx);
        return signed.toString();
    }

    public void request(String nodeUrl) {

        ApiClient client = new ApiClient(nodeUrl);
        OkHttpClient ok = client.getOkBuilder().build();
        Retrofit retrofit = client.getAdapterBuilder()
                .client(ok)
                .build();
        try {
            Method method = InfoApi.class.getMethod("getNodeInfo");
            NodeInfo res = RetrofitUtil.<NodeInfo>invokeServiceMethod(retrofit, method, null).execute().body();
            System.out.println(res);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
