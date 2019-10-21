package org.ergoplatform.polyglot;

import org.ergoplatform.api.client.NodeInfo;

public class BlockchainContextImpl implements BlockchainContext {

    private final byte _networkPrefix;
    private final NodeInfo _nodeInfo;

    public BlockchainContextImpl(byte networkPrefix, NodeInfo nodeInfo) {
        _networkPrefix = networkPrefix;
        _nodeInfo = nodeInfo;
    }

    @Override
    public UnsignedTransactionBuilder newUnsignedTransaction() {
        return new UnsignedTransactionBuilderImpl(this);
    }

    public byte getNetworkPrefix() {
        return _networkPrefix;
    }
}

