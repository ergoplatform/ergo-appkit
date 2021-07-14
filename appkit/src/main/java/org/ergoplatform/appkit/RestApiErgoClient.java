package org.ergoplatform.appkit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ergoplatform.appkit.config.ErgoNodeConfig;
import org.ergoplatform.appkit.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.explorer.client.ExplorerApiClient;
import org.ergoplatform.restapi.client.ApiClient;

import java.util.function.Function;

/**
 * This implementation of {@link ErgoClient} uses REST API of Ergo node for communication.
 */
public class RestApiErgoClient implements ErgoClient {
    private final String _nodeUrl;
    private final NetworkType _networkType;
    private final ApiClient _client;
    private final String _explorerUrl;
    private final ExplorerApiClient _explorer;

    public final static String defaultMainnetExplorerUrl = "https://api.ergoplatform.com";
    public final static String defaultTestnetExplorerUrl = "https://api-testnet.ergoplatform.com";

    /**
     * Create and initialize a new instance.
     *
     * @param nodeUrl     http url to Ergo node REST API endpoint of the form
     *                    `https://host[:port]` where port is optional.
     * @param networkType type of network (mainnet, testnet) the Ergo node is part of
     * @param apiKey      api key to authenticate this client
     * @param explorerUrl Optional http url to Ergo Explorer REST API endpoint of the
     *                    form `https://host[:port]` where port is optional.
     *                    If `null` or empty string passed then the Explorer client is not
     *                    initialized and the client works in the `node only` mode.
     */
    RestApiErgoClient(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl) {
        _nodeUrl = nodeUrl;
        _networkType = networkType;
        _client = new ApiClient(_nodeUrl, "ApiKeyAuth", apiKey);
        _explorerUrl = explorerUrl;
        if (Strings.isNullOrEmpty(_explorerUrl)) {
            _explorer = new ExplorerApiClient(_explorerUrl);
        } else {
            _explorer = null;
        }
    }

    @Override
    public <T> T execute(Function<BlockchainContext, T> action) {
        BlockchainContext ctx = new BlockchainContextBuilderImpl(_client, _explorer, _networkType).build();
        T res = action.apply(ctx);
        return res;
    }

    /**
     * Returns the default URL for the given network type.
     */
    public static String getDefaultExplorerUrl(NetworkType networkType) {
        switch (networkType) {
        case MAINNET:
            return defaultMainnetExplorerUrl;
        default:
            return defaultTestnetExplorerUrl;
        }
    }

    /**
     * Creates a new {@link ErgoClient} instance connected to a given node of the given network type.
     *
     * @param nodeUrl     http url to Ergo node REST API endpoint of the form `https://host:port/`
     * @param networkType type of network (mainnet, testnet) the Ergo node is part of
     * @param apiKey      api key to authenticate this client
     * @return a new instance of {@link ErgoClient} connected to a given node
     */
    public static ErgoClient create(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl) {
        return new RestApiErgoClient(nodeUrl, networkType, apiKey, explorerUrl);
    }

    /**
     * Create a new {@link ErgoClient} instance using node configuration parameters.
     */
    public static ErgoClient create(ErgoNodeConfig nodeConf, String explorerUrl) {
        return RestApiErgoClient.create(
                nodeConf.getNodeApi().getApiUrl(),
                nodeConf.getNetworkType(),
                nodeConf.getNodeApi().getApiKey(),
                explorerUrl);
    }

    /**
     * Get underlying Ergo node REST API typed client.
     */
    ApiClient getNodeApiClient() {
        return _client;
    }

    /**
     * Get underlying Ergo Network Explorer REST API typed client.
     */
    ExplorerApiClient getExplorerApiClient() {
        Preconditions.checkNotNull(_explorer, ErgoClient.explorerUrlNotSpecifiedMessage);
        return _explorer;
    }

}
