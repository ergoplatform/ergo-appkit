package org.ergoplatform.polyglot;

import org.ergoplatform.api.client.BlockHeader;
import org.ergoplatform.api.client.NodeInfo;

import java.util.List;

public class BlockchainContextImpl implements BlockchainContext {

    private final byte _networkPrefix;
    private final NodeInfo _nodeInfo;
    private final List<BlockHeader> _headers;

    public BlockchainContextImpl(
            byte networkPrefix, NodeInfo nodeInfo, List<BlockHeader> headers) {
        _networkPrefix = networkPrefix;
        _nodeInfo = nodeInfo;
        _headers = headers;
    }

    @Override
    public UnsignedTransactionBuilder newUnsignedTransaction() {
        return new UnsignedTransactionBuilderImpl(this);
    }

    public byte getNetworkPrefix() {
        return _networkPrefix;
    }
}

