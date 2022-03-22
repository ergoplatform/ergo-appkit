package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.BlockchainParameters;
import org.ergoplatform.restapi.client.NodeInfo;

public class NodeInfoParameters implements BlockchainParameters {
    private final NodeInfo nodeInfo;

    public NodeInfoParameters(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    @Override
    public NetworkType getNetworkType() {
        return NetworkType.fromValue(nodeInfo.getNetwork());
    }

    @Override
    public int getStorageFeeFactor() {
        return nodeInfo.getParameters().getStorageFeeFactor();
    }

    @Override
    public int getMinValuePerByte() {
        return nodeInfo.getParameters().getMinValuePerByte();
    }

    @Override
    public int getMaxBlockSize() {
        return nodeInfo.getParameters().getMaxBlockSize();
    }

    @Override
    public int getTokenAccessCost() {
        return nodeInfo.getParameters().getTokenAccessCost();
    }

    @Override
    public int getInputCost() {
        return nodeInfo.getParameters().getInputCost();
    }

    @Override
    public int getDataInputCost() {
        return nodeInfo.getParameters().getDataInputCost();
    }

    @Override
    public int getOutputCost() {
        return nodeInfo.getParameters().getOutputCost();
    }

    @Override
    public long getMaxBlockCost() {
        return nodeInfo.getParameters().getMaxBlockCost().longValue();
    }

    @Override
    public byte getBlockVersion() {
        return nodeInfo.getParameters().getBlockVersion().byteValue();
    }
}
