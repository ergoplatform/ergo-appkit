package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.appkit.*;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;

import java.util.ArrayList;
import java.util.List;

import scala.collection.JavaConversions;

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
        _outputs = JavaConversions.seqAsJavaList(_tx.outputs());
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
}
