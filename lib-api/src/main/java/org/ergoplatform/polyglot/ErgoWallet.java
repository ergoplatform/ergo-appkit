package org.ergoplatform.polyglot;

import java.util.List;
import java.util.Optional;

public interface ErgoWallet {
    Optional<List<InputBox>> getUnspentBoxes(long amountToSpend);
}
