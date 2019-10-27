package org.ergoplatform.polyglot.impl;

import org.ergoplatform.polyglot.*;
import org.ergoplatform.restapi.client.BlockHeader;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.NodeInfo;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;

public class BlockchainContextImpl implements BlockchainContext {

    private final Retrofit _retrofit;
    private final NetworkType _networkType;
    private final NodeInfo _nodeInfo;
    private final List<BlockHeader> _headers;

    public BlockchainContextImpl(
            Retrofit retrofit, NetworkType networkType, NodeInfo nodeInfo, List<BlockHeader> headers) {
        _retrofit = retrofit;
        _networkType = networkType;
        _nodeInfo = nodeInfo;
        _headers = headers;
    }

    @Override
    public UnsignedTransactionBuilder newTxBuilder() {
        return new UnsignedTransactionBuilderImpl(this);
    }

    @Override
    public InputBox[] getBoxesById(String... boxIds) throws ErgoClientException {
        List<InputBox> list = new ArrayList<>();
        for (String id : boxIds) {
            ErgoTransactionOutput boxData = ErgoNodeFacade.getBoxById(_retrofit, id);
            if (boxData == null) {
                throw new ErgoClientException("Cannot load UTXO box " + id, null);
            }
            list.add(new InputBoxImpl(boxData));
        }
        InputBox[] inputs = list.toArray(new InputBox[0]);
        return inputs;
    }

    @Override
    public ErgoProverBuilder newProverBuilder() {
        return new ErgoProverBuilderImpl(this);
    }

    @Override
    public NetworkType getNetworkType() {
        return _networkType;
    }

    @Override
    public int getHeight() { return _headers.get(0).getHeight(); }

    /*=====  Package-private methods accessible from other Impl classes. =====*/

    Retrofit getRetrofit() {
        return _retrofit;
    }

    public NodeInfo getNodeInfo() {
        return _nodeInfo;
    }

    public List<BlockHeader> getHeaders() {
        return _headers;
    }
}

