package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.ReducedErgoLikeTransaction;
import org.ergoplatform.appkit.ReducedTransaction;

public class ReducedTransactionImpl implements ReducedTransaction {
    private final BlockchainContextImpl _ctx;
    private final ReducedErgoLikeTransaction _tx;
    private final int _txCost;

    public ReducedTransactionImpl(
            BlockchainContextImpl ctx,
            ReducedErgoLikeTransaction tx, int txCost) {
        _ctx = ctx;
        _tx = tx;
        _txCost = txCost;
    }

    /**
     * Returns underlying {@link ReducedErgoLikeTransaction}
     */
    public ReducedErgoLikeTransaction getTx() {
        return _tx;
    }

    @Override
    public int getCost() {
        return _txCost;
    }

}
