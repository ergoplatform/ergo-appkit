package org.ergoplatform.polyglot;

import org.ergoplatform.*;
import scala.collection.IndexedSeq;

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

    public UnsignedTransactionBuilderImpl(
            BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    @Override
    public UnsignedTransactionBuilder inputs(InputBox... inputBoxIds) {
        List<UnsignedInput> items = Arrays.asList(inputBoxIds)
                .stream()
                .map(box -> JavaHelpers.createUnsignedInput(box.getId().getBytes()))
                .collect(Collectors.toList());
        _inputs.addAll(items);
        return this;
    }

    public UnsignedTransactionBuilder withDataInputs(DataInput... dataInputs) {
        Collections.addAll(_dataInputs, dataInputs);
        return this;
    }

    @Override
    public UnsignedTransactionBuilder outputs(OutBox... candidates) {
        _outputCandidates = new ArrayList<>();
        ErgoBox[] boxes =
                Stream.of(candidates).<ErgoBox>map(c -> ((OutBoxImpl)c).getErgoBox()).toArray(n -> new ErgoBox[n]);
        Collections.addAll(_outputCandidates, boxes);
        return this;
    }

    @Override
    public UnsignedTransaction build() {
        IndexedSeq<UnsignedInput> inputs = JavaHelpers.toIndexedSeq(_inputs);
        IndexedSeq<DataInput> dataInputs = JavaHelpers.toIndexedSeq(_dataInputs);
        IndexedSeq<ErgoBoxCandidate> outputCandidates = JavaHelpers.toIndexedSeq(_outputCandidates);

        return new UnsignedTransactionImpl(new UnsignedErgoLikeTransaction(inputs, dataInputs, outputCandidates));
    }

    @Override
    public OutBoxBuilder outBoxBuilder() {
        return new OutBoxBuilderImpl(this);
    }

    @Override
    public byte getNetworkPrefix() {
        return _ctx.getNetworkPrefix();
    }
}
