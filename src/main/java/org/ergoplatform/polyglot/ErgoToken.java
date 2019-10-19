package org.ergoplatform.polyglot;

public class ErgoToken {
    private final ErgoId _id;
    private final long _value;

    public ErgoToken(ErgoId id, long value) {
        _id = id;
        _value = value;
    }

    public ErgoId getId() {
        return _id;
    }

    public long getValue() {
        return _value;
    }
}

