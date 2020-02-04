package org.ergoplatform.appkit.impl;

import org.ergoplatform.Input;
import org.ergoplatform.appkit.*;
import sigmastate.SType;
import sigmastate.Values;

import java.util.Map;

public class SignedInputImpl implements SignedInput {
    private final SignedTransactionImpl _signedTx;
    private final Input _input;
    private final ErgoId _id;

    public SignedInputImpl(SignedTransactionImpl signedTransaction, Input input) {
        _signedTx = signedTransaction;
        _input = input;
        _id = new ErgoId(input.boxId());
    }

    @Override
    public byte[] getProofBytes() {
        return _input.spendingProof().proof();
    }

    @Override
    public Map<Byte, ErgoValue<?>> getContextVars() {
        Iso<Map<Byte, ErgoValue<?>>, scala.collection.Map<Byte, Values.EvaluatedValue<SType>>> iso = Iso.isoJMapToMap(Iso.isoErgoValueToSValue());
        scala.collection.Map<Byte, Values.EvaluatedValue<SType>> map = (scala.collection.Map<Byte, Values.EvaluatedValue<SType>>)(Object)_input.spendingProof().extension().values();
        return iso.from(map);
    }

    @Override
    public ErgoId getId() {
        return _id;
    }

    @Override
    public SignedTransaction getTransaction() {
        return _signedTx;
    }
}
