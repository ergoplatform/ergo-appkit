package org.ergoplatform.polyglot;

/**
 * This interface is used to configure and build a new {@link ErgoProver prover}.
 */
public interface ErgoProverBuilder {
    /**
     * Configure this builder to use the given seed when building a new prover.
     * @param seed secret seed phrase to be used in prover for generating proofs.
     */
    ErgoProverBuilder withSeed(String seed);

    /**
     * Builds a new prover using provided configuration.
     */
    ErgoProver build();
}

