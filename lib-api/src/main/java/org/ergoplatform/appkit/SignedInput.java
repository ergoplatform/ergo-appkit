package org.ergoplatform.appkit;

import org.ergoplatform.sdk.ErgoId;

import java.util.Map;

/**
 * Represents an input of a {@link SignedTransaction}.
 * Contains information necessary to spend the input.
 * The information includes proofs of knowledge of necessary secrets (aka signatures).
 * The proofs are generated by a {@link ErgoProver} configured with the secrets.
 * When transaction is validated the proofs are verified (this is a generalization of a signature
 * verification).
 * Additional information may be provided to the verifier via context variables attached to each
 * {@link SignedInput}.
 */
public interface SignedInput {
    /**
     * Bytes of the generated proofs (aka generalized signature)
     */
    byte[] getProofBytes();

    /**
     * Context variables attached by the prover to this input. They will be passed to the contract which
     * protects this input. Each variable is accessible by id using `getVar` function of ErgoScript.
     */
    Map<Byte, ErgoValue<?>> getContextVars();

    /**
     * Id of the box, which will be spent by the transaction.
     */
    ErgoId getId();

    /**
     * Transaction which contains this input.
     */
    SignedTransaction getTransaction();
}
