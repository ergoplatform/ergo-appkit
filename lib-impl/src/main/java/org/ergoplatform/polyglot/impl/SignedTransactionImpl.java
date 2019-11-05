package org.ergoplatform.polyglot.impl;

import com.google.gson.Gson;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.polyglot.SignedTransaction;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.JSON;
import sigmastate.Values;

public class SignedTransactionImpl implements SignedTransaction {

    private final BlockchainContextImpl _ctx;
    private final ErgoLikeTransaction _tx;

    public SignedTransactionImpl(BlockchainContextImpl ctx, ErgoLikeTransaction tx) {
        _ctx = ctx;
        _tx = tx;
    }

    /**
     * Returns underlying {@link ErgoLikeTransaction}
     */
    ErgoLikeTransaction getTx() {
        return _tx;
    }

    @Override
    public String toString() {
        return "Signed(" + _tx + ")";
    }

    @Override
    public String getId() {
        return _tx.id();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        ErgoTransaction tx = ScalaBridge.isoErgoTransaction().from(_tx);
        if (prettyPrint) {
            tx.getOutputs().forEach(o -> {
                Values.ErgoTree tree = ScalaBridge.isoStringToErgoTree().to(o.getErgoTree());
                o.ergoTree(tree.toString());
            });
        }
        Gson gson = prettyPrint ? JSON.createGson().setPrettyPrinting().create() : _ctx.getApiClient().getGson();
        String json = gson.toJson(tx);
        return json;
    }
}
