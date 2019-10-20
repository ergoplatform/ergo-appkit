package org.ergoplatform.polyglot.ni;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ergoplatform.ErgoAddressEncoder;
import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.InfoApi;
import org.ergoplatform.api.client.NodeInfo;
import org.ergoplatform.polyglot.*;

import java.io.IOException;

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

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(nodeUrl + "/info").build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        InfoApi api = new ApiClient(nodeUrl).createService(InfoApi.class);
//        try {
//            Response<NodeInfo> response = api.getNodeInfo().execute();
//            System.out.println(response.body().toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
