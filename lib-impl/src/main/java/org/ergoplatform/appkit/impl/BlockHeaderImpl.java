package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.JavaHelpers;
import org.ergoplatform.appkit.BlockHeader;

import java.math.BigInteger;

import special.sigma.AvlTree;
import special.sigma.Header;

public class BlockHeaderImpl extends PreHeaderImpl implements BlockHeader {
    private final Header sigmaHeader;
    private final org.ergoplatform.restapi.client.BlockHeader header;

    public BlockHeaderImpl(Header h, org.ergoplatform.restapi.client.BlockHeader header) {
        super(JavaHelpers.toPreHeader(h));
        this.sigmaHeader = h;
        this.header = header;
    }

    public static BlockHeaderImpl createFromRestApi(org.ergoplatform.restapi.client.BlockHeader header) {
        Header h = ScalaBridge.isoBlockHeader().to(header);
        return new BlockHeaderImpl(h, header);
    }

    @Override
    public String getId() {
        return header.getId();
    }

    @Override
    public AvlTree getStateRoot() {
        return sigmaHeader.stateRoot();
    }

    @Override
    public String getAdProofsRoot() {
        return header.getAdProofsRoot();
    }

    @Override
    public String getTransactionsRoot() {
        return header.getTransactionsRoot();
    }

    @Override
    public String getExtensionHash() {
        return header.getExtensionHash();
    }

    @Override
    public String getPowSolutionsPk() {
        return header.getPowSolutions().getPk();
    }

    @Override
    public String getPowSolutionsW() {
        return header.getPowSolutions().getW();
    }

    @Override
    public BigInteger getPowSolutionsD() {
        return header.getPowSolutions().getD();
    }

    @Override
    public String getPowSolutionsN() {
        return header.getPowSolutions().getN();
    }
}
