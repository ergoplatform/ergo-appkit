package org.ergoplatform.polyglot;

import org.ergoplatform.UnsignedErgoLikeTransaction;

public class UnsignedTransactionImpl implements UnsignedTransaction {
    private final UnsignedErgoLikeTransaction _tx;

    public UnsignedTransactionImpl(UnsignedErgoLikeTransaction tx) {
        _tx = tx;
    }

    UnsignedErgoLikeTransaction getTx() {
        return _tx;
    }
}
