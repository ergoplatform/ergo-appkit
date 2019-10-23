package org.ergoplatform.polyglot;

public interface ErgoProver {
  SignedTransaction sign(UnsignedTransaction tx);
}

