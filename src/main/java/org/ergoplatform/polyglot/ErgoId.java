package org.ergoplatform.polyglot;

import java.util.Arrays;

public class ErgoId {
    private final byte[] _idBytes;

    public ErgoId(byte[] idBytes) {
        _idBytes = idBytes;
    }

    public byte[] getBytes() {
        return _idBytes;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_idBytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ErgoId) {
            Arrays.equals(this._idBytes, ((ErgoId)obj)._idBytes);
        }
        return false;
    }
}
