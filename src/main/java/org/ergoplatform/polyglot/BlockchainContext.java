package org.ergoplatform.polyglot;

public interface BlockchainContext {
    UnsignedTransactionBuilder newUnsignedTransaction();

    InputBox getBoxById(String boxId) throws ErgoClientException;

    ErgoProverBuilder newProver();
}

