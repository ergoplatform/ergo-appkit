package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBox;

public class OutBoxImpl implements OutBox {
  private final ErgoBox _ergoBox;

  public OutBoxImpl(ErgoBox ergoBox) {
    _ergoBox = ergoBox;
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
  public Proposition getProposition() {
    return null;
  }

  @Override
  public ErgoToken token(ErgoId id) {
    return null;
  }

  ErgoBox getErgoBox() {
    return _ergoBox;
  }
}
