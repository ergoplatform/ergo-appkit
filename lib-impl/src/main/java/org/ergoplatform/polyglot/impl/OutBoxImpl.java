package org.ergoplatform.polyglot.impl;

import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.polyglot.*;

public class OutBoxImpl implements OutBox {
  private final ErgoBoxCandidate _ergoBoxCandidate;

  public OutBoxImpl(ErgoBoxCandidate ergoBoxCandidate) {
    _ergoBoxCandidate = ergoBoxCandidate;
  }

  @Override
  public long getValue() {
    return _ergoBoxCandidate.value();
  }

  @Override
  public ErgoToken token(ErgoId id) {
    return null;
  }

  ErgoBoxCandidate getErgoBoxCandidate() {
    return _ergoBoxCandidate;
  }

  @Override
  public InputBox convertToInputWith(String txId, short boxIndex) {
    return new InputBoxImpl(_ergoBoxCandidate.toBox(txId, boxIndex));
  }
}
