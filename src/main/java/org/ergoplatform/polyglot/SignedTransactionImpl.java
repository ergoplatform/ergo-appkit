package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoLikeTransaction;

public class SignedTransactionImpl implements SignedTransaction {
    private final ErgoLikeTransaction _tx;

    public SignedTransactionImpl(ErgoLikeTransaction tx) {
        _tx = tx;
    }

    @Override
    public String toString() {
        return "Signed(" + _tx + ")" ;
    }
}
