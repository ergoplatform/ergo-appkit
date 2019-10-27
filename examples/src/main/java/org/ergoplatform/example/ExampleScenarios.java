package org.ergoplatform.example;

import okhttp3.OkHttpClient;
import org.ergoplatform.polyglot.impl.ErgoNodeFacade;
import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.NodeInfo;
import org.ergoplatform.polyglot.*;
import retrofit2.Retrofit;

import java.util.Arrays;

public class ExampleScenarios {

    /**
     * Example creating and signing transaction that spends given boxes and aggregate
     * their ERGs into single new box protected with simple deadline based contract.
     *
     * @param ctx        blockchain context to be used to create the new transaction
     * @param seedPhrase secrete phrase used to generate secrets required to spend the boxes.
     * @param deadline   deadline (blockchain height) after which the newly created box can be spent
     * @param boxIds     string encoded (base16) ids of the boxes to be spent and agregated into the new box.
     */
    public SignedTransaction aggregateUtxoBoxes(BlockchainContext ctx, String seedPhrase, int deadline, String... boxIds) {
        UnsignedTransactionBuilder txB = ctx.newTxBuilder();
        InputBox[] boxes = ctx.getBoxesById(boxIds);
        Long total = Arrays.stream(boxes).map(b -> b.getValue()).reduce(0L, (x, y) -> x + y);
        UnsignedTransaction tx = txB
                .boxesToSpend(boxes)
                .outputs(
                        txB.outBoxBuilder()
                                .value(total)
                                .contract(
                                        ConstantsBuilder.create().item("deadline", deadline).build(),
                                        "{ HEIGHT > deadline }")
                                .build())
                .build();

        ErgoProverBuilder proverB = ctx.newProver();
        ErgoProver prover = proverB.withSeed(seedPhrase).build();
        SignedTransaction signed = prover.sign(tx);
        return signed;
    }

}
