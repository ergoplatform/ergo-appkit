package org.ergoplatform.appkit.impl;

import org.ergoplatform.*;
import org.ergoplatform.appkit.*;
import org.ergoplatform.appkit.impl.ScalaBridge;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;
import org.ergoplatform.wallet.transactions.TransactionBuilder;
import org.ergoplatform.wallet.boxes.DefaultBoxSelector$;
import org.ergoplatform.wallet.boxes.BoxSelector;
import scala.collection.IndexedSeq;
import scala.collection.immutable.Map;
import special.collection.Coll;
import special.sigma.Header;
import special.sigma.PreHeader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static org.ergoplatform.appkit.Parameters.MinChangeValue;
import static org.ergoplatform.appkit.Parameters.MinFee;

public class UnsignedTransactionBuilderImpl implements UnsignedTransactionBuilder {

    private final BlockchainContextImpl _ctx;
    ArrayList<UnsignedInput> _inputs = new ArrayList<>();
    ArrayList<DataInput> _dataInputs = new ArrayList<>();
    ArrayList<ErgoBoxCandidate> _outputCandidates = new ArrayList<>();
    private List<InputBoxImpl> _inputBoxes;
    private List<InputBoxImpl> _dataInputBoxes = new ArrayList<>();
    private List<ErgoToken> _tokensToBurn = new ArrayList<>();
    private long _feeAmount;
    private ErgoAddress _changeAddress;
    private PreHeaderImpl _ph;

    public UnsignedTransactionBuilderImpl(
            BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    @Override
    public UnsignedTransactionBuilder preHeader(org.ergoplatform.appkit.PreHeader ph) {
        checkState(_ph == null, "PreHeader is already specified");
        _ph = (PreHeaderImpl)ph;
        return this;
    }

    @Override
    public UnsignedTransactionBuilder boxesToSpend(List<InputBox> inputBoxes) {
        List<UnsignedInput> items = inputBoxes
                .stream()
                .map(box -> JavaHelpers.createUnsignedInput(box.getId().getBytes()))
                .collect(Collectors.toList());
        _inputs.addAll(items);
        _inputBoxes = inputBoxes.stream()
                .map(b -> (InputBoxImpl)b)
                .collect(Collectors.toList());
        return this;
    }

    @Override
    public UnsignedTransactionBuilder withDataInputs(List<InputBox> inputBoxes) {
        List<DataInput> items = inputBoxes
            .stream()
            .map(box -> JavaHelpers.createDataInput(box.getId().getBytes()))
            .collect(Collectors.toList());
        _dataInputs.addAll(items);
        _dataInputBoxes = inputBoxes.stream()
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

    @Override
    public UnsignedTransactionBuilder tokensToBurn(ErgoToken... tokens) {
        _tokensToBurn.addAll(Stream.of(tokens).collect(Collectors.toList()));
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
        List<ExtendedInputBox> boxesToSpend = _inputBoxes.stream()
            .map(b -> new ExtendedInputBox(b.getErgoBox(), b.getExtension()))
            .collect(Collectors.toList());
        List<ErgoBox> dataInputBoxes = _dataInputBoxes.stream()
            .map(b -> b.getErgoBox())
            .collect(Collectors.toList());
        IndexedSeq<DataInput> dataInputs = JavaHelpers.toIndexedSeq(_dataInputs);

        checkState(_feeAmount > 0, "Fee amount should be defined (using fee() method).");
        checkState(_feeAmount >= MinFee, "Fee amount should be >= " + MinFee + ", got " + _feeAmount);
        checkState(_changeAddress != null, "Change address is not defined");

        IndexedSeq<ErgoBoxCandidate> outputCandidates = JavaHelpers.toIndexedSeq(_outputCandidates);
        IndexedSeq<ErgoBox> inputBoxes = JavaHelpers.toIndexedSeq(
            boxesToSpend.stream()
                .map(eb -> eb.box())
                .collect(Collectors.toList()));
        Map<String, Object> burnTokens = JavaHelpers.createTokensMap(
          Iso$.MODULE$.isoJListErgoTokenToMapPair().to(_tokensToBurn)
        );
        BoxSelector boxSelector = DefaultBoxSelector$.MODULE$;
        UnsignedErgoLikeTransaction tx = TransactionBuilder.buildUnsignedTx(
            inputBoxes,
            dataInputs,
            outputCandidates,
            _ctx.getHeight(),
            _feeAmount,
            _changeAddress,
            MinChangeValue,
            Parameters.MinerRewardDelay,
            burnTokens,
            boxSelector).get();
        ErgoLikeStateContext stateContext = createErgoLikeStateContext();

        return new UnsignedTransactionImpl(tx, boxesToSpend, dataInputBoxes, stateContext);
    }

    private ErgoLikeStateContext createErgoLikeStateContext() {
        return new ErgoLikeStateContext() {
            private Coll<Header> _allHeaders = Iso.JListToColl(ScalaBridge.isoBlockHeader(),
                    ErgoType.headerType().getRType()).to(_ctx.getHeaders());
            private Coll<Header> _headers = _allHeaders.slice(1, _allHeaders.length());
            private PreHeader _preHeader = _ph == null ? _ctx._preHeader._ph : _ph._ph;

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
    public org.ergoplatform.appkit.PreHeader getPreHeader() {
        return _ph == null ? _ctx.getPreHeader() : _ph;
    }

    @Override
    public OutBoxBuilder outBoxBuilder() {
        return new OutBoxBuilderImpl(this);
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
