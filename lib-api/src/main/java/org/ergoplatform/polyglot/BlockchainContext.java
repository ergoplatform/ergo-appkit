package org.ergoplatform.polyglot;

/**
 * This interface represent a specific context of blockchain for execution
 * of transaction transaction building scenario.
 * It contains methods for accessing UTXO boxes, current blockchain state,
 * node information etc.
 * An instance of this interface can also be used to create new builders
 * for creating new transactions and provers (used for transaction signing).
 */
public interface BlockchainContext {
    /**
     * Creates a new builder of unsigned transaction.
     * A new builder is created for every call.
     */
    UnsignedTransactionBuilder newTxBuilder();

    /**
     * Retrieves UTXO boxes available in this blockchain context.
     *
     * @param boxIds array of string encoded ids of the boxes in the UTXO.
     * @return an array of requested boxes suitable for spending in transactions
     * created using this context.
     * @throws ErgoClientException if some boxes are not avaliable.
     */
    InputBox[] getBoxesById(String... boxIds) throws ErgoClientException;

    /**
     * Creates a new builder of {@link ErgoProver}.
     */
    ErgoProverBuilder newProverBuilder();

    /**
     * Returns a network type of this context.
     */
    NetworkType getNetworkType();

    /**
     * Return the height of the blockchain at the point of time when this
     * context was created.
     * The context is immutable, thus to obtain a new height later in time
     * a new context should be should be created.
     */
    int getHeight();
}

