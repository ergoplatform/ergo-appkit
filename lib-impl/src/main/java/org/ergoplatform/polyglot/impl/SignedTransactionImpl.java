package org.ergoplatform.polyglot.impl;

import com.google.gson.Gson;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.polyglot.SignedTransaction;
import org.ergoplatform.restapi.client.ErgoTransaction;

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
    public String toJson() {
        ErgoTransaction tx = ScalaBridge.isoErgoTransaction().from(_tx);
        Gson gson = _ctx.getApiClient().getGson();
        String json = gson.toJson(tx);
        return json;
    }
}
