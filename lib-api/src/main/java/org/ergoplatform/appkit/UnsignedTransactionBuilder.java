package org.ergoplatform.appkit;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.sdk.ErgoToken;

import java.util.List;

/**
 * This interface is used to build a new {@link UnsignedTransaction} which can later
 * be signed by a {@link ErgoProver prover}. A new instance of this builder can be
 * {@link BlockchainContext#newTxBuilder() obtained} from the {@link BlockchainContext}.
 * Before unsigned transaction can be {@link BlockchainContext#sendTransaction(SignedTransaction) sent} to the
 * blockchain it should be signed by a prover.
 * The proved should be {@link ErgoProverBuilder#build() constructed} by the {@link ErgoProverBuilder builder}
 * {@link BlockchainContext#newProverBuilder() obtained} from the same {@link BlockchainContext context}.
 *
 * @see UnsignedTransaction
 */
public interface UnsignedTransactionBuilder {
    /**
     * Specifies {@link PreHeader} instance to be used for transaction signing.
     */
    UnsignedTransactionBuilder preHeader(PreHeader ph);

    /**
     * @see #addInputs(List)
     */
    UnsignedTransactionBuilder addInputs(InputBox... boxes);

    /**
     * Adds input boxes to an already specified list of inputs or, if no input boxes are defined yet,
     * as the boxes to spend. The order is preserved.
     * The boxes that will be spent by the transaction when it will be included in a block.
     *
     * @param boxes list of boxes to be spent by the transaction. The boxes can either be
     *              {@link BlockchainContext#getBoxesById(String...) obtained} from context of created from
     *               scratch
     *              as {@link OutBox} and then {@link OutBox#convertToInputWith(String, short) converted} to
     *              {@link InputBox}.
     */
    UnsignedTransactionBuilder addInputs(List<InputBox> boxes);

    /**
     * @deprecated Use {@link #addInputs(List)} instead.
     */
    @Deprecated
    UnsignedTransactionBuilder boxesToSpend(List<InputBox> boxes);

    /**
     * @see #addDataInputs(List)
     */
    UnsignedTransactionBuilder addDataInputs(InputBox... boxes);

    /**
     * @deprecated Use {@link #addDataInputs(List)} instead.
     */
    @Deprecated
    UnsignedTransactionBuilder withDataInputs(List<InputBox> boxes);

    /**
     * Adds input boxes to an already specified list of data inputs or, if no data input boxes
     * are defined yet, set the boxes as the data input boxes to be used. The order is preserved.
     *
     * @param boxes list of boxes to be used as data-inputs by the transaction. The boxes can either be
     *              {@link BlockchainContext#getBoxesById(String...) obtained} from context of created from
     *               scratch
     *              as {@link OutBox} and then {@link OutBox#convertToInputWith(String, short) converted} to
     *              {@link InputBox}.
     */
    UnsignedTransactionBuilder addDataInputs(List<InputBox> boxes);

    /**
     * @deprecated use {@link #addOutputs(OutBox...)}
     */
    @Deprecated
    UnsignedTransactionBuilder outputs(OutBox... outputs);

    /**
     * @see #addOutputs(List)
     */
    UnsignedTransactionBuilder addOutputs(OutBox... outBoxes);

    /**
     * Adds output boxes to an already specified list of outputs or, if no output boxes are defined yet,
     * as the boxes to be output. The order is preserved.
     * After this transaction is {@link UnsignedTransactionBuilder#build() built},
     * {@link ErgoProver#sign(UnsignedTransaction)} signed,
     * {@link BlockchainContext#sendTransaction(SignedTransaction) sent} to the node and included
     * into a next block the output boxes will be put in the UTXO set.
     *
     * @param outBoxes output boxes created by the transaction
     */
    UnsignedTransactionBuilder addOutputs(List<OutBox> outBoxes);

    /**
     * Adds transaction fee output.
     *
     * @param feeAmount transaction fee amount in NanoErgs
     */
    UnsignedTransactionBuilder fee(long feeAmount);

    /**
     * @see #tokensToBurn(List)
     */
    UnsignedTransactionBuilder tokensToBurn(ErgoToken... tokens);

    /**
     * Configures amounts for tokens to be burnt.
     * Each Ergo box can store zero or more tokens (aka assets).
     * In contrast to strict requirement on ERG balance between transaction inputs and outputs,
     * the amounts of output tokens can be less than the amounts of input tokens.
     * This is interpreted as token burning i.e. reducing the total amount of tokens in
     * circulation in the blockchain.
     * Note, once issued/burnt, the amount of tokens in circulation cannot be increased.
     *
     * @param tokens one or more tokens to be burnt as part of the transaction.
     * @see ErgoToken
     */
    UnsignedTransactionBuilder tokensToBurn(List<ErgoToken> tokens);

    /**
     * Adds change output to the specified address if needed.
     *
     * @param address address to send output
     * @deprecated use {@link #sendChangeTo(Address)}
     */
    @Deprecated
    UnsignedTransactionBuilder sendChangeTo(ErgoAddress address);

    /**
     * Adds change output to the specified address if needed.
     *
     * @param address address to send output
     */
    UnsignedTransactionBuilder sendChangeTo(Address address);

    /**
     * Builds a new unsigned transaction in the {@link BlockchainContext context} inherited from this builder.
     *
     * @return a new instance of {@link UnsignedTransaction}
     */
    UnsignedTransaction build();

    /**
     * Returns the context for which this builder is building transactions.
     */
    BlockchainContext getCtx();

    /** Returns current (either default of configured) pre-header. */
    PreHeader getPreHeader();

    /**
     * Returns the network type of the blockchain represented by the
     *  {@link UnsignedTransactionBuilder#getCtx()
     * context} of this builder.
     */
    NetworkType getNetworkType();

    /**
     * Creates a new builder of output box. The returned builder can be used to
     * {@link OutBoxBuilder#build() build} only single instance of {@link OutBox}.
     * For each new {@link OutBox} a new {@link OutBoxBuilder} should be created.
     */
    OutBoxBuilder outBoxBuilder();

    /**
     * Returns all input boxes attached to this builder.
     */
    List<InputBox> getInputBoxes();

    /**
     * Returns all output boxes attached to this builder.
     */
    List<OutBox> getOutputBoxes();
}
