package org.ergoplatform.polyglot;

public class ErgoToken {
    private final ErgoId _id;
    private final long _value;

    public ErgoToken(ErgoId id, long value) {
        _id = id;
        _value = value;
    }

    public ErgoToken(byte[] idBytes, long value) {
        this(new ErgoId(idBytes), value);
    }

    public ErgoToken(String id, long value) {
        this(JavaHelpers.decodeBase16(id), value);
    }

    public ErgoId getId() {
        return _id;
    }

    public long getValue() {
        return _value;
    }
}

