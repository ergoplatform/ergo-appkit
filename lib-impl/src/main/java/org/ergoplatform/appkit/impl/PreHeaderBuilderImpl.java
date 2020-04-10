package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.PreHeader;
import org.ergoplatform.appkit.PreHeaderBuilder;
import sigmastate.eval.CPreHeader;
import special.collection.Coll;
import special.sigma.GroupElement;
import special.sigma.Header;

public class PreHeaderBuilderImpl implements PreHeaderBuilder {
    private final BlockchainContextImpl _ctx;
    private Byte _version;
    private Coll<Object> _parentId;
    private Long _timestamp;
    private Integer _height;
    private GroupElement _minerPk;
    private Coll<Object> _votes;
    private Long _nBits;

    public PreHeaderBuilderImpl(BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    @Override
    public PreHeaderBuilder version(byte version) {
        _version = version;
        return this;
    }

    @Override
    public PreHeaderBuilder parentId(Coll<Byte> parentId) {
        _parentId = (Coll<Object>)(Object)parentId;
        return this;
    }

    @Override
    public PreHeaderBuilder timestamp(Long timestamp) {
        _timestamp = timestamp;
        return this;
    }

    @Override
    public PreHeaderBuilder nBits(Long nBits) {
        _nBits = nBits;
        return this;
    }

    @Override
    public PreHeaderBuilder height(Integer height) {
        _height = height;
        return this;
    }

    @Override
    public PreHeaderBuilder minerPk(GroupElement minerPk) {
        _minerPk = minerPk;
        return this;
    }

    @Override
    public PreHeaderBuilder votes(Coll<Byte> votes) {
        _votes = (Coll<Object>)(Object)votes;
        return this;
    }

    @Override
    public PreHeader build() {
        Header h = ScalaBridge.isoBlockHeader().to(_ctx.getHeaders().get(0));
        byte version = _version == null ? h.version() : _version;
        Coll<Object> parentId = _parentId == null ? h.parentId() : _parentId;
        long timestamp = _timestamp == null ? h.timestamp() : _timestamp;
        long nBits = _nBits == null ? h.nBits() : _nBits;
        int height = _height == null ? h.height() : _height;
        GroupElement minerPk = _minerPk == null ? h.minerPk() : _minerPk;
        Coll<Object> votes = _votes == null ? h.votes() : _votes;
        CPreHeader ph = new CPreHeader(version, parentId, timestamp, nBits, height, minerPk, votes);
        return new PreHeaderImpl(ph);
    }
}
