package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBoxCandidate;

public class OutBoxImpl implements OutBox {
  private final ErgoBoxCandidate _ergoBoxCandidate;

  public OutBoxImpl(ErgoBoxCandidate ergoBoxCandidate) {
    _ergoBoxCandidate = ergoBoxCandidate;
  }

  @Override
  public ErgoId getId() {
    return null;
  }

  @Override
  public UnsignedTransaction getTx() {
    return null;
  }

  @Override
  public int getBoxIndex() {
    return 0;
  }

  @Override
  public long getValue() {
    return 0;
  }

  @Override
  public ErgoToken token(ErgoId id) {
    return null;
  }

  ErgoBoxCandidate getErgoBoxCandidate() {
    return _ergoBoxCandidate;
  }
}
