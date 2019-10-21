package org.ergoplatform.polyglot;

public interface ErgoProverBuilder {
    ErgoProver build();

    ErgoProverBuilder withSeed(String seed);
}

