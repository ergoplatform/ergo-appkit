package org.ergoplatform.appkit;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Mocked loader for BoxOperations
 */
public class MockedBoxesLoader implements BoxOperations.IUnspentBoxesLoader {

    private final List<InputBox> mockBoxes;

    public MockedBoxesLoader(List<InputBox> mockBoxes) {
        this.mockBoxes = mockBoxes;
    }

    @Override
    public void prepare(@Nonnull BlockchainContext ctx, List<Address> addresses, long grossAmount, @Nonnull List<ErgoToken> tokensToSpend) {

    }

    @Override
    public void prepareForAddress(Address address) {

    }

    @Nonnull
    @Override
    public List<InputBox> loadBoxesPage(@Nonnull BlockchainContext ctx, @Nonnull Address address, @Nonnull Integer page) {
        if (page == 0) {
            return mockBoxes;
        } else
            return Collections.emptyList();
    }
}
