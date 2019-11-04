package org.ergoplatform.polyglot.impl;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.polyglot.ErgoId;
import org.ergoplatform.polyglot.ErgoToken;
import org.ergoplatform.polyglot.InputBox;
import org.ergoplatform.polyglot.JavaHelpers;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import scala.Tuple2;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputBoxImpl implements InputBox {
    private final ErgoId _id;
    private final ErgoBox _ergoBox;

    public InputBoxImpl(ErgoTransactionOutput boxData) {
        _id = new ErgoId(JavaHelpers.decodeStringToBytes(boxData.getBoxId()));
        _ergoBox = ScalaBridge.isoErgoTransactionOutput().to(boxData);
    }

    public InputBoxImpl(ErgoBox ergoBox) {
        _ergoBox = ergoBox;
        _id = new ErgoId(ergoBox.id());
    }

    @Override
    public ErgoId getId() {
        return _id;
    }

    @Override
    public Long getValue() {
        return _ergoBox.value();
    }

    public ErgoBox getErgoBox() {
        return _ergoBox;
    }

    @Override
    public String toString() {
        return String.format("InputBox(%s, %s)", getId(), getValue());
    }
}
