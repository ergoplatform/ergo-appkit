package org.ergoplatform.polyglot;

public interface OutBox {
    ErgoId getId();
    UnsignedTransaction getTx();
    int getBoxIndex();
    long getValue();
    Proposition getProposition();
    ErgoToken token(ErgoId id);
}
