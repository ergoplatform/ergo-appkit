package org.ergoplatform.appkit.impl;
import org.ergoplatform.appkit.PreHeader;
import sigma.Coll;
import sigma.GroupElement;

public class PreHeaderImpl implements PreHeader {
    final sigma.PreHeader _ph;

    public PreHeaderImpl(sigma.PreHeader ph) {
        _ph = ph;
    }

    @Override
    public byte getVersion() {
        return _ph.version();
    }

    @Override
    public Coll<Byte> getParentId() {
        return (Coll<Byte>)(Object)_ph.parentId();
    }

    @Override
    public long getTimestamp() {
        return _ph.timestamp();
    }

    @Override
    public long getNBits() {
        return _ph.nBits();
    }

    @Override
    public int getHeight() {
        return _ph.height();
    }

    @Override
    public GroupElement getMinerPk() {
        return _ph.minerPk();
    }

    @Override
    public Coll<Byte> getVotes() {
        return (Coll<Byte>)(Object)_ph.votes();
    }
}
