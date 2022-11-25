package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ContextVar;
import org.ergoplatform.appkit.ErgoContract;
import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.OutBoxBuilder;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.appkit.UnsignedTransaction;
import org.ergoplatform.appkit.UnsignedTransactionBuilder;
import org.ergoplatform.appkit.impl.ErgoTreeContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class BabelFeeOperations {
    /**
     * creates a new babel fee box for a given token id and price per token
     *
     * @param boxOperations prepared BoxOperations object defining babel fee box creator and amount
     *                      to spend
     * @param tokenId       tokenId to create the babel fee box for
     * @param pricePerToken the price per raw token value
     * @return prepared transaction to create the new babel fee box
     */
    public static UnsignedTransaction createNewBabelContractTx(
        BoxOperations boxOperations,
        ErgoId tokenId,
        long pricePerToken
    ) {
        BabelFeeBoxState babelFeeBoxState = BabelFeeBoxState.newBuilder()
            .withBoxCreator(boxOperations.getSenders().get(0))
            .withPricePerToken(pricePerToken)
            .withTokenId(tokenId)
            .withValue(boxOperations.getAmountToSpend())
            .build();

        return boxOperations.buildTxWithDefaultInputs(txB -> {
            OutBox outBox = babelFeeBoxState.buildOutbox(txB, null);
            txB.outputs(outBox);
            return txB;
        });
    }

    public static UnsignedTransaction cancelBabelFeeContract(BoxOperations boxOperations, InputBox babelBox) {
        BabelFeeBoxState babelFeeBoxState = new BabelFeeBoxState(babelBox);

        Address creatorAddress = Address.fromSigmaBoolean(babelFeeBoxState.getBoxCreator().getSigmaBoolean(),
            boxOperations.getBlockchainContext().getNetworkType());

        // if we can't pay tx fee from the babel box content, we need to load another box
        List<InputBox> boxesToSpend;
        if (babelFeeBoxState.getValue() < boxOperations.getFeeAmount() + Parameters.MinChangeValue)
            boxesToSpend = boxOperations.loadTop();
        else
            boxesToSpend = new ArrayList<>();

        boxesToSpend.add(0, babelBox);

        UnsignedTransactionBuilder txB = boxOperations.getBlockchainContext().newTxBuilder();

        OutBoxBuilder outBoxBuilder = txB.outBoxBuilder()
            .value(Math.max(Parameters.MinChangeValue, babelFeeBoxState.getValue() - boxOperations.getFeeAmount()))
            .contract(creatorAddress.toErgoContract());

        if (!babelBox.getTokens().isEmpty())
            outBoxBuilder.tokens(babelBox.getTokens().get(0));

        return txB.boxesToSpend(boxesToSpend)
            .fee(boxOperations.getFeeAmount())
            .sendChangeTo(creatorAddress)
            .outputs(outBoxBuilder.build())
            .build();

    }

    /**
     * Tries to fetch a babel fee box for the given tokenId from blockchain data source using the
     * given loader.
     * If maxPagesToLoadForPriceSearch is 0, the babel fee box with the best price satisfying
     * feeAmount is returned. As this can result in infinite loading times, it is recommended to
     * specify a maximum pages to load variable which means that the box with the best price within
     * these pages is returnd.
     * Clients should implement an own logic to retrieve babel fee boxes if needed.
     *
     * @param ctx       current blockchain context
     * @param loader    loader to receive unspent boxes
     * @param tokenId   tokenId offered to swap
     * @param feeAmount nanoErg amount needed to swap
     * @param maxPagesToLoadForPriceSearch number of pages to load to search for best price, or 0 to
     *                                     load all pages
     * @return babel fee box satisfying the needs, or null if none available
     */
    @Nullable
    public static InputBox findBabelFeeBox(
        BlockchainContext ctx,
        BoxOperations.IUnspentBoxesLoader loader,
        ErgoId tokenId,
        long feeAmount,
        int maxPagesToLoadForPriceSearch) {
        ErgoContract contractForToken = new ErgoTreeContract(
            new BabelFeeBoxContract(tokenId).getErgoTree(), ctx.getNetworkType());
        Address address = contractForToken.toAddress();
        loader.prepare(ctx, Collections.singletonList(address), feeAmount, new ArrayList<>());

        int page = 0;
        List<InputBox> inputBoxes = null;

        InputBox returnBox = null;
        long bestPricePerToken = 0;

        while ((page == 0 || !inputBoxes.isEmpty()) &&
               (returnBox == null ||
                maxPagesToLoadForPriceSearch == 0 ||
                page < maxPagesToLoadForPriceSearch)) {
            inputBoxes = loader.loadBoxesPage(ctx, address, page);

            // find the cheapest box satisfying our fee amount needs
            for (InputBox inputBox : inputBoxes) {
                try {
                    BabelFeeBoxState babelFeeBoxState = new BabelFeeBoxState(inputBox);
                    long priceFromBox = babelFeeBoxState.getPricePerToken();
                    if (babelFeeBoxState.getValueAvailableToBuy() >= feeAmount && priceFromBox > bestPricePerToken) {
                        returnBox = inputBox;
                        bestPricePerToken = priceFromBox;
                    }
                } catch (Throwable t) {
                    // ignore, check next
                }
            }

            page++;
        }

        return returnBox;
    }

    /**
     * Adds babel fee boxes (input and output) to the given transaction builder
     *
     * @param txB            transaction builder, can already contain input and output boxes
     * @param babelBox       input babel box to make the swap with
     * @param nanoErgToCover nanoErgs to be covered by babel box, usually the fee amount needed, maybe a change amount as well
     */
    public static void addBabelFeeBoxes(
        UnsignedTransactionBuilder txB,
        InputBox babelBox,
        long nanoErgToCover
    ) {
        BabelFeeBoxState babelBoxState = new BabelFeeBoxState(babelBox);
        BabelFeeBoxState outBabelBoxState = babelBoxState.buildSucceedingState(babelBoxState.calcTokensToSellForErgAmount(nanoErgToCover));
        OutBox outBabelBox = outBabelBoxState.buildOutbox(txB, babelBox);

        txB.addInputs(babelBox.withContextVars(ContextVar.of((byte) 0, ErgoValue.of(txB.getOutputBoxes().size()))));
        txB.addOutputs(outBabelBox);
    }
}
