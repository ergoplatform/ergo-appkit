package org.ergoplatform.polyglot;

import java.util.Dictionary;

public interface OutBoxBuilder {
    OutBoxBuilder value(long value);
    OutBoxBuilder proposition(Proposition proposition);

    OutBoxBuilder contract(Dictionary<String, Object> constants, String contractText);

    OutBoxBuilder tokens(ErgoToken... tokens);
    OutBoxBuilder registers(Dictionary<Byte, Object> regs);
    OutBox build();
}
