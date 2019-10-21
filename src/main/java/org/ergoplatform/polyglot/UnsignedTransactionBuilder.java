package org.ergoplatform.polyglot;

public interface UnsignedTransactionBuilder {
    byte getNetworkPrefix();

    UnsignedTransactionBuilder inputs(InputBox... inputBoxIds);

    UnsignedTransactionBuilder outputs(OutBox... candidates);

    UnsignedTransaction build();

    OutBoxBuilder outBoxBuilder();
}
