package org.ergoplatform.polyglot;

import org.ergoplatform.*;
import scala.Tuple2;
import sigmastate.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;

public class OutBoxBuilderImpl implements OutBoxBuilder {

    private final UnsignedTransactionBuilderImpl _txB;
    private long _value = 0;
    private Dictionary<String, Object> _constants;
    private String _contractText = "";
    private ArrayList<ErgoToken> _tokens = new ArrayList<>();

    public OutBoxBuilderImpl(UnsignedTransactionBuilderImpl txB) {
        _txB = txB;
    }

    public OutBoxBuilderImpl value(long value) {
        _value = value;
        return this;
    }

    @Override
    public OutBoxBuilderImpl proposition(Proposition proposition) {
        return null;
    }

    @Override
    public OutBoxBuilderImpl contract(Dictionary<String, Object> constants, String contractText) {
        _constants = constants;
        _contractText = contractText;
        return this;
    }

    public OutBoxBuilderImpl tokens(ErgoToken... tokens) {
        Collections.addAll(_tokens, tokens);
        return this;
    }

    @Override
    public OutBoxBuilderImpl registers(Dictionary<Byte, Object> regs) {
        return null;
    }

    public OutBox build() {
        Values.ErgoTree tree = JavaHelpers.compile(_constants, _contractText, _txB.getNetworkPrefix());
        ErgoBoxCandidate ergoBoxCandidate = JavaHelpers.createBoxCandidate(_value, tree, _tokens,
                new ArrayList<Tuple2<String, Object>>(), _txB.getCtx().getHeight());  // TODO pass user specified creationHeight
        return new OutBoxImpl(ergoBoxCandidate);
    }
}
