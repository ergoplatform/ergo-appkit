package org.ergoplatform.polyglot;

public interface OutBox {
    ErgoId getId();
    UnsignedTransaction getTx();
    int getBoxIndex();
    long getValue();
    ErgoToken token(ErgoId id);
}
