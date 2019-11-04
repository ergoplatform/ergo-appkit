package org.ergoplatform.polyglot.impl;

import org.ergoplatform.*;
import org.ergoplatform.polyglot.*;
import org.ergoplatform.polyglot.impl.ScalaBridge;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;
import scala.collection.IndexedSeq;
import special.collection.Coll;
import special.sigma.Header;
import special.sigma.PreHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class UnsignedTransactionBuilderImpl implements UnsignedTransactionBuilder {

    private final BlockchainContextImpl _ctx;
    ArrayList<UnsignedInput> _inputs = new ArrayList<>();
    ArrayList<DataInput> _dataInputs = new ArrayList<>();
    ArrayList<ErgoBoxCandidate> _outputCandidates = new ArrayList<>();
    private List<InputBoxImpl> _inputBoxes;
    private long _feeAmount;
    private ErgoAddress _changeAddress;

    public UnsignedTransactionBuilderImpl(
            BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    @Override
    public UnsignedTransactionBuilder boxesToSpend(InputBox... inputBoxes) {
        List<UnsignedInput> items = Arrays.asList(inputBoxes)
                .stream()
                .map(box -> JavaHelpers.createUnsignedInput(box.getId().getBytes()))
                .collect(Collectors.toList());
        _inputs.addAll(items);
        _inputBoxes = Stream.of(inputBoxes)
                .map(b -> (InputBoxImpl)b)
                .collect(Collectors.toList());
        return this;
    }

    @Override
    public UnsignedTransactionBuilder outputs(OutBox... outputs) {
        checkState(_outputCandidates.isEmpty(), "Outputs already specified.");
        _outputCandidates = new ArrayList<>();
        appendOutputs(outputs);
        return this;
    }

    @Override
    public UnsignedTransactionBuilder fee(long feeAmount) {
        checkState(_feeAmount == 0, "Fee already defined");
        _feeAmount = feeAmount;
        return this;
    }

    private void appendOutputs(OutBox... outputs) {
        ErgoBoxCandidate[] boxes =
                Stream.of(outputs).map(c -> ((OutBoxImpl)c).getErgoBoxCandidate()).toArray(n -> new ErgoBoxCandidate[n]);
        Collections.addAll(_outputCandidates, boxes);
    }

    @Override
    public UnsignedTransactionBuilder sendChangeTo(ErgoAddress changeAddress) {
        checkState(_changeAddress == null, "Change address is already specified");
        _changeAddress = changeAddress;
        return this;
    }

    @Override
    public UnsignedTransaction build() {
        IndexedSeq<UnsignedInput> inputs = JavaHelpers.toIndexedSeq(_inputs);
        IndexedSeq<DataInput> dataInputs = JavaHelpers.toIndexedSeq(_dataInputs);

        checkState(_feeAmount > 0, "Fee amount should be defined (using fee() method).");

        OutBox feeOut = outBoxBuilder()
                .value(_feeAmount)
                .contract(_ctx.newContract(ErgoScriptPredef.feeProposition(Parameters.MinerRewardDelay)))
                .build();
        appendOutputs(feeOut);

        checkState(_changeAddress != null, "Change address is not defined");

        Long inputTotal = _inputBoxes.stream().map(b -> b.getValue()).reduce(0L, (x, y) -> x + y);
        Long outputTotal = _outputCandidates.stream().map(b -> b.value()).reduce(0L, (x, y) -> x + y);

        long changeAmt = inputTotal - outputTotal;
        OutBox changeOut = outBoxBuilder()
                .value(changeAmt)
                .contract(_ctx.newContract(_changeAddress.script()))
                .build();
        appendOutputs(changeOut);

        IndexedSeq<ErgoBoxCandidate> outputCandidates = JavaHelpers.toIndexedSeq(_outputCandidates);
        UnsignedErgoLikeTransaction tx = new UnsignedErgoLikeTransaction(inputs, dataInputs, outputCandidates);
        List<ErgoBox> boxesToSpend = _inputBoxes.stream().map(b -> b.getErgoBox()).collect(Collectors.toList());
        ErgoLikeStateContext stateContext = createErgoLikeStateContext();

        return new UnsignedTransactionImpl(tx, boxesToSpend, new ArrayList<>(), stateContext);
    }

    private ErgoLikeStateContext createErgoLikeStateContext() {
        return new ErgoLikeStateContext() {
                private Coll<Header> _allHeaders = Iso.JListToColl(ScalaBridge.isoBlockHeader(), ErgoType.headerType().getRType()).to(_ctx.getHeaders());
                private Coll<Header> _headers = _allHeaders.slice(1, _allHeaders.length());
                private PreHeader _preHeader = JavaHelpers.toPreHeader(_allHeaders.apply(0));

                @Override
                public Coll<Header> sigmaLastHeaders() {
                    return _headers;
                }

                @Override
                public byte[] previousStateDigest() {
                    return JavaHelpers.getStateDigest(_headers.apply(0).stateRoot());
                }

                @Override
                public PreHeader sigmaPreHeader() {
                    return _preHeader;
                }
            };
    }

    @Override
    public BlockchainContext getCtx() {
        return _ctx;
    }

    @Override
    public OutBoxBuilder outBoxBuilder() {
        return new OutBoxBuilderImpl(_ctx, this);
    }

    @Override
    public NetworkType getNetworkType() {
        return _ctx.getNetworkType();
    }

    @Override
    public List<InputBox> getInputBoxes() {
        return _inputBoxes.stream().collect(Collectors.toList());
    }
}
