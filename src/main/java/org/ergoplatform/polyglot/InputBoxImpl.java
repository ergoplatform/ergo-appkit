package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.api.client.ErgoTransactionOutput;
import org.ergoplatform.settings.ErgoAlgos;
import org.ergoplatform.settings.ErgoAlgos$;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class InputBoxImpl implements InputBox {
    private final ErgoId _id;
    private final ErgoTransactionOutput _boxData;

    public InputBoxImpl(ErgoTransactionOutput boxData) {
        _boxData = boxData;
        _id = new ErgoId(JavaHelpers.decodeBase16(boxData.getBoxId()));
    }

    @Override
    public ErgoId getId() {
        return _id;
    }

    public ErgoBox getErgoBox() {
        byte[] treeBytes = JavaHelpers.Algos().decode(_boxData.getErgoTree()).get();
        Values.ErgoTree tree = ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(treeBytes);
        List<ErgoToken> tokens = _boxData.getAssets().stream()
          .map(a -> new ErgoToken(a.getTokenId(), a.getAmount()))
          .collect(Collectors.toList());
        ErgoBox ergoBox = JavaHelpers.createBox(_boxData.getValue(), tree, tokens, _boxData.getCreationHeight());
        return ergoBox;
    }
}
