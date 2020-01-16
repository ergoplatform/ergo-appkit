package org.ergoplatform.appkit;

import special.sigma.GroupElement;

/**
 * This interface is used to configure and build a new {@link ErgoProver prover}.
 */
public interface ErgoProverBuilder {
    /**
     * Configure this builder to use the given seed when building a new prover.
     *
     * @param mnemonicPhrase secret seed phrase to be used in prover for generating proofs.
     * @param mnemonicPass   password to protect secret seed phrase.
     */
    ErgoProverBuilder withMnemonic(SecretString mnemonicPhrase, SecretString mnemonicPass);

    /**
     * Configure this builder to use the given mnemonic when building a new prover.
     *
     * @param mnemonic {@link Mnemonic} instance containing secret seed phrase to be used in prover for
     *                 generating proofs.
     */
    ErgoProverBuilder withMnemonic(Mnemonic mnemonic);

    /**
     * Configure this builder to use the given {@link SecretStorage} when building a new prover.
     *
     * @param storage {@link SecretStorage} instance containing encrypted secret seed phrase to be used in
     *                prover for generating proofs.
     */
    ErgoProverBuilder withSecretStorage(SecretStorage storage);

    /**
     * Add DHT prover input using this prover's secret as belonging to the first party (i.e. Alice).
     *
     * @param otherParty       address of the other party whose public key is to be included in
     *                         Diffie-Hellman Tuple. If this prover corresponds to Alice, then `otherParty`
     *                         is Bob.
     * @param additionalSecret an additionalSecret necessary to construct DHT when this prover corresponds to
     *                         a first party (e.g. Alice)
     * @see
     * <a href="https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28">example</a>
     */
    ErgoProverBuilder withFirstDHTSecret(Address otherParty, GroupElement additionalSecret);

    /**
     * Add DHT prover input using this prover's secret as belonging to second party (i.e. Bob).
     *
     * @param otherParty address of the other party whose public key is to be included in
     *                   Diffie-Hellman Tuple. This prover is assumed to belong to Bob, then `otherParty`
     *                   is Alice.
     * @see
     * <a href="https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28">example</a>
     */
    ErgoProverBuilder withSecondDHTSecret(Address otherParty);

    /**
     * Builds a new prover using provided configuration.
     */
    ErgoProver build();
}

