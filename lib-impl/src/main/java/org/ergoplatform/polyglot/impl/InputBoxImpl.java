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
        _ergoBox = mkErgoBox(boxData);
    }

    public InputBoxImpl(ErgoBox ergoBox) {
        _ergoBox = ergoBox;
        _id = new ErgoId(ergoBox.id());
    }

    private ErgoBox mkErgoBox(ErgoTransactionOutput boxData) {
        byte[] treeBytes = JavaHelpers.Algos().decode(boxData.getErgoTree()).get();
        Values.ErgoTree tree = ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(treeBytes);
        List<ErgoToken> tokens = boxData.getAssets().stream()
                .map(a -> new ErgoToken(a.getTokenId(), a.getAmount()))
                .collect(Collectors.toList());
        List<Tuple2<String, Object>> regs = new ArrayList<>();
        boxData.getAdditionalRegisters().forEach((regName, value) -> {
            Values.Value v = JavaHelpers.deserializeValue(JavaHelpers.Algos().decode(value).get());
            regs.add(new Tuple2(regName, value));
        });

        ErgoBox ergoBox = JavaHelpers.createBox(
                boxData.getValue(), tree, tokens, regs, boxData.getCreationHeight(), boxData.getTransactionId(),
                boxData.getIndex().shortValue());
        return ergoBox;
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
}
