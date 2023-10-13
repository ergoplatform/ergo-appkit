package org.ergoplatform.appkit;

import java.util.List;

/**
 * EIP-41/EIP-11 compliant multi sig transaction
 */
public abstract class MultisigTransaction {

    /**
     * @return transaction that is going to be signed
     */
    abstract public ReducedTransaction getTransaction();

    /**
     * adds a new hint to this multisig transaction
     * @param prover to add commitment for
     */
    abstract public void addHint(ErgoProver prover);

    /**
     * adds the hints not present on this instance from another multisig transaction instance
     * for the same transaction.
     */
    abstract public void mergeHints(MultisigTransaction other);

    /**
     * adds the hints not present on this instance from the EIP-11 json
     */
    abstract public void mergeHints(String json);

    /**
     * @return list of participants that added a hint for the transaction
     */
    abstract public List<Address> getCommitingParticipants();
    /**
     * @return true if SignedTransaction can be built
     */
    abstract public boolean isHintBagComplete();

    /**
     * @return the signed transaction if enough commitments are available
     * @throws IllegalStateException if {@link #isHintBagComplete()} is false
     */
    abstract public SignedTransaction toSignedTransaction();

    /**
     * @return EIP-11 compliant json string to transfer the partially signed transaction to the
     * next participant
     */
    abstract public String hintsToJson();

    /**
     * constructs a multi sig transaction from a reduced transaction
     */
    public static MultisigTransaction fromTransaction(ReducedTransaction transaction) {
        throw new UnsupportedOperationException();
    }

    /**
     * constructs a multi sig transaction from EIP-11 json string
     */
    public static MultisigTransaction fromJson(String json) {
        throw new UnsupportedOperationException();
    }
}
