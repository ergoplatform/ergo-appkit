package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext;
import scala.collection.IndexedSeq;

import java.util.ArrayList;

public class UnsignedTransactionImpl implements UnsignedTransaction {
    private final UnsignedErgoLikeTransaction _tx;
    private ArrayList<ErgoBox> _boxesToSpend;
    private ArrayList<ErgoBox> _dataBoxes;
    private ErgoLikeStateContext _stateContext;

    public UnsignedTransactionImpl(UnsignedErgoLikeTransaction tx) {
        _tx = tx;
    }

    UnsignedErgoLikeTransaction getTx() {
        return _tx;
    }

    public ArrayList<ErgoBox> getBoxesToSpend() {
        return _boxesToSpend;
    }

    public ArrayList<ErgoBox> getDataBoxes() {
       return _dataBoxes;
    }

    public ErgoLikeStateContext getStateContext() {
        return _stateContext;
    }
}
