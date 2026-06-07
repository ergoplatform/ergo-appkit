package org.ergoplatform.appkit.impl;

import org.ergoplatform.Input;
import org.ergoplatform.appkit.*;
import org.ergoplatform.sdk.ErgoId;

import java.util.HashMap;
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
        Map<Byte, ErgoValue<?>> result = new HashMap<>();
        java.util.Map<Object, Object> javaMap = ScalaBridge.contextExtensionValuesAsJava(_input);
        javaMap.forEach((key, value) ->
            result.put((Byte) key, AppkitIso.isoErgoValueToSValue().from((sigma.ast.EvaluatedValue) value)));
        return result;
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
