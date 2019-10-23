package org.ergoplatform.polyglot;

public interface BlockchainContextBuilder {
    int LastHeadersInContext = 10;
    BlockchainContext build() throws ErgoClientException;
}
