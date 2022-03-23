package org.ergoplatform.appkit;

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
        unconfirmedSpentBoxesIds.clear();
        BlockchainDataSource dataSource = ctx.getDataSource();
        List<Transaction> unconfirmedTransactions = dataSource.getUnconfirmedTransactions(1000, 0);
        for (Transaction unconfirmedTx : unconfirmedTransactions) {
            unconfirmedSpentBoxesIds.addAll(unconfirmedTx.getInputBoxesIds());
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
                inputBoxes.addAll(ctx.getDataSource().getUnconfirmedUnspentBoxesFor(sender, 0, 50));
            } catch (Throwable t) {
                // something did not work - bad luck but just proceed without chained tx
            }
        }

        return inputBoxes;
    }
}


