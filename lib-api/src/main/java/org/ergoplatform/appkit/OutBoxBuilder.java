package org.ergoplatform.appkit;

import sigma.data.SigmaConstants;
import org.ergoplatform.sdk.ErgoToken;

/**
 * This interface is used to build a new output box, which can be included
 * in the new unsigned transaction. When transaction is signed, sent to the
 * blockchain and then included by miners in a new block, then the output
 * constructed using this builder will be added to UTXO set.
 *
 * @see UnsignedTransactionBuilder#outBoxBuilder()
 */
public interface OutBoxBuilder {
    /**
     * Configure the Erg amount of the output box.
     * Each transaction is required to
     *
     * @param value amount in NanoErg
     */
    OutBoxBuilder value(long value);

    /**
     * Configure the guarding contract of the output box. This contract will be compiled into
     * ErgoTree, serialized and then sent to the blockchain as part of the signed transaction.
     *
     * @param contract ergo contract (aka `proposition`) protecting this box from illegal spending.
     */
    OutBoxBuilder contract(ErgoContract contract);

    /**
     * Configures amounts for one or more tokens (up to {@link SigmaConstants.MaxTokens}).
     * Each Ergo box can store zero or more tokens (aka assets).
     *
     * @param tokens zero or more tokens to be added to the constructed output box.
     *               An empty call is allowed and adds no tokens.
     * @see ErgoToken
     */
    OutBoxBuilder tokens(ErgoToken... tokens);

    /**
     * Mints new token according to https://github.com/ergoplatform/eips/blob/master/eip-0004.md
     *
     * @param token token to mint
     * @see Eip4Token and Eip4TokenBuilder
     */
    OutBoxBuilder mintToken(Eip4Token token);

    /**
     * Configures one or more optional registers of the output box.
     * Each box have 4 mandatory registers holding value of NanoErgs, guarding script,
     * tokens, creation info.
     * Optional (aka non-mandatory) registers numbered from index 4 up to 9.
     *
     * @param registers array of optional register values,
     *                  where registers[0] corresponds to R4, registers[1] - R5, etc.
     * @see ErgoValue
     * @see org.ergoplatform.ErgoBox.NonMandatoryRegisterId
     */
    OutBoxBuilder registers(ErgoValue<?>... registers);

    /**
     * Configure the height when the transaction containing the box was created.
     * This height, when explicitly specified, should not exceed height of the block,
     * containing the transaction with this output box.
     * By default, when this method is not used, the creationHeight is taken from the
     * current {@link BlockchainContext} instance used by this builder.
     * @param height specified block height
     */
    OutBoxBuilder creationHeight(int height);

    /**
     * Computes a safe minimum box value (in NanoErgs) for the output box currently
     * configured on this builder, using the {@code minValuePerByte} parameter from the
     * {@link BlockchainContext} associated with this builder.
     *
     * <p>The calculation accounts for the full serialized box size as measured by the
     * Ergo node (candidate body including the VLQ-encoded value itself, 32-byte
     * transaction id, and worst-case VLQ output index), so an output built with
     * {@code .value(builder.getMinBoxValue())} is guaranteed to satisfy the minimum box
     * value requirement at any output position in the transaction.
     *
     * @return minimum box value in NanoErgs
     * @throws IllegalStateException if the guarding contract has not been configured yet
     */
    long getMinBoxValue();

    /**
     * Computes a safe minimum box value (in NanoErgs) for the output box currently
     * configured on this builder using the given {@code minValuePerByte} rate.
     *
     * @param minValuePerByte minimum nanoERGs per serialized byte of the box
     * @return minimum box value in NanoErgs
     * @throws IllegalStateException if the guarding contract has not been configured yet
     * @throws IllegalArgumentException if {@code minValuePerByte} is negative
     */
    long getMinBoxValue(int minValuePerByte);

    /**
     * Creates {@link OutBox} instance using specified parameters.
     *
     * @return output box which can be {@link UnsignedTransactionBuilder#outputs(OutBox...) added}
     * to {@link UnsignedTransaction}
     * @see UnsignedTransaction
     */
    OutBox build();
}
