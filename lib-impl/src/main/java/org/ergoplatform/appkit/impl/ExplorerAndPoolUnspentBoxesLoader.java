package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.explorer.client.model.TransactionInfo;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionInput;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
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
 * <p>
 * Optionally, you can also use boxes available on mempool to be spent, allowing to make chained tx.
 */
public class ExplorerAndPoolUnspentBoxesLoader extends BoxOperations.ExplorerApiWithCheckerLoader {
    private final List<String> unconfirmedSpentBoxesIds = new ArrayList<>();
    private boolean unconfirmedBoxesFetched;
    private boolean allowChainedTx = false;

    public ExplorerAndPoolUnspentBoxesLoader withAllowChainedTx(boolean allowChainedTx) {
        this.allowChainedTx = allowChainedTx;
        return this;
    }

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
    public void prepareForAddress(Address address) {
        unconfirmedBoxesFetched = false;
    }

    @Override
    protected boolean canUseBox(InputBox box) {
        return !unconfirmedSpentBoxesIds.contains(box.getId().toString());
    }

    @Nonnull
    @Override
    public List<InputBox> loadBoxesPage(@Nonnull BlockchainContext ctx, @Nonnull Address sender, @Nonnull Integer page) {
        List<InputBox> inputBoxes = super.loadBoxesPage(ctx, sender, page);

        if (inputBoxes.isEmpty() && allowChainedTx && !unconfirmedBoxesFetched) {
            // needed to not go into an infinite loop for the last page
            unconfirmedBoxesFetched = true;

            // fetch unconfirmed transactions for this address and add its boxes as last page
            try {
                String senderAddress = sender.toString();
                BlockchainContextImpl ctxImpl = (BlockchainContextImpl) ctx;
                List<TransactionInfo> mempoolTx = ExplorerFacade.getApiV1MempoolTransactionsByaddressP1(ctxImpl.getRetrofitExplorer(),
                    senderAddress, 0, 50);

                // now check if we have boxes on the address
                for (TransactionInfo tx : mempoolTx) {
                    for (OutputInfo output : tx.getOutputs()) {
                        if (output.getAddress().equals(senderAddress) && output.getSpentTransactionId() == null) {
                            // we have an unconfirmed box - get info from node for it
                            ErgoTransactionOutput boxInfo = ErgoNodeFacade.getBoxWithPoolById(((BlockchainContextImpl) ctx).getRetrofit(), output.getBoxId());
                            if (boxInfo != null) {
                                inputBoxes.add(new InputBoxImpl(ctxImpl, boxInfo));
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                // something did not work - bad luck but just proceed without chained tx
            }
        }

        return inputBoxes;
    }
}


