package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.UnsignedTransaction;
import org.ergoplatform.explorer.client.model.OutputInfo;

/**
 * This class represents AgeUSD bank according to
 * https://github.com/ergoplatform/eips/blob/master/eip-0015.md
 */
public class AgeUsdBank {
    public static final String RATE_BOX_TOKEN_ID = "011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f";
    public static final String BANK_BOX_TOKEN_ID = "7d672d1def471720ca5782fd6473e47e796d9ac0c138d9911346f118b2f6d9d9";

    private final long scOraclePrice; // "rate" in EIP-15
    private final long scCirculating; // circulating stable coin amount
    private final long rcCirculating; // circulating reserve coin amount
    private final long ergReserve; // "bcReserve" in EIP-15 - nanoergs the bank holds

    // EIP 15 constants
    private static final long feePercent = 2;
    private static final long minReserveRatioPercent = 400L; // percent
    private static final long maxReserveRatioPercent = 800L; // percent
    private static final long rcDefaultPrice = 1000000L;

    public AgeUsdBank(OutputInfo bankBox, OutputInfo rateBox) {
        if (!rateBox.getAssets().get(0).getTokenId()
            .equals(RATE_BOX_TOKEN_ID)) {
            throw new IllegalArgumentException("rateBox should own token with id " + RATE_BOX_TOKEN_ID);
        }
        if (!bankBox.getAssets().get(2).getTokenId()
            .equals(BANK_BOX_TOKEN_ID)) {
            throw new IllegalArgumentException("bankBox should own token with id " + BANK_BOX_TOKEN_ID);
        }

        scOraclePrice = (Long) ErgoValue.fromHex(rateBox.getAdditionalRegisters().get("R4")
            .serializedValue).getValue() / 100;
        scCirculating = (Long) ErgoValue.fromHex(bankBox.getAdditionalRegisters().get("R4")
            .serializedValue).getValue();
        rcCirculating = (Long) ErgoValue.fromHex(bankBox.getAdditionalRegisters().get("R5")
            .serializedValue).getValue();

        ergReserve = bankBox.getValue();
    }

    public AgeUsdBank(long scOraclePrice, long scCirculating, long rcCirculating, long ergReserve) {
        this.scOraclePrice = scOraclePrice / 100;
        this.scCirculating = scCirculating;
        this.rcCirculating = rcCirculating;
        this.ergReserve = ergReserve;
    }

    /**
     * @return how many ERG bank owes sc holders
     */
    public long getErgLiabilities() {
        long ergReserveNeeded = scCirculating * scOraclePrice;
        return Math.max(Math.min(ergReserve, ergReserveNeeded), 0);
    }

    public long getCurrentReserveRatio() {
        return getReserveRatio(0);
    }

    public long getReserveRatio(long scCirculatingDelta) {
        long ergReserveNeeded = (scCirculating + scCirculatingDelta) * scOraclePrice;
        return (ergReserveNeeded == 0) ? maxReserveRatioPercent :
            (ergReserve * 100) / ergReserveNeeded;
    }

    public boolean canExchangeStableCoin(long scCirculatingDelta) {
        return scCirculatingDelta <= 0 || getReserveRatio(scCirculatingDelta) >= minReserveRatioPercent;
    }

    public boolean canExchangeReserveCoin(long rcCirculatingDelta) {
        return rcCirculatingDelta > 0 && getReserveRatio(rcCirculatingDelta) <= maxReserveRatioPercent
            || rcCirculatingDelta <= 0 && getReserveRatio(rcCirculatingDelta) >= minReserveRatioPercent;
    }

    /**
     * @return stable coin price in nanoergs. Note that stable coin price relates to raw stable
     * coin token amount
     */
    public long getStableCoinPrice() {
        return Math.min(scOraclePrice, scCirculating == 0 ?
            Long.MAX_VALUE : getErgLiabilities() / scCirculating);
    }

    public long getReserveCoinPrice() {
        long equity = ergReserve - getErgLiabilities();
        return rcCirculating == 0 ? rcDefaultPrice : equity / rcCirculating;
    }

    public long getBankFee(long ergDeltaAmount) {
        return Math.abs(ergDeltaAmount * feePercent) / 100;
    }

    public UnsignedTransaction buildStableCoinExchangeTransaction(BoxOperations operations, long scDelta) {
        return buildExchangeTransaction(operations, scDelta, 0);
    }

    public UnsignedTransaction buildReserveCoinExchangeTransaction(BoxOperations operations, long rcDelta) {
        return buildExchangeTransaction(operations, 0, rcDelta);
    }

    private UnsignedTransaction buildExchangeTransaction(BoxOperations operations, long scDelta, long rcDelta) {
        // TODO tx: Tokens order must be the same. Bank box out is 0, receipt out is 1.
        // Receipt R4: bank sc/rc amount change (negative means withdrawn from bank)
        // Receipt R5: bank nanonerg amount change (negative means withdrawn from bank)
        // Bank box out R4: scCirculating, R5: rc circulating
        throw new UnsupportedOperationException("Not implemented");
    }
}
