package org.ergoplatform.appkit.impl;

import com.google.gson.Gson;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.appkit.*;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.JSON;
import org.ergoplatform.restapi.client.UnsignedErgoTransaction;
import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.sdk.ErgoToken;
import org.ergoplatform.sdk.ExtendedInputBox;
import org.ergoplatform.sdk.wallet.protocol.context.ErgoLikeStateContext;

import java.util.ArrayList;
import java.util.List;

import scala.collection.JavaConverters;
import sigmastate.Values;

import javax.annotation.Nonnull;

public class UnsignedTransactionImpl implements UnsignedTransaction {
    private final UnsignedErgoLikeTransaction _tx;
    private List<ExtendedInputBox> _boxesToSpend;
    private List<ErgoBox> _dataBoxes;
    private List<ErgoToken> _tokensToBurn;
    private List<ErgoBox> _outputs;
    private ErgoAddress _changeAddress;
    private ErgoLikeStateContext _stateContext;
    private BlockchainContextImpl _ctx;


    public UnsignedTransactionImpl(
        UnsignedErgoLikeTransaction tx, List<ExtendedInputBox> boxesToSpend,
        List<ErgoBox> dataBoxes, ErgoAddress changeAddress,
        ErgoLikeStateContext stateContext, BlockchainContextImpl ctx,
        List<ErgoToken> tokensToBurn) {
        _tx = tx;
        _boxesToSpend = boxesToSpend;
        _dataBoxes = dataBoxes;
        _tokensToBurn = tokensToBurn;
        _outputs = AppkitHelpers$.MODULE$.toJavaList(_tx.outputs());
        _changeAddress = changeAddress;
        _stateContext = stateContext;
        _ctx = ctx;
    }

    @Override
    public String getId() {
        return getTx().id();
    }

    public UnsignedErgoLikeTransaction getTx() {
        return _tx;
    }

    public List<ExtendedInputBox> getBoxesToSpend() {
        return _boxesToSpend;
    }

    public List<ErgoBox> getDataBoxes() {
       return _dataBoxes;
    }

    public ErgoLikeStateContext getStateContext() {
        return _stateContext;
    }

    @Override
    public List<InputBox> getInputs() {
        List<InputBox> returnVal = new ArrayList<>(_boxesToSpend.size());
        for (ExtendedInputBox boxToSpend : _boxesToSpend) {
            returnVal.add(new InputBoxImpl(boxToSpend.box()));
        }
        return returnVal;
    }

    @Override
    public List<String> getInputBoxesIds() {
        List<String> returnVal = new ArrayList<>(_boxesToSpend.size());
        for (ExtendedInputBox boxToSpend : _boxesToSpend) {
            returnVal.add(new ErgoId(boxToSpend.box().id()).toString());
        }
        return returnVal;
    }

    @Override
    public List<OutBox> getOutputs() {
        List<OutBox> returnVal = new ArrayList<>(_outputs.size());
        for (ErgoBoxCandidate output : _outputs) {
            returnVal.add(new OutBoxImpl(output));
        }
        return returnVal;
    }

    @Override
    public List<InputBox> getDataInputs() {
        List<InputBox> returnVal = new ArrayList<>(_dataBoxes.size());
        for (ErgoBox dataBox : _dataBoxes) {
            returnVal.add(new InputBoxImpl(dataBox));
        }
        return returnVal;
    }

    @Override
    public ErgoAddress getChangeAddress() {
        return _changeAddress;
    }

    @Override
    public @Nonnull List<ErgoToken> getTokensToBurn() {
        return _tokensToBurn;
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson(prettyPrint, true);
    }

    @Override
    public String toJson(boolean prettyPrint, boolean formatJson) {
        UnsignedErgoTransaction tx = ScalaBridge.isoUnsignedErgoTransaction().from(_tx);
        if (prettyPrint) {
            for (ErgoTransactionOutput o : tx.getOutputs()) {
                Values.ErgoTree tree = ScalaBridge.isoStringToErgoTree().to(o.getErgoTree());
                o.ergoTree(tree.toString());
            }
        }
        Gson gson = (prettyPrint || formatJson) ? JSON.createGson().setPrettyPrinting().create() : JSON.createGson().create();
        String json = gson.toJson(tx);
        return json;
    }
}
