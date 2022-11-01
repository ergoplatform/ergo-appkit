package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.ErgoId;
import org.ergoplatform.appkit.SigmaProp;

import java.util.Objects;

public class BabelFeeBoxBuilder {
    private long pricePerToken;
    private ErgoId tokenId;
    private SigmaProp boxCreator;
    private long value;
    private long tokenAmount;

    public BabelFeeBoxBuilder withPricePerToken(long pricePerToken) {
        this.pricePerToken = pricePerToken;
        return this;
    }

    public BabelFeeBoxBuilder withTokenId(ErgoId tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    public BabelFeeBoxBuilder withBoxCreator(SigmaProp boxCreator) {
        this.boxCreator = boxCreator;
        return this;
    }

    public BabelFeeBoxBuilder withBoxCreator(Address address) {
        this.boxCreator = SigmaProp.createFromAddress(address);
        return this;
    }

    public BabelFeeBoxBuilder withValue(long value) {
        this.value = value;
        return this;
    }

    public BabelFeeBoxBuilder withTokenAmount(long tokenAmount) {
        if (tokenAmount < 0)
            throw new IllegalArgumentException("pricePerToken must be equal or greater than 0");
        this.tokenAmount = tokenAmount;
        return this;
    }

    public BabelFeeBox build() {
        Objects.requireNonNull(boxCreator, "Box creator not set");
        Objects.requireNonNull(tokenId, "Token ID not set");
        if (value <= 0) {
            throw new IllegalArgumentException("value must be greater than 0");
        }
        if (pricePerToken <= 0) {
            throw new IllegalArgumentException("pricePerToken must be greater than 0");
        }

        return new BabelFeeBox(pricePerToken, tokenId, boxCreator, value, tokenAmount);
    }
}
