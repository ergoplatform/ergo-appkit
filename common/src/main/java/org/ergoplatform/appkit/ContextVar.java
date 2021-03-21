package org.ergoplatform.appkit;

public class ContextVar {
    private final byte _id;
    private final ErgoValue<?> _value;

    public ContextVar(byte id, ErgoValue<?> value) {
        _id = id;
        _value = value;
    }

    public byte getId() { return _id; }

    public ErgoValue<?> getValue() { return _value; }
}
