package org.ergoplatform.polyglot;

import java.util.List;

public interface UnsignedTransactionBuilder {
    UnsignedTransactionBuilder boxesToSpend(InputBox... inputBoxIds);

    UnsignedTransactionBuilder outputs(OutBox... candidates);

    UnsignedTransaction build();

    BlockchainContext getCtx();

    NetworkType getNetworkType();

    OutBoxBuilder outBoxBuilder();

    List<InputBox> getInputBoxes();
}
