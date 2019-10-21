package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.wallet.interface4j.crypto.ErgoUnsafeProver;
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter;
import scala.collection.IndexedSeq;
import scala.util.Try;
import sigmastate.basics.DLogProtocol;

import java.util.ArrayList;

public class ErgoProverImpl implements ErgoProver {
  private final ErgoProvingInterpreter _prover;

  public ErgoProverImpl(ErgoProvingInterpreter prover) {
    _prover = prover;
  }

  @Override
  public SignedTransaction sign(UnsignedTransaction tx) {
    UnsignedTransactionImpl txImpl = (UnsignedTransactionImpl)tx;
    IndexedSeq<ErgoBox> boxesToSpend = JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend());
    IndexedSeq<ErgoBox> dataBoxes = JavaHelpers.toIndexedSeq(txImpl.getDataBoxes());
    ErgoLikeTransaction signed = _prover.sign(txImpl.getTx(), boxesToSpend, dataBoxes, txImpl.getStateContext()).get();
    return new SignedTransactionImpl(signed);
  }
}
