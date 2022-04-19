package org.ergoplatform.appkit;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * {@link BoxOperations.IUnspentBoxesLoader} implementation to be used with
 * {@link BoxOperations#withInputBoxesLoader(BoxOperations.IUnspentBoxesLoader)}
 * <p>
 * Pre-loads all boxes from node wallet, then returns pages of boxes from the master list.
 * May optionally allow boxes to be grabbed from the MemPool as well.
 */
public class NodeWalletUnspentBoxesLoader implements BoxOperations.IUnspentBoxesLoader {
    private List<InputBox> allBoxes;
    private boolean withMemPoolBoxes = false;

    public NodeWalletUnspentBoxesLoader withMemPoolBoxes(boolean withMemPoolBoxes) {
        this.withMemPoolBoxes = withMemPoolBoxes;
        return this;
    }

    public List<InputBox> getAllBoxes() {
        return allBoxes;
    }

    @Override
    public void prepare(@Nonnull BlockchainContext ctx, List<Address> addresses, long grossAmount, @Nonnull List<ErgoToken> tokensToSpend) {
        BlockchainDataSource dataSource = ctx.getDataSource();
        if(withMemPoolBoxes)
            allBoxes = dataSource.getUnconfirmedUnspentWalletBoxes();
        else
            allBoxes = dataSource.getUnspentWalletBoxes();
    }

    @Override
    public void prepareForAddress(Address address) {
        // Do nothing, actual address used will be from node wallet anyway
        // Maybe interface should be changed to be more abstracted? Or a separate interface is needed?
    }

    @Nonnull
    @Override
    public List<InputBox> loadBoxesPage(@Nonnull BlockchainContext ctx, @Nonnull Address sender, @Nonnull Integer page) {
        int currentOffset = ctx.DEFAULT_LIMIT_FOR_API * page;
        int pageEndIndex  = currentOffset + ctx.DEFAULT_LIMIT_FOR_API;

        if(currentOffset > allBoxes.size())
            return allBoxes.subList(0,0); // Return empty list in case current offset is greater than ending index

        if(pageEndIndex > allBoxes.size())
            pageEndIndex = allBoxes.size(); // Shorten returned list to last index in case pageEndIndex is greater than size of list

        return allBoxes.subList(currentOffset, pageEndIndex);
    }
}


