package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.api.client.ErgoTransactionOutput;
import org.ergoplatform.settings.ErgoAlgos;
import org.ergoplatform.settings.ErgoAlgos$;
import scala.Tuple2;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;
import sigmastate.serialization.ValueSerializer;
import special.sigma.AnyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputBoxImpl implements InputBox {
    private final ErgoId _id;
    private final ErgoTransactionOutput _boxData;

    public InputBoxImpl(ErgoTransactionOutput boxData) {
        _boxData = boxData;
        _id = new ErgoId(JavaHelpers.decodeStringToBytes(boxData.getBoxId()));
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
        List<Tuple2<String, Object>> regs = new ArrayList<>();
        _boxData.getAdditionalRegisters().forEach((regName, value) -> {
            Values.Value v = JavaHelpers.deserializeValue(JavaHelpers.Algos().decode(value).get());
            regs.add(new Tuple2(regName, value));
        });

        ErgoBox ergoBox = JavaHelpers.createBox(
                _boxData.getValue(), tree, tokens, regs, _boxData.getTransactionId(),
                 _boxData.getIndex().shortValue(), _boxData.getCreationHeight());
        return ergoBox;
    }
}
