package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ErgoId;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.UnsignedTransaction;

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

}
