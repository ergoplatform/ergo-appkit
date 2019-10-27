package org.ergoplatform.example.util;

import org.ergoplatform.polyglot.BlockchainContext;
import org.ergoplatform.polyglot.ErgoClient;
import org.ergoplatform.polyglot.NetworkType;
import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.restapi.client.ApiClient;

import java.util.function.Function;

public class BlockchainErgoClient implements ErgoClient {
    private final String _nodeUrl;
    private final NetworkType _networkType;

    public BlockchainErgoClient(String nodeUrl, NetworkType networkType) {
        _nodeUrl = nodeUrl;
        _networkType = networkType;
    }

    @Override
    public <T> T execute(Function<BlockchainContext, T> action) {
        ApiClient client = new ApiClient(_nodeUrl);
        BlockchainContext ctx = new BlockchainContextBuilderImpl(client, _networkType).build();
        T res = action.apply(ctx);
        return res;
    }
}
