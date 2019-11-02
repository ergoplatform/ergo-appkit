package org.ergoplatform.example.util;

import org.ergoplatform.polyglot.BlockchainContext;
import org.ergoplatform.polyglot.ErgoClient;
import org.ergoplatform.polyglot.NetworkType;
import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.restapi.client.ApiClient;

import java.util.function.Function;

/**
 * This implementation of {@link ErgoClient} uses REST API of Ergo node for communication.
 */
public class RestApiErgoClient implements ErgoClient {
    private final String _nodeUrl;
    private final NetworkType _networkType;

    /**
     * Create and initialize a new instance.
     *
     * @param nodeUrl http url to Ergo node REST API endpoint of the form `https://host:port/`.
     */
    public RestApiErgoClient(String nodeUrl, NetworkType networkType) {
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
