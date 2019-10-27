package org.ergoplatform.example.util;

import org.ergoplatform.polyglot.BlockchainContext;
import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.restapi.client.ApiClient;

import java.util.function.Function;

public class BlockchainRunner implements Runner {
    private final String _nodeUrl;
    private final byte _networkPrefix;

    public BlockchainRunner(String nodeUrl, byte networkPrefix) {
        _nodeUrl = nodeUrl;
        _networkPrefix = networkPrefix;
    }

    @Override
    public <T> T run(Function<BlockchainContext, T> action) {
        ApiClient client = new ApiClient(_nodeUrl);
        BlockchainContext ctx = new BlockchainContextBuilderImpl(client, _networkPrefix).build();
        T res = action.apply(ctx);
        return res;
    }
}
