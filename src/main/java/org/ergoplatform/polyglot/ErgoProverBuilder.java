package org.ergoplatform.polyglot;

import org.ergoplatform.wallet.interface4j.crypto.ErgoUnsafeProver;

public class ErgoProverBuilder {

  String _seed = "";

  public ErgoProverBuilder withSeed(String seed)  {
    _seed = seed;
    return this;
  }

  public ErgoProver build() {
    return new ErgoProver(new ErgoUnsafeProver(), _seed);
  }
}
