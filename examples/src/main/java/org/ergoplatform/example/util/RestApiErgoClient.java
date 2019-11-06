package org.ergoplatform.example.util;

import org.ergoplatform.ergotool.ErgoNodeConfig;
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
    private final ApiClient _client;

    /**
     * Create and initialize a new instance.
     *
     * @param nodeUrl http url to Ergo node REST API endpoint of the form `https://host:port/`.
     * @param apiKey  api key to authenticate this client
     */
    RestApiErgoClient(String nodeUrl, NetworkType networkType, String apiKey) {
        _nodeUrl = nodeUrl;
        _networkType = networkType;
        _client = new ApiClient(_nodeUrl, "ApiKeyAuth", apiKey);
    }

    @Override
    public <T> T execute(Function<BlockchainContext, T> action) {
        BlockchainContext ctx = new BlockchainContextBuilderImpl(_client, _networkType).build();
        T res = action.apply(ctx);
        return res;
    }

    public static ErgoClient create(String nodeUrl, NetworkType networkType, String apiKey) {
        return new RestApiErgoClient(nodeUrl, networkType, apiKey);
    }

    public static ErgoClient create(ErgoNodeConfig nodeConf) {
        return RestApiErgoClient.create(
                nodeConf.getNodeApi().getApiUrl(),
                nodeConf.getNetworkType(),
                nodeConf.getNodeApi().getApiKey());
    }

    ApiClient getApiClient() {
        return _client;
    }
}
