package org.ergoplatform.polyglot.impl;

import com.google.common.base.Preconditions;
import org.ergoplatform.polyglot.ErgoWallet;
import org.ergoplatform.polyglot.InputBox;
import org.ergoplatform.restapi.client.WalletBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ErgoWalletImpl implements ErgoWallet {
    private final List<WalletBox> _unspentBoxesData;
    private BlockchainContextImpl _ctx;
    private List<InputBox> _unspentBoxes;

    public ErgoWalletImpl(List<WalletBox> unspentBoxesData) {
        Preconditions.checkNotNull(unspentBoxesData);
        _unspentBoxesData = unspentBoxesData;
    }

    void setContext(BlockchainContextImpl ctx) {
        Preconditions.checkState(_ctx == null, "Cannot reset context of wallet %s", this);
        _ctx = ctx;
    }

    @Override
    public Optional<List<InputBox>> getUnspentBoxes(long amountToSpend) {
        if (_unspentBoxes == null) {
            _unspentBoxes = _unspentBoxesData.stream().map(boxInfo -> {
                return new InputBoxImpl(_ctx, boxInfo.getBox());
            }).collect(Collectors.toList());
        }

        if (amountToSpend == 0) {
            // all unspent boxes are requested
            return Optional.of(_unspentBoxes);
        }

        // collect boxes to cover requested amount
        ArrayList<InputBox> res = new ArrayList<InputBox>();
        long collected = 0;
        for (int i = 0; i < _unspentBoxes.size() && collected < amountToSpend; ++i) {
            InputBox box = _unspentBoxes.get(i);
            collected += box.getValue();
            res.add(box);
        }
        if (collected < amountToSpend) return Optional.empty();
        return Optional.of(res);
    }
}
