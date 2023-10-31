package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BlockHeader;

import java.math.BigInteger;

import org.ergoplatform.sdk.Extensions;
import sigma.Coll;
import sigma.AvlTree;
import sigma.GroupElement;
import sigma.Header;

public class BlockHeaderImpl extends PreHeaderImpl implements BlockHeader {
    private final Header sigmaHeader;
    private final org.ergoplatform.restapi.client.BlockHeader header;

    public BlockHeaderImpl(Header h, org.ergoplatform.restapi.client.BlockHeader header) {
        super(new Extensions.HeaderOps(h).toPreHeader());
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
    public Coll<Byte> getAdProofsRoot() {
        return (Coll<Byte>) (Object) sigmaHeader.ADProofsRoot();
    }

    @Override
    public Coll<Byte> getTransactionsRoot() {
        return (Coll<Byte>) (Object) sigmaHeader.transactionsRoot();
    }

    @Override
    public Coll<Byte> getExtensionHash() {
        return (Coll<Byte>) (Object) sigmaHeader.extensionRoot();
    }

    @Override
    public GroupElement getPowSolutionsPk() {
        return sigmaHeader.minerPk();
    }

    @Override
    public GroupElement getPowSolutionsW() {
        return sigmaHeader.powOnetimePk();
    }

    @Override
    public BigInteger getPowSolutionsD() {
        return header.getPowSolutions().getD();
    }

    @Override
    public Coll<Byte> getPowSolutionsN() {
        return (Coll<Byte>) (Object) sigmaHeader.powNonce();
    }
}
