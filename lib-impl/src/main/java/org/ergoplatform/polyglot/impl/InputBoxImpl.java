package org.ergoplatform.polyglot.impl;

import com.google.gson.Gson;
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
    private final BlockchainContextImpl _ctx;
    private final ErgoId _id;
    private final ErgoBox _ergoBox;
    private final ErgoTransactionOutput _boxData;

    public InputBoxImpl(BlockchainContextImpl ctx, ErgoTransactionOutput boxData) {
        _ctx = ctx;
        _id = new ErgoId(JavaHelpers.decodeStringToBytes(boxData.getBoxId()));
        _ergoBox = ScalaBridge.isoErgoTransactionOutput().to(boxData);
        _boxData = boxData;
    }

    public InputBoxImpl(BlockchainContextImpl ctx, ErgoBox ergoBox) {
        _ctx = ctx;
        _ergoBox = ergoBox;
        _id = new ErgoId(ergoBox.id());
        _boxData = ScalaBridge.isoErgoTransactionOutput().from(ergoBox);
    }

    @Override
    public ErgoId getId() {
        return _id;
    }

    @Override
    public Long getValue() {
        return _ergoBox.value();
    }

    @Override
    public String toJson() {
        Gson gson = _ctx.getApiClient().getGson();
        ErgoTransactionOutput cloned = gson.fromJson(gson.toJson(_boxData), _boxData.getClass());
        cloned.ergoTree(_ergoBox.ergoTree().toString());
        String json = gson.toJson(cloned);
        return json;
    }

    public ErgoBox getErgoBox() {
        return _ergoBox;
    }

    @Override
    public String toString() {
        return String.format("InputBox(%s, %s)", getId(), getValue());
    }
}
