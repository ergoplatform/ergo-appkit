package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.ReducedErgoLikeTransaction;
import org.ergoplatform.appkit.ReducedErgoLikeTransactionSerializer$;
import org.ergoplatform.appkit.ReducedTransaction;
import sigmastate.serialization.SigmaSerializer$;
import sigmastate.utils.SigmaByteWriter;

import java.util.Objects;

public class ReducedTransactionImpl implements ReducedTransaction {
    private final BlockchainContextBase _ctx;
    private final ReducedErgoLikeTransaction _tx;
    private final int _txCost;

    public ReducedTransactionImpl(
            BlockchainContextBase ctx,
            ReducedErgoLikeTransaction tx, int txCost) {
        _ctx = ctx;
        _tx = tx;
        _txCost = txCost;
    }

    /**
     * Returns underlying {@link ReducedErgoLikeTransaction}
     */
    @Override
    public ReducedErgoLikeTransaction getTx() {
        return _tx;
    }

    @Override
    public int getCost() {
        return _txCost;
    }

    @Override
    public byte[] toBytes() {
        SigmaByteWriter w = SigmaSerializer$.MODULE$.startWriter();
        ReducedErgoLikeTransactionSerializer$.MODULE$.serialize(_tx, w);
        w.putUInt(_txCost);
        return w.toBytes();
    }

    @Override
    public int hashCode() {
        return 31 * _tx.hashCode() + _txCost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReducedTransactionImpl) {
           ReducedTransactionImpl that = (ReducedTransactionImpl)obj;
           return Objects.equals(that._tx, this._tx) && that._txCost == this._txCost;
        }
        return false;
    }
}
