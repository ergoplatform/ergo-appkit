package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.explorer.client.model.OutputInfo;

/**
 * This class represents AgeUSD bank according to
 * https://github.com/ergoplatform/eips/blob/master/eip-0015.md
 */
public class AgeUsdBank {
    /**
     * Token stored at the current Oracle Box
     */
    public static final String RATE_BOX_TOKEN_ID = "011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f";
    /**
     * Token stored at the current bank box
     */
    public static final String BANK_BOX_TOKEN_ID = "7d672d1def471720ca5782fd6473e47e796d9ac0c138d9911346f118b2f6d9d9";
    /**
     * ID of stable coin token, with decimals 2
     */
    public static final String SC_TOKEN_ID = "03faf2cb329f2e90d6d23b58d91bbb6c046aa143261cc21f52fbe2824bfcbf04";
    /**
     * ID of reserve coin token
     */
    public static final String RC_TOKEN_ID = "003bd19d0187117f130b62e1bcab0939929ff5c7709f843c5c4dd158949285d0";

    private final long scOraclePrice; // "rate" in EIP-15
    private final long scCirculating; // circulating stable coin amount
    private final long rcCirculating; // circulating reserve coin amount
    private final long ergReserve; // "bcReserve" in EIP-15 - nanoergs the bank holds

    // EIP 15 constants
    public static final long FEE_PERCENT = 2;
    public static final long MIN_RESERVE_RATIO_PERCENT = 400L; // percent
    public static final long MAX_RESERVE_RATIO_PERCENT = 800L; // percent
    public static final long RC_DEFAULT_PRICE = 1000000L;

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

    /**
     * @return current reserve ratio in bank box
     */
    public long getCurrentReserveRatio() {
        return getReserveRatio(0);
    }

    /**
     * @return reserve ratio bank box has when the given stable coin exchange takes place
     */
    public long getReserveRatio(long scCirculatingDelta) {
        long ergReserveNeeded = (scCirculating + scCirculatingDelta) * scOraclePrice;
        return (ergReserveNeeded == 0) ? MAX_RESERVE_RATIO_PERCENT :
            (ergReserve * 100) / ergReserveNeeded;
    }

    /**
     * @return circulating stable coin amount
     */
    public long getCirculatingStableCoins() {
        return scCirculating;
    }

    /**
     * @return circulating reserve coin amount
     */
    public long getCirculatingReserveCoins() {
        return rcCirculating;
    }

    /**
     * @return whether the given stable coin exchange is valid
     */
    public boolean canExchangeStableCoin(long scCirculatingDelta) {
        return scCirculatingDelta <= 0 || getReserveRatio(scCirculatingDelta) >= MIN_RESERVE_RATIO_PERCENT;
    }

    /**
     * @return whether the given reserve coin exchange is valid
     */
    public boolean canExchangeReserveCoin(long rcCirculatingDelta) {
        return rcCirculatingDelta > 0 && getReserveRatio(rcCirculatingDelta) <= MAX_RESERVE_RATIO_PERCENT
            || rcCirculatingDelta <= 0 && getReserveRatio(rcCirculatingDelta) >= MIN_RESERVE_RATIO_PERCENT;
    }

    /**
     * @return stable coin price in nanoergs. Note that stable coin price relates to raw stable
     * coin token amount
     */
    public long getStableCoinPrice() {
        return Math.min(scOraclePrice, scCirculating == 0 ?
            Long.MAX_VALUE : getErgLiabilities() / scCirculating);
    }

    /**
     * @return reserve coin price in nanoergs.
     */
    public long getReserveCoinPrice() {
        long equity = ergReserve - getErgLiabilities();
        return rcCirculating == 0 ? RC_DEFAULT_PRICE : equity / rcCirculating;
    }

    public long getStableCoinExchangeErgAmount(long scDelta) {
        return getStableCoinPrice() * scDelta;
    }

    public long getReserveCoinExchangeErgAmount(long rcDelta) {
        return getReserveCoinPrice() * rcDelta;
    }

    /**
     * @return bank fee for a given erg delta amount
     */
    public long getBankFee(long exchangeErgAmount) {
        return Math.abs(exchangeErgAmount * FEE_PERCENT) / 100;
    }

    /**
     * @param operations BoxOperations class prepared with senders, fee and desired loader
     * @return builder to generate an exchange transaction with the bank
     */
    public AgeUsdExchangeTransactionBuilder getExchangeTransactionBuilder(BoxOperations operations) {
        if (operations.getBlockchainContext().getNetworkType() != NetworkType.MAINNET) {
            throw new IllegalArgumentException("Use mainnnet: AgeUSD transactions only defined on mainnet.");
        }
        return new AgeUsdExchangeTransactionBuilder(this, operations);
    }
}
