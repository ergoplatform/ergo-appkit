package org.ergoplatform.appkit;

import sigmastate.Values;

import java.util.List;
import java.util.function.Function;

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
     * Creates a new PreHeader based on this blockchain context.
     * The header of the last block is used to derive default values for the new PreHeader.
     *
     * @return builder which can be used to set all properties of the new pre-header
     */
    PreHeaderBuilder createPreHeader();

    /**
     * Parses the given json string and create a {@link SignedTransaction} instance.
     * Should be inverse to {@link SignedTransaction#toJson(boolean)} i.e. preserve
     * {@code signedTxFromJson(signed.toJson(false)).toJson(false) == signed.toJson(false)}
     */
    SignedTransaction signedTxFromJson(String json);

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
     * @throws ErgoClientException if some boxes are not available.
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

    /**
     * Sends a signed transaction to a blockchain node. On the blockchain node the transaction
     * is first placed in a pool and then later can be selected by miner and included in the next block.
     * The new transactions are also replicated all over the network.
     *
     * @param tx a signed {@link SignedTransaction transaction} to be sent to the blockchain node
     */
    String sendTransaction(SignedTransaction tx);

    ErgoWallet getWallet();

    ErgoContract newContract(Values.ErgoTree ergoTree);

    ErgoContract compileContract(Constants constants, String ergoScript);

    /** Default size of the chunk (aka page size) used in API requests. */
    int DEFAULT_LIMIT_FOR_API = 20;

    /**
     * Get unspent boxes owned by the given address starting from the given offset up to
     * the given limit (basically one page of the boxes).
     *
     * @param address owner of the boxes to be retrieved
     * @param offset  optional zero based offset of the first box in the list,
     *                default = 0
     * @param limit   optional number of boxes to retrieve. Note that returned list might
     *                contain less elements if data for some boxes couldn't be retrieved
     * @return a requested chunk of boxes owned by the address
     */
    List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit);

    /**
     * Get unspent boxes owned by the given address starting from the given offset up to
     * the given limit (basically one page of the boxes).
     * Uses {@link BoxOperations#getCoveringBoxesFor(long, List, Function)} with
     * Explorer API as data source.
     *
     * Deprecated - call {@link BoxOperations#getCoveringBoxesFor(long, List, Function)} directly
     *
     * @param address owner of the boxes to be retrieved
     * @param amountToSpend amount of NanoErgs to be covered
     * @param tokensToSpend ErgoToken to spent
     * @return a new instance of {@link CoveringBoxes} set
     */
    @Deprecated
    CoveringBoxes getCoveringBoxesFor(Address address, long amountToSpend, List<ErgoToken> tokensToSpend);

    /**
     * Deserializes the transaction from the serialized bytes of a ReducedErgoLikeTransaction.
     * Note, the cost is also parsed in addition to the transaction bytes.
     */
    ReducedTransaction parseReducedTransaction(byte[] txBytes);

    /**
     * Deserializes the transaction from the serialized bytes of a ErgoLikeTransaction.
     * Note, the cost is also parsed in addition to the transaction bytes.
     */
    SignedTransaction parseSignedTransaction(byte[] txBytes);
}

