package org.ergoplatform.appkit.impl;

import com.google.common.base.Preconditions;

import org.ergoplatform.appkit.BoxSelectorsJavaHelpers;
import org.ergoplatform.appkit.ErgoWallet;
import org.ergoplatform.appkit.InputBox;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ErgoWalletImpl implements ErgoWallet {
    private BlockchainContextImpl _ctx;
    private List<InputBox> _unspentBoxes;

    public ErgoWalletImpl(List<InputBox> unspentBoxes) {
        Preconditions.checkNotNull(unspentBoxes);
        _unspentBoxes = unspentBoxes;
    }

    void setContext(BlockchainContextImpl ctx) {
        Preconditions.checkState(_ctx == null, "Cannot reset context of wallet %s", this);
        _ctx = ctx;
    }

    @Override
    public Optional<List<InputBox>> getUnspentBoxes(long amountToSpend) {
        List<InputBox> selected = BoxSelectorsJavaHelpers.selectBoxes(_unspentBoxes, amountToSpend, Collections.emptyList());
        if (selected == null) return Optional.empty();
        return Optional.of(selected);
    }
}
