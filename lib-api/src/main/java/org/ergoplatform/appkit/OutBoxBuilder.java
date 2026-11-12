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
     * The value must be positive: a box carrying zero or negative ERG is
     * rejected by the node while {@code minValuePerByte > 0} (360 by
     * default), since every box must cover at least the minimum value
     * per byte of its serialized size.
     *
     * @param value amount in NanoErg, must be positive
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
     * Creates {@link OutBox} instance using specified parameters.
     *
     * @return output box which can be {@link UnsignedTransactionBuilder#outputs(OutBox...) added}
     * to {@link UnsignedTransaction}
     * @throws IllegalStateException    if the contract is not defined
     * @throws IllegalArgumentException if the box value is not positive, including
     *                                  when {@link #value(long)} was never called and
     *                                  the value is still the zero default
     * @see UnsignedTransaction
     */
    OutBox build();
}
