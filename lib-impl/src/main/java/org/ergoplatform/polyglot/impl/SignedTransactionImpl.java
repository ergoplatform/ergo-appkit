package org.ergoplatform.polyglot.impl;

import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.polyglot.SignedTransaction;

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
