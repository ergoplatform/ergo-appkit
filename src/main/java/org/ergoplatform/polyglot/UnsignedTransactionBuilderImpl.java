package org.ergoplatform.polyglot;

import org.ergoplatform.*;
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

public class UnsignedTransactionBuilderImpl implements UnsignedTransactionBuilder {

    private final BlockchainContextImpl _ctx;
    ArrayList<UnsignedInput> _inputs = new ArrayList<>();
    ArrayList<DataInput> _dataInputs = new ArrayList<>();
    ArrayList<ErgoBoxCandidate> _outputCandidates = new ArrayList<>();
    private List<InputBoxImpl> _inputBoxes;

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

    public UnsignedTransactionBuilder withDataInputs(DataInput... dataInputs) {
        Collections.addAll(_dataInputs, dataInputs);
        return this;
    }

    @Override
    public UnsignedTransactionBuilder outputs(OutBox... candidates) {
        _outputCandidates = new ArrayList<>();
        ErgoBoxCandidate[] boxes =
                Stream.of(candidates).map(c -> ((OutBoxImpl)c).getErgoBoxCandidate()).toArray(n -> new ErgoBoxCandidate[n]);
        Collections.addAll(_outputCandidates, boxes);
        return this;
    }

    @Override
    public UnsignedTransaction build() {
        IndexedSeq<UnsignedInput> inputs = JavaHelpers.toIndexedSeq(_inputs);
        IndexedSeq<DataInput> dataInputs = JavaHelpers.toIndexedSeq(_dataInputs);
        IndexedSeq<ErgoBoxCandidate> outputCandidates = JavaHelpers.toIndexedSeq(_outputCandidates);
        UnsignedErgoLikeTransaction tx = new UnsignedErgoLikeTransaction(inputs, dataInputs, outputCandidates);
        List<ErgoBox> boxesToSpend = _inputBoxes.stream().map(b -> b.getErgoBox()).collect(Collectors.toList());

        ErgoLikeStateContext stateContext = new ErgoLikeStateContext() {
            private Coll<Header> _allHeaders = ScalaBridge.toHeaders(_ctx.getHeaders());
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

        return new UnsignedTransactionImpl(tx, boxesToSpend, new ArrayList<>(), stateContext);
    }

    @Override
    public BlockchainContext getCtx() {
        return _ctx;
    }

    @Override
    public OutBoxBuilder outBoxBuilder() {
        return new OutBoxBuilderImpl(this);
    }

    @Override
    public byte getNetworkPrefix() {
        return _ctx.getNetworkPrefix();
    }

    @Override
    public List<InputBox> getInputBoxes() {
        return _inputBoxes.stream().collect(Collectors.toList());
    }
}
