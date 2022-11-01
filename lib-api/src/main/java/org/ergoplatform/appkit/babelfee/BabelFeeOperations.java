package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ErgoId;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.OutBoxBuilder;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.appkit.UnsignedTransaction;
import org.ergoplatform.appkit.UnsignedTransactionBuilder;

import java.util.ArrayList;
import java.util.List;

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
        BabelFeeBox babelFeeBox = BabelFeeBox.newBuilder().withBoxCreator(boxOperations.getSenders().get(0))
            .withPricePerToken(pricePerToken)
            .withTokenId(tokenId)
            .withValue(boxOperations.getAmountToSpend())
            .build();

        return boxOperations.buildTxWithDefaultInputs(txB -> {
            OutBox outBox = babelFeeBox.buildOutbox(txB);
            txB.outputs(outBox);
            return txB;
        });
    }

    public static UnsignedTransaction cancelBabelFeeContract(BoxOperations boxOperations, InputBox babelBox) {
        BabelFeeBox babelFeeBox = new BabelFeeBox(babelBox);

        Address creatorAddress = Address.fromSigmaBoolean(babelFeeBox.getBoxCreator().getSigmaBoolean(),
            boxOperations.getBlockchainContext().getNetworkType());

        // if we can't pay tx fee from the babel box content, we need to load another box
        List<InputBox> boxesToSpend;
        if (babelFeeBox.getValue() < boxOperations.getFeeAmount() + Parameters.MinChangeValue)
            boxesToSpend = boxOperations.loadTop();
        else
            boxesToSpend = new ArrayList<>();

        boxesToSpend.add(0, babelBox);

        UnsignedTransactionBuilder txB = boxOperations.getBlockchainContext().newTxBuilder();

        OutBoxBuilder outBoxBuilder = txB.outBoxBuilder()
            .value(Math.max(Parameters.MinChangeValue, babelFeeBox.getValue() - boxOperations.getFeeAmount()))
            .contract(creatorAddress.toErgoContract());

        if (!babelBox.getTokens().isEmpty())
            outBoxBuilder.tokens(babelBox.getTokens().get(0));

        return txB.boxesToSpend(boxesToSpend)
            .fee(boxOperations.getFeeAmount())
            .sendChangeTo(creatorAddress)
            .outputs(outBoxBuilder.build())
            .build();

    }
}
