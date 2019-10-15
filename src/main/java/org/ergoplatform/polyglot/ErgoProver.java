package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.wallet.interface4j.crypto.ErgoUnsafeProver;
import sigmastate.basics.DLogProtocol;

public class ErgoProver {
  private final ErgoUnsafeProver _prover;
  private final DLogProtocol.DLogProverInput _secretInput;

  public ErgoProver(ErgoUnsafeProver prover, String seed) {
    _prover = prover;
    _secretInput = JavaHelpers.proverInputFromSeed(seed);
  }

  public ErgoLikeTransaction sign(UnsignedErgoLikeTransaction tx) {
    return _prover.prove(tx, _secretInput);
  }
}
