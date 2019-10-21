package org.ergoplatform.polyglot;

import org.ergoplatform.api.client.ErgoTransactionOutput;

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
}
