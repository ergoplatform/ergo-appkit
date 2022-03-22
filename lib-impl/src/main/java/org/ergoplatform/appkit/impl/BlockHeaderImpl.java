package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.JavaHelpers;
import org.ergoplatform.appkit.BlockHeader;

import special.sigma.Header;

public class BlockHeaderImpl extends PreHeaderImpl implements BlockHeader {
    final Header h;

    public BlockHeaderImpl(Header h) {
        super(JavaHelpers.toPreHeader(h));
        this.h = h;
    }

    public static BlockHeaderImpl createFromRestApi(org.ergoplatform.restapi.client.BlockHeader header) {
        Header h = ScalaBridge.isoBlockHeader().to(header);
        return new BlockHeaderImpl(h);
    }
}
