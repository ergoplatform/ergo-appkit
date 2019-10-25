package org.ergoplatform.polyglot;

public interface BlockchainContext {
    UnsignedTransactionBuilder newTxBuilder();

    InputBox[] getBoxesById(String... boxIds) throws ErgoClientException;

    ErgoProverBuilder newProver();

    byte getNetworkPrefix();

    int getHeight();
}

