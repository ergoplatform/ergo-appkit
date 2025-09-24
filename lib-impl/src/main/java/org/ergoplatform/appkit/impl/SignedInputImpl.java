package org.ergoplatform.appkit.impl;

import org.ergoplatform.Input;
import org.ergoplatform.appkit.*;
import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.sdk.SdkIsos;
import sigma.ast.EvaluatedValue;
import sigma.data.Iso;
import sigma.ast.SType;


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
        Iso<Map<Byte, ErgoValue<?>>, scala.collection.Map<Byte, EvaluatedValue<SType>>> iso = SdkIsos.isoJMapToMap(AppkitIso.isoErgoValueToSValue());
        scala.collection.Map<Byte, EvaluatedValue<SType>> map = (scala.collection.Map<Byte, EvaluatedValue<SType>>)(Object)_input.spendingProof().extension().values();
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
