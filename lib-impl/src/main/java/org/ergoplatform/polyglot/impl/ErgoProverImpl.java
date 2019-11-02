package org.ergoplatform.polyglot.impl;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.polyglot.ErgoProver;
import org.ergoplatform.polyglot.JavaHelpers;
import org.ergoplatform.polyglot.SignedTransaction;
import org.ergoplatform.polyglot.UnsignedTransaction;
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter;
import scala.collection.IndexedSeq;
import sigmastate.basics.DLogProtocol;

public class ErgoProverImpl implements ErgoProver {
    private final BlockchainContextImpl _ctx;
    private final ErgoProvingInterpreter _prover;

    public ErgoProverImpl(BlockchainContextImpl ctx, ErgoProvingInterpreter prover) {
        _ctx = ctx;
        _prover = prover;
    }

    @Override
    public String getP2PKAddress() {
        DLogProtocol.ProveDlog pk = _prover.secrets().apply(0).publicImage();
        return JavaHelpers.encodedP2PKAddress(pk, _ctx.getNetworkType().networkPrefix);
    }

    @Override
    public SignedTransaction sign(UnsignedTransaction tx) {
        UnsignedTransactionImpl txImpl = (UnsignedTransactionImpl)tx;
        IndexedSeq<ErgoBox> boxesToSpend = JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend());
        IndexedSeq<ErgoBox> dataBoxes = JavaHelpers.toIndexedSeq(txImpl.getDataBoxes());
        ErgoLikeTransaction signed =
                _prover.sign(txImpl.getTx(), boxesToSpend, dataBoxes, txImpl.getStateContext()).get();
        return new SignedTransactionImpl(signed);
    }
}
