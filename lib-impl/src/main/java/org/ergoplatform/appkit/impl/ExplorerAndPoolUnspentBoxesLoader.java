package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionInput;
import org.ergoplatform.restapi.client.Transactions;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link BoxOperations.IUnspentBoxesLoader} implementation to be used with
 * {@link BoxOperations#withInputBoxesLoader(BoxOperations.IUnspentBoxesLoader)}
 * <p>
 * Instead of the default implementation, this one fetches the mempool from the node connected to
 * and blacklists the inputs so they aren't used for transactions made by {@link BoxOperations}
 * methods.
 * <p>
 * Note that the mempool is fetched on object instantiation, so don't pass references around as
 * the data will be outdated after some time.
 */
public class ExplorerAndPoolUnspentBoxesLoader extends BoxOperations.ExplorerApiWithCheckerLoader {
    private final List<String> unconfirmedSpentBoxesIds;

    public ExplorerAndPoolUnspentBoxesLoader(BlockchainContextImpl ctx) {
        unconfirmedSpentBoxesIds = new ArrayList<>();

        Transactions unconfirmedTransactions = ErgoNodeFacade.getUnconfirmedTransactions(ctx.getRetrofit(), 1000, 0);
        for (ErgoTransaction unconfirmedTx : unconfirmedTransactions) {
            for (ErgoTransactionInput txInput : unconfirmedTx.getInputs()) {
                unconfirmedSpentBoxesIds.add(txInput.getBoxId());
            }
        }

    }

    @Override
    protected boolean canUseBox(InputBox box) {
        return !unconfirmedSpentBoxesIds.contains(box.getId().toString());
    }
}


