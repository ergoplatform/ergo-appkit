package org.ergoplatform.polyglot;

public class ErgoId {
    private final byte[] _idBytes;

    public ErgoId(byte[] idBytes) {
        _idBytes = idBytes;
    }

    public byte[] getBytes() {
        return _idBytes;
    }
}
