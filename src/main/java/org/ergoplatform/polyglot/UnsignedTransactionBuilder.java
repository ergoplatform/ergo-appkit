package org.ergoplatform.polyglot;

import org.ergoplatform.UnsignedErgoLikeTransaction;

public interface UnsignedTransactionBuilder {
    UnsignedTransactionBuilder withInputs(String... inputBoxIds);

    UnsignedTransactionBuilder withCandidates(OutBox... candidates);

    UnsignedTransaction build();
}
