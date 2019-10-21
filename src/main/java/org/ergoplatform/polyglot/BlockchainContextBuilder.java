package org.ergoplatform.polyglot;

public interface BlockchainContextBuilder {
    BlockchainContext build() throws ErgoClientException;
}
