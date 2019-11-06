package org.ergoplatform.appkit;

/**
 * This interface is used to configure and build a new {@link ErgoProver prover}.
 */
public interface ErgoProverBuilder {
    /**
     * Configure this builder to use the given seed when building a new prover.
     * @param mnemonicPhrase secret seed phrase to be used in prover for generating proofs.
     * @param mnemonicPass password to protect secret seed phrase.
     */
    ErgoProverBuilder withMnemonic(String mnemonicPhrase, String mnemonicPass);

    /**
     * Builds a new prover using provided configuration.
     */
    ErgoProver build();
}

