package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.SigmaProp;
import org.ergoplatform.sdk.ErgoId;

import java.util.Objects;

/**
 * Builder class to conveniently instantiate a {@link BabelFeeBoxState} with self-defined
 * information.
 */
public class BabelFeeBoxStateBuilder {
    private long pricePerToken;
    private ErgoId tokenId;
    private SigmaProp boxCreator;
    private long value;
    private long tokenAmount;

    /**
     * see {@link BabelFeeBoxState#getPricePerToken()}
     */
    public BabelFeeBoxStateBuilder withPricePerToken(long pricePerToken) {
        this.pricePerToken = pricePerToken;
        return this;
    }

    /**
     * see {@link BabelFeeBoxState#getTokenId()}
     */
    public BabelFeeBoxStateBuilder withTokenId(ErgoId tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    /**
     * see {@link BabelFeeBoxState#getBoxCreator()}
     */
    public BabelFeeBoxStateBuilder withBoxCreator(SigmaProp boxCreator) {
        this.boxCreator = boxCreator;
        return this;
    }

    /**
     * see {@link BabelFeeBoxState#getBoxCreator()}
     */
    public BabelFeeBoxStateBuilder withBoxCreator(Address address) {
        this.boxCreator = SigmaProp.createFromAddress(address);
        return this;
    }

    /**
     * see {@link BabelFeeBoxState#getValue()}
     */
    public BabelFeeBoxStateBuilder withValue(long nanoErgValue) {
        this.value = nanoErgValue;
        return this;
    }

    /**
     * see {@link BabelFeeBoxState#getTokenAmount()}
     */
    public BabelFeeBoxStateBuilder withTokenAmount(long tokenAmount) {
        if (tokenAmount < 0)
            throw new IllegalArgumentException("pricePerToken must be equal or greater than 0");
        this.tokenAmount = tokenAmount;
        return this;
    }

    /**
     * @return Babel fee box state built with the given data
     */
    public BabelFeeBoxState build() {
        Objects.requireNonNull(boxCreator, "Box creator not set");
        Objects.requireNonNull(tokenId, "Token ID not set");
        if (value <= 0) {
            throw new IllegalArgumentException("value must be greater than 0");
        }
        if (pricePerToken <= 0) {
            throw new IllegalArgumentException("pricePerToken must be greater than 0");
        }

        return new BabelFeeBoxState(pricePerToken, tokenId, boxCreator, value, tokenAmount);
    }
}
