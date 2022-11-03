package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.ErgoId;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.OutBoxBuilder;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.appkit.SigmaProp;
import org.ergoplatform.appkit.TransactionBox;
import org.ergoplatform.appkit.UnsignedTransactionBuilder;
import org.ergoplatform.appkit.impl.ErgoTreeContract;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Represents a Babel Fee Box state, see EIP-0031
 * https://github.com/ergoplatform/eips/blob/master/eip-0031.md
 * <p>
 * The term “babel fees“ refers to the concept of paying transaction fees in tokens instead of
 * platform’s primary token (ERG). It is a contract that buys tokens and pays ERG, suitable to be
 * used in any transaction.
 */
public class BabelFeeBoxState {

    private final long pricePerToken;
    private final ErgoId tokenId;
    private final SigmaProp boxCreator;
    private final long value;
    private final long tokenAmount;

    public BabelFeeBoxState(TransactionBox ergoBox) {
        value = ergoBox.getValue();
        List<ErgoValue<?>> registers = ergoBox.getRegisters();
        boxCreator = new SigmaProp((special.sigma.SigmaProp) registers.get(0).getValue());
        pricePerToken = (Long) registers.get(1).getValue();
        this.tokenId = new BabelFeeBoxContract(ergoBox.getErgoTree()).getTokenId();

        if (!ergoBox.getTokens().isEmpty()) {
            ErgoToken ergoToken = ergoBox.getTokens().get(0);
            tokenAmount = ergoToken.getValue();

            if (!ergoToken.getId().equals(tokenId)) {
                throw new IllegalStateException("Token id of contract and token id in babel box should be equial.");
            }
        } else {
            tokenAmount = 0;
        }
    }

    BabelFeeBoxState(long pricePerToken, ErgoId tokenId, SigmaProp boxCreator, long value, long tokenAmount) {
        this.pricePerToken = pricePerToken;
        this.tokenId = tokenId;
        this.boxCreator = boxCreator;
        this.value = value;
        this.tokenAmount = tokenAmount;
    }

    /**
     * @return nanoErg amount offered per raw token amount
     */
    public long getPricePerToken() {
        return pricePerToken;
    }

    /**
     * @return token id this babel fee box is offering change for
     */
    public ErgoId getTokenId() {
        return tokenId;
    }

    /**
     * @return box creator or owner of the babel fee box
     */
    public SigmaProp getBoxCreator() {
        return boxCreator;
    }

    /**
     * @return overall nanoErg value in the box. Not all is available to change for tokens as a
     * small amount must remain in the successor box
     */
    public long getValue() {
        return value;
    }

    /**
     * @return raw amount of tokens already collected in the box
     */
    public long getTokenAmount() {
        return tokenAmount;
    }

    /**
     * @return overall ERG value available to change for token
     */
    public long getValueAvailableToBuy() {
        return value - Parameters.MinChangeValue;
    }

    /**
     * @return max token raw amount possible to swap at best price
     */
    public long getMaxTokenAmountToBuy() {
        return getValueAvailableToBuy() / pricePerToken;
    }

    /**
     * @param nanoErgs amount we want to receive, usually to pay transaction fees
     * @return the amount of tokens to sell to receive at least this amount of nanoErgs
     */
    public long calcTokensToSellForErgAmount(long nanoErgs) {
        long floorAmount = nanoErgs / pricePerToken;
        return (floorAmount * pricePerToken >= nanoErgs) ? floorAmount : floorAmount + 1;
    }

    /**
     * constructs a new babel fee box state based on this fee box with a certain token amount change
     * done
     *
     * @param tokenAmountChange the token amount to add to the new babel fee box
     * @return new babel fee box after swap was done
     */
    public BabelFeeBoxState buildSucceedingState(long tokenAmountChange) {
        if (tokenAmountChange <= 0)
            throw new IllegalArgumentException("tokenAmountChange must be greater than 0");
        if (tokenAmountChange > getMaxTokenAmountToBuy())
            throw new IllegalArgumentException("tokenAmountChange must be less or equal maxAmountToBuy");

        return new BabelFeeBoxState(pricePerToken, tokenId, boxCreator,
            value - tokenAmountChange * pricePerToken,
            tokenAmountChange + tokenAmount);
    }

    /**
     * @param txBuilder         txBuilder to build the new outbox with
     * @param precedingBabelBox if this is not the initial babel fee box, preceeding babel fee box
     *                          must be given to set registers correct
     * @return outbox representing this babel fee box
     */
    public OutBox buildOutbox(UnsignedTransactionBuilder txBuilder, @Nullable InputBox precedingBabelBox) {
        OutBoxBuilder outBoxBuilder = txBuilder.outBoxBuilder()
            .contract(new ErgoTreeContract(new BabelFeeBoxContract(tokenId).getErgoTree(), txBuilder.getNetworkType()))
            .value(value)
            .registers(ErgoValue.of(boxCreator), ErgoValue.of(pricePerToken),
                ErgoValue.of(precedingBabelBox != null ? precedingBabelBox.getId().getBytes() : new byte[0]));

        if (tokenAmount > 0)
            outBoxBuilder.tokens(new ErgoToken(tokenId, tokenAmount));

        return outBoxBuilder.build();
    }

    public static BabelFeeBoxStateBuilder newBuilder() {
        return new BabelFeeBoxStateBuilder();
    }
}
