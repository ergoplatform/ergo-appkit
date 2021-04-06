package org.ergoplatform.appkit.impl;

import com.google.gson.Gson;
import org.ergoplatform.ErgoBox;
import org.ergoplatform.appkit.*;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.JSON;
import sigmastate.Values;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sigmastate.interpreter.ContextExtension;

public class InputBoxImpl implements InputBox {
    private final BlockchainContextImpl _ctx;
    private final ErgoId _id;
    private final ErgoBox _ergoBox;
    private final ErgoTransactionOutput _boxData;
    private ContextExtension _extension;

    public InputBoxImpl(BlockchainContextImpl ctx, ErgoTransactionOutput boxData) {
        _ctx = ctx;
        _id = new ErgoId(JavaHelpers.decodeStringToBytes(boxData.getBoxId()));
        _ergoBox = ScalaBridge.isoErgoTransactionOutput().to(boxData);
        _boxData = boxData;
        _extension = ContextExtension.empty();
    }

    public InputBoxImpl(BlockchainContextImpl ctx, ErgoBox ergoBox) {
        _ctx = ctx;
        _ergoBox = ergoBox;
        _id = new ErgoId(ergoBox.id());
        _boxData = ScalaBridge.isoErgoTransactionOutput().from(ergoBox);
        _extension = ContextExtension.empty();
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
    public List<ErgoToken> getTokens() {
        List<ErgoToken> tokens = Iso.isoTokensListToPairsColl().from(_ergoBox.additionalTokens());
        return tokens;
    }

    @Override
    public List<ErgoValue<?>> getRegisters() {
        return JavaHelpers.getBoxRegisters(_ergoBox);
    }

    @Override
    public Values.ErgoTree getErgoTree() {
        return _ergoBox.ergoTree();
    }

    @Override
    public InputBox withContextVars(ContextVar... variables) {
        ContextExtension extension = Iso.isoContextVarsToContextExtension().to(
          Stream.of(variables).collect(Collectors.toList())
        );
        InputBoxImpl res =  new InputBoxImpl(_ctx, _ergoBox);
        res._extension = extension;
        return res;
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson(prettyPrint, true);
    }

    @Override
    public String toJson(boolean prettyPrint, boolean formatJson) {
    	Gson gson = (prettyPrint || formatJson) ? JSON.createGson().setPrettyPrinting().create() : _ctx.getApiClient().getGson();
    	ErgoTransactionOutput data = _boxData;
    	if (prettyPrint) {
    		data = _ctx.getApiClient().cloneDataObject(_boxData);
    		data.ergoTree(_ergoBox.ergoTree().toString());
    	}
    	String json = gson.toJson(data);
    	return json;
    }

    public ErgoBox getErgoBox() {
        return _ergoBox;
    }

    public ContextExtension getExtension() {
        return _extension;
    }

    @Override
    public String toString() {
        return String.format("InputBox(%s, %s)", getId(), getValue());
    }
}
