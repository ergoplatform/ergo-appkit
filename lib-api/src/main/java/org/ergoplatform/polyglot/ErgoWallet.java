package org.ergoplatform.polyglot;

import java.util.List;

public interface ErgoWallet {
    List<InputBox> getUnspentBoxes();
}
