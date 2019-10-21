package org.ergoplatform.polyglot;

import org.ergoplatform.api.client.BlockHeader;
import org.ergoplatform.api.client.ErgoTransactionOutput;
import org.ergoplatform.api.client.NodeInfo;
import retrofit2.Retrofit;

import java.util.List;

public class BlockchainContextImpl implements BlockchainContext {

    private final Retrofit _retrofit;
    private final byte _networkPrefix;
    private final NodeInfo _nodeInfo;
    private final List<BlockHeader> _headers;

    public BlockchainContextImpl(
            Retrofit retrofit, byte networkPrefix, NodeInfo nodeInfo, List<BlockHeader> headers) {
        _retrofit = retrofit;
        _networkPrefix = networkPrefix;
        _nodeInfo = nodeInfo;
        _headers = headers;
    }

    @Override
    public UnsignedTransactionBuilder newUnsignedTransaction() {
        return new UnsignedTransactionBuilderImpl(this);
    }

    @Override
    public InputBox getBoxById(String boxId) throws ErgoClientException {
        ErgoTransactionOutput boxData = ErgoNodeFacade.getBoxById(_retrofit, boxId);
        if (boxData == null) {
            throw new ErgoClientException("Cannot load UTXO box " + boxId, null);
        }
        return new InputBoxImpl(boxData);
    }

    public byte getNetworkPrefix() {
        return _networkPrefix;
    }

    public Retrofit getRetrofit() {
        return _retrofit;
    }

    public NodeInfo getNodeInfo() {
        return _nodeInfo;
    }

    public List<BlockHeader> getHeaders() {
        return _headers;
    }
}

