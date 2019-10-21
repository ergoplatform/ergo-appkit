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
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class Runner {

    public String sign(String nodeUrl, String... boxIds) throws ErgoClientException {
        ApiClient client = new ApiClient(nodeUrl);
        BlockchainContext ctx =
         new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.TestnetNetworkPrefix()).build();

        List<InputBox> list = new ArrayList<>();
        for (String id : boxIds) {
            InputBox box = ctx.getBoxById(id);
            list.add(box);
        }
        InputBox[] inputs = list.toArray(new InputBox[0]);
        UnsignedTransactionBuilder txB = ctx.newUnsignedTransaction();
        UnsignedTransaction tx = txB
                .inputs(inputs)
                .outputs(
                        txB.outBoxBuilder()
                                .value(10)
                                .contract(
                                        ConstantsBuilder.create().item("deadline", 10).build(),
                                        "{ HEIGHT > deadline }")
                                .build())
                .build();

        ErgoProverBuilder proverB = ctx.newProver();
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

            NodeInfo res = ErgoNodeFacade.getNodeInfo(retrofit);
            System.out.println(res);
        } catch (ErgoClientException e) {
            e.printStackTrace();
        }
    }

}
