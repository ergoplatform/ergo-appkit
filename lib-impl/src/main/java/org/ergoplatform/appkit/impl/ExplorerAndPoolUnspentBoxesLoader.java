package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionInput;
import org.ergoplatform.restapi.client.Transactions;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * {@link BoxOperations.IUnspentBoxesLoader} implementation to be used with
 * {@link BoxOperations#withInputBoxesLoader(BoxOperations.IUnspentBoxesLoader)}
 * <p>
 * Instead of the default implementation, this one fetches the mempool from the node connected to
 * and blacklists the inputs so they aren't used for transactions made by {@link BoxOperations}
 * methods.
 */
public class ExplorerAndPoolUnspentBoxesLoader extends BoxOperations.ExplorerApiWithCheckerLoader {
    private final List<String> unconfirmedSpentBoxesIds = new ArrayList<>();

    @Override
    public void prepare(@Nonnull BlockchainContext ctx, List<Address> addresses, long grossAmount, @Nonnull List<ErgoToken> tokensToSpend) {
        if (!(ctx instanceof BlockchainContextImpl)) {
            throw new IllegalArgumentException("This loader needs to be used with BlockchainContextImpl");
        }

        unconfirmedSpentBoxesIds.clear();

        Transactions unconfirmedTransactions = ErgoNodeFacade.getUnconfirmedTransactions(((BlockchainContextImpl) ctx)
            .getRetrofit(), 1000, 0);
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


