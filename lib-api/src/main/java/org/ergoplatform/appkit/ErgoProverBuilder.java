package org.ergoplatform.appkit;

import special.sigma.GroupElement;

import java.math.BigInteger;

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
     * Configure this builder to derive the new EIP-3 secret key with the given index.
     * The derivation uses master key derived from the mnemonic configured using {@link
     * ErgoProverBuilder#withMnemonic(SecretString, SecretString)}.
     *
     * @param index last index in the EIP-3 derivation path.
     */
    ErgoProverBuilder withEip3Secret(int index);

    /**
     * Configure this builder to use the given {@link SecretStorage} when building a new prover.
     *
     * @param storage {@link SecretStorage} instance containing encrypted secret seed phrase to be used in
     *                prover for generating proofs.
     */
    ErgoProverBuilder withSecretStorage(SecretStorage storage);

    /**
     * Configures this builder to use group elements (g, h, u, v) and secret x for a
     * ProveDHTuple statement when building a new prover.
     *
     * ProveDHTuple is a statement consisting of 4 group elements (g, h, u, v) and
     * requires the prover to prove knowledge of secret integer x such that.
     *
     *   u = g^x
     *    and
     *   y = h^x
     *
     * @param g {@Link GroupElement} instance defining g
     * @param h {@Link GroupElement} instance defining h
     * @param u {@Link GroupElement} instance defining u
     * @param v {@Link GroupElement} instance defining v
     * @param x {@Link BigInteger} instance defining x
     *
     * @see
     * <a href="https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28">example</a>
     *
     * @see
     * <a href="https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b54a173865a532de09bbcbf10da32ee2a491c8f9/sigmastate/src/main/scala/sigmastate/basics/DiffieHellmanTupleProtocol.scala#L58">implementation</a>
     */
    ErgoProverBuilder withDHTData(GroupElement g, GroupElement h, GroupElement u, GroupElement v, BigInteger x);

    /**
     * This allows adding additional secret for use in proveDlog, when the secret is not
     * part of the wallet.
     *
     * Multiple secrets can be added by calling this method multiple times.
     *
     * Multiple secrets are necessary for statements that need multiple proveDlogs, such
     * as proveDlog(a) && proveDlog(b), where a and b are two group elements.
     */
    ErgoProverBuilder withDLogSecret(BigInteger x);

    /**
     * Builds a new prover using provided configuration.
     */
    ErgoProver build();
}

