package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.appkit.*;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;

import java.util.List;
import java.util.stream.Collectors;

public class UnsignedTransactionImpl implements UnsignedTransaction {
    private final UnsignedErgoLikeTransaction _tx;
    private List<ExtendedInputBox> _boxesToSpend;
    private List<ErgoBox> _dataBoxes;
    private List<ErgoBoxCandidate> _outputs;
    private ErgoAddress _changeAddress;
    private ErgoLikeStateContext _stateContext;
    private BlockchainContextImpl _ctx;


    public UnsignedTransactionImpl(
        UnsignedErgoLikeTransaction tx, List<ExtendedInputBox> boxesToSpend,
        List<ErgoBox> dataBoxes, List<ErgoBoxCandidate> outputs, ErgoAddress changeAddress, ErgoLikeStateContext stateContext, BlockchainContextImpl ctx) {
        _tx = tx;
        _boxesToSpend = boxesToSpend;
        _dataBoxes = dataBoxes;
        _outputs = outputs;
        _changeAddress = changeAddress;
        _stateContext = stateContext;
        _ctx = ctx;
    }

    UnsignedErgoLikeTransaction getTx() {
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
        return _boxesToSpend.stream().map(b -> new InputBoxImpl(_ctx, b.box())).collect(Collectors.toList());
    }

    @Override
    public List<OutBox> getOutputs() {
        return _outputs.stream().map(b -> new OutBoxImpl(_ctx, b)).collect(Collectors.toList());
    }

    @Override
    public List<InputBox> getDataInputs() {
        return _dataBoxes.stream().map(b -> new InputBoxImpl(_ctx, b)).collect(Collectors.toList());
    }

    @Override
    public ErgoAddress getChangeAddress() {
        return _changeAddress;
    }
}
