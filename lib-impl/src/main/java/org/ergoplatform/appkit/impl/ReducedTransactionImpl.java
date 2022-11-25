package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.UnsignedInput;
import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.sdk.ReducedErgoLikeTransaction;
import org.ergoplatform.sdk.ReducedErgoLikeTransactionSerializer$;
import org.ergoplatform.appkit.ReducedTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import scala.collection.JavaConversions;
import sigmastate.serialization.SigmaSerializer$;
import sigmastate.utils.SigmaByteWriter;

public class ReducedTransactionImpl implements ReducedTransaction {
    private final BlockchainContextBase _ctx;
    private final ReducedErgoLikeTransaction _tx;
    private final int _txCost;

    public ReducedTransactionImpl(
            BlockchainContextBase ctx,
            ReducedErgoLikeTransaction tx, int txCost) {
        _ctx = ctx;
        _tx = tx;
        _txCost = txCost;
    }

    @Override
    public String getId() {
        return getTx().unsignedTx().id();
    }

    @Override
    public List<String> getInputBoxesIds() {
        List<UnsignedInput> inputs = JavaConversions.seqAsJavaList(getTx().unsignedTx().inputs());
        List<String> returnVal = new ArrayList<>(inputs.size());
        for (UnsignedInput input : inputs) {
            returnVal.add(new ErgoId(input.boxId()).toString());
        }
        return returnVal;
    }

    @Override
    public List<OutBox> getOutputs() {
        List<ErgoBox> outputs = JavaConversions.seqAsJavaList(_tx.unsignedTx().outputs());
        List<OutBox> returnVal = new ArrayList<>(outputs.size());
        for (ErgoBoxCandidate output : outputs) {
            returnVal.add(new OutBoxImpl(output));
        }
        return returnVal;    }

    /**
     * Returns underlying {@link ReducedErgoLikeTransaction}
     */
    @Override
    public ReducedErgoLikeTransaction getTx() {
        return _tx;
    }

    @Override
    public int getCost() {
        return _txCost;
    }

    @Override
    public byte[] toBytes() {
        SigmaByteWriter w = SigmaSerializer$.MODULE$.startWriter();
        ReducedErgoLikeTransactionSerializer$.MODULE$.serialize(_tx, w);
        w.putUInt(_txCost);
        return w.toBytes();
    }

    @Override
    public int hashCode() {
        return 31 * _tx.hashCode() + _txCost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReducedTransactionImpl) {
           ReducedTransactionImpl that = (ReducedTransactionImpl)obj;
           return Objects.equals(that._tx, this._tx) && that._txCost == this._txCost;
        }
        return false;
    }
}
