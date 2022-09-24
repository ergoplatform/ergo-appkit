package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.BoxSelectorsJavaHelpers;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.OutBoxBuilder;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.appkit.UnsignedTransaction;
import org.ergoplatform.appkit.UnsignedTransactionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Builds an {@link UnsignedTransaction} for an AgeUSD exchange.
 * <p>
 * See {@link #buildReserveCoinExchangeTransaction(long)} and {@link #buildStableCoinExchangeTransaction(long)}
 */
public class AgeUsdExchangeTransactionBuilder {

    private final AgeUsdBank bank;
    private final BoxOperations boxOperations;
    private final BlockchainContext ctx;
    private long additionalFeeAmount;
    @Nullable
    private Address additionalFeeAddress;
    @Nullable
    private String rateBoxId;
    @Nullable
    private String bankBoxId;

    protected AgeUsdExchangeTransactionBuilder(AgeUsdBank bank, BoxOperations operations) {
        this.bank = bank;
        boxOperations = operations;
        ctx = operations.getBlockchainContext();
    }

    /**
     * Adds an additional UI fee to the generated transaction
     *
     * @param additionalFeeAddress additional address for UI fee, or null if not needed
     * @param additionalFeeAmount  additional UI fee amount or 0 if not needed
     */
    public AgeUsdExchangeTransactionBuilder withAdditionalFee(@Nullable Address additionalFeeAddress,
                                                              long additionalFeeAmount) {
        this.additionalFeeAddress = additionalFeeAddress;
        this.additionalFeeAmount = additionalFeeAddress != null ? Math.max(0, additionalFeeAmount) : 0;
        return this;
    }

    /**
     * @param rateBoxId box id to use as the oracle data input for the transaction, or
     *                  null to let the builder search for the oracle box (default)
     */
    public AgeUsdExchangeTransactionBuilder withRateBoxId(@Nullable String rateBoxId) {
        this.rateBoxId = rateBoxId;
        return this;
    }

    /**
     * @param bankBoxId box id to use as the bank box input for the transaction, or
     *                  null to let the builder search for the bank box (default)
     */
    public AgeUsdExchangeTransactionBuilder withBankBoxId(@Nullable String bankBoxId) {
        this.bankBoxId = bankBoxId;
        return this;
    }

    /**
     * @param scCirculatingDelta Desired Stable coin exchange amount with positive amount meaning withdrawal
     *                           from bank
     */
    public UnsignedTransaction buildStableCoinExchangeTransaction(long scCirculatingDelta) {
        return buildExchangeTransaction(scCirculatingDelta * -1, 0);
    }

    /**
     * see {@link #buildStableCoinExchangeTransaction(long)}
     */
    public UnsignedTransaction buildReserveCoinExchangeTransaction(long rcCirculatingDelta) {
        return buildExchangeTransaction(0, rcCirculatingDelta * -1);
    }

    private UnsignedTransaction buildExchangeTransaction(long scDelta, long rcDelta) {
        if (scDelta != 0 && rcDelta != 0 || scDelta != 0 && !bank.canExchangeStableCoin(scDelta * -1)
            || rcDelta != 0 && !bank.canExchangeReserveCoin(rcDelta * -1)) {
            throw new IllegalArgumentException("Cannot execute desired exchange due to reserve ratio.");
        }

        long exchangeErgAmount = -(bank.getReserveCoinExchangeErgAmount(rcDelta) + bank.getStableCoinExchangeErgAmount(scDelta));
        long bankFeeAmount = bank.getBankFee(exchangeErgAmount);

        // > 0: customer will pay, < 0: customer will receive
        long totalAmount = exchangeErgAmount + bankFeeAmount + additionalFeeAmount + Parameters.MinChangeValue;
        long receiptBoxAmount = Math.max(-totalAmount, Parameters.MinChangeValue);

        // we load the boxes to spent for the customer
        List<InputBox> customerBoxesToSpend = loadBoxesToSpendByCustomer(scDelta, rcDelta, totalAmount);

        // we load the bank's current box. Bank always have only one box
        InputBox bankBox = bankBoxId != null ? ctx.getDataSource().getBoxByIdWithMemPool(bankBoxId) :
            ctx.getDataSource().getUnspentBoxesFor(
                new ErgoToken(AgeUsdBank.BANK_BOX_TOKEN_ID, 0L), 0, 1).get(0);

        // bank box is first to spend, all customer input boxes follow
        List<InputBox> inputBoxes = new ArrayList<>(customerBoxesToSpend.size() + 1);
        inputBoxes.add(bankBox);
        inputBoxes.addAll(customerBoxesToSpend);

        // oracle's rate box is needed as data input
        List<InputBox> rateBox = rateBoxId != null ?
            Collections.singletonList(ctx.getDataSource().getBoxByIdWithMemPool(rateBoxId)) :
            ctx.getDataSource().getUnspentBoxesFor(
                new ErgoToken(AgeUsdBank.RATE_BOX_TOKEN_ID, 0L), 0, 1);

        UnsignedTransactionBuilder txBuilder = ctx.newTxBuilder();

        List<OutBox> outboxes = new ArrayList<>();

        // construct the new bank box
        OutBox bankBoxOut = txBuilder.outBoxBuilder()
            .value(bankBox.getValue() + exchangeErgAmount + bankFeeAmount)
            .contract(new ErgoTreeContract(bankBox.getErgoTree(), NetworkType.MAINNET))
            // preserve tokens
            .tokens(new ErgoToken(AgeUsdBank.SC_TOKEN_ID, bankBox.getTokens().get(0).getValue() + scDelta),
                new ErgoToken(AgeUsdBank.RC_TOKEN_ID, bankBox.getTokens().get(1).getValue() + rcDelta),
                new ErgoToken(AgeUsdBank.BANK_BOX_TOKEN_ID, bankBox.getTokens().get(2).getValue())
            )
            // Bank box out R4: scCirculating, R5: rc circulating
            .registers(ErgoValue.of(bank.getCirculatingStableCoins() - scDelta),
                ErgoValue.of(bank.getCirculatingReserveCoins() - rcDelta))
            .build();
        outboxes.add(bankBoxOut);

        // construct the receipt box
        OutBoxBuilder receiptBoxBuilder = txBuilder.outBoxBuilder()
            .value(receiptBoxAmount)
            .contract(boxOperations.getSenders().get(0).toErgoContract())
            // Receipt R4: bank sc/rc amount change (positive means withdrawn from bank)
            // Receipt R5: bank nanonerg amount change (negative means withdrawn from bank)
            .registers(ErgoValue.of(scDelta != 0 ? -scDelta : -rcDelta),
                ErgoValue.of(exchangeErgAmount + bankFeeAmount));

        if (scDelta < 0) {
            receiptBoxBuilder.tokens(new ErgoToken(AgeUsdBank.SC_TOKEN_ID, -scDelta));
        } else if (rcDelta < 0) {
            receiptBoxBuilder.tokens(new ErgoToken(AgeUsdBank.RC_TOKEN_ID, -rcDelta));
        }

        outboxes.add(receiptBoxBuilder.build());

        if (additionalFeeAddress != null && additionalFeeAmount > 0) {
            OutBox uiFeeBox = txBuilder.outBoxBuilder()
                .value(additionalFeeAmount)
                .contract(additionalFeeAddress.toErgoContract())
                .build();
            outboxes.add(uiFeeBox);
        }

        // Box Selector has to preselect so we don't get exceptions later
        long outputTotal = boxOperations.getFeeAmount();
        for (OutBox outbox : outboxes) {
            outputTotal += outbox.getValue();
        }
        // Box selectors needs tokens as well.
        List<ErgoToken> tokensOut = new ArrayList<>();
        tokensOut.add(new ErgoToken(AgeUsdBank.SC_TOKEN_ID, bankBox.getTokens().get(0).getValue() + Math.max(0, scDelta)));
        tokensOut.add(new ErgoToken(AgeUsdBank.RC_TOKEN_ID, bankBox.getTokens().get(1).getValue() + Math.max(0, rcDelta)));
        tokensOut.add(new ErgoToken(AgeUsdBank.BANK_BOX_TOKEN_ID, bankBox.getTokens().get(2).getValue()));

        List<InputBox> selectedBoxes = BoxSelectorsJavaHelpers.selectBoxes(inputBoxes, outputTotal, tokensOut);

        return txBuilder.boxesToSpend(selectedBoxes)
            .outputs(outboxes.toArray(new OutBox[]{}))
            .fee(boxOperations.getFeeAmount())
            .sendChangeTo(boxOperations.getSenders().get(0).getErgoAddress())
            .withDataInputs(rateBox).build();
    }

    private List<InputBox> loadBoxesToSpendByCustomer(long scDelta, long rcDelta, long totalAmount) {
        boxOperations.withAmountToSpend(Math.max(0, totalAmount));

        if (scDelta > 0) {
            boxOperations.withTokensToSpend(Collections.singletonList(
                new ErgoToken(AgeUsdBank.SC_TOKEN_ID, scDelta)));
        }
        if (rcDelta > 0) {
            boxOperations.withTokensToSpend(Collections.singletonList(
                new ErgoToken(AgeUsdBank.RC_TOKEN_ID, rcDelta)));
        }

        return boxOperations.loadTop();
    }
}
