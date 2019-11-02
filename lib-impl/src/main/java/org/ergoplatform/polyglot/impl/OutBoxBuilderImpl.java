package org.ergoplatform.polyglot.impl;

import com.google.common.base.Preconditions;
import org.ergoplatform.*;
import org.ergoplatform.polyglot.*;
import scala.Tuple2;
import sigmastate.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;

public class OutBoxBuilderImpl implements OutBoxBuilder {

    private final UnsignedTransactionBuilderImpl _txB;
    private long _value = 0;
    private ErgoContract _contract;
    private ArrayList<ErgoToken> _tokens = new ArrayList<>();
    private ArrayList<ErgoValue> _registers = new ArrayList<>();

    public OutBoxBuilderImpl(UnsignedTransactionBuilderImpl txB) {
        _txB = txB;
    }

    public OutBoxBuilderImpl value(long value) {
        _value = value;
        return this;
    }

    @Override
    public OutBoxBuilderImpl contract(ErgoContract contract) {
        _contract = contract;
        return this;
    }

    public OutBoxBuilderImpl tokens(ErgoToken... tokens) {
        Preconditions.checkArgument(tokens.length > 0,
                "At least one token should be specified");
        Collections.addAll(_tokens, tokens);
        return this;
    }

    @Override
    public OutBoxBuilderImpl registers(ErgoValue... registers) {
        Preconditions.checkArgument(registers.length > 0,
                "At least one register should be specified");
        Collections.addAll(_registers, registers);
        return this;
    }

    public OutBox build() {
        Values.ErgoTree tree = JavaHelpers.compile(
                _contract.getConstants(), _contract.getErgoScript(), _txB.getNetworkType().networkPrefix);
        ErgoBoxCandidate ergoBoxCandidate = JavaHelpers.createBoxCandidate(_value, tree, _tokens,
                new ArrayList<Tuple2<String, Object>>(), _txB.getCtx().getHeight());  // TODO pass user specified
        // creationHeight
        return new OutBoxImpl(ergoBoxCandidate);
    }
}
